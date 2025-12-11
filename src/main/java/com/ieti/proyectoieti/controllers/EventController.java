package com.ieti.proyectoieti.controllers;

import com.ieti.proyectoieti.controllers.dto.EventRequest;
import com.ieti.proyectoieti.models.Event;
import com.ieti.proyectoieti.services.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Event management APIs")
public class EventController {

  private static final Logger logger = LoggerFactory.getLogger(EventController.class);
  private final EventService eventService;

  public EventController(EventService eventService) {
    this.eventService = eventService;
  }

  @Operation(
      summary = "Create a new event",
      description = "Creates a new event with the provided details. Requires authentication.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Event created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
    @ApiResponse(responseCode = "401", description = "User not authenticated")
  })
  @PostMapping
  public ResponseEntity<?> createEvent(
      @Valid @RequestBody EventRequest eventRequest, HttpServletRequest request) {

    // Get authenticated user information from JWT filter
    String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");
    String authenticatedUserEmail = (String) request.getAttribute("authenticatedUserEmail");

    if (authenticatedUserId == null) {
      logger.warn("Attempt to create event without authentication");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Authentication required", "message", "Please log in to create events"));
    }

    logger.info(
        "User {} ({}) creating event: {}",
        authenticatedUserEmail,
        authenticatedUserId,
        eventRequest.getTitle());

    Event event =
        eventService.createEvent(
            eventRequest.getTitle(),
            eventRequest.getDescription(),
            eventRequest.getDate(),
            eventRequest.getLocation(),
            eventRequest.getCategory());

    return ResponseEntity.ok(event);
  }

  @Operation(
      summary = "Get all events",
      description = "Retrieves a list of all events. Public endpoint, no authentication required.")
  @ApiResponse(responseCode = "200", description = "List of events retrieved successfully")
  @GetMapping
  public ResponseEntity<List<Event>> getEvents(HttpServletRequest request) {
    String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");
    if (authenticatedUserId != null) {
      logger.debug("Authenticated user {} fetching all events", authenticatedUserId);
    } else {
      logger.debug("Anonymous user fetching all events");
    }
    return ResponseEntity.ok(eventService.getEvents());
  }

  @Operation(
      summary = "Get event by ID",
      description = "Retrieves a specific event by its ID. Public endpoint, no authentication required.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Event retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Event not found")
  })
  @GetMapping("/{eventId}")
  public ResponseEntity<?> getEventById(
      @PathVariable String eventId, HttpServletRequest request) {
    String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");
    logger.debug(
        "User {} fetching event: {}",
        authenticatedUserId != null ? authenticatedUserId : "anonymous",
        eventId);

    try {
      Event event = eventService.getEventById(eventId);
      return ResponseEntity.ok(event);
    } catch (IllegalArgumentException e) {
      logger.warn("Event not found: {}", eventId);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Event not found", "eventId", eventId));
    } catch (Exception e) {
      logger.error("Error fetching event {}: {}", eventId, e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to fetch event", "message", e.getMessage()));
    }
  }

  @Operation(
      summary = "Get events by category",
      description = "Retrieves events filtered by category. Public endpoint, no authentication required.")
  @ApiResponse(responseCode = "200", description = "Events retrieved successfully")
  @GetMapping("/category/{category}")
  public ResponseEntity<List<Event>> getEventsByCategory(
      @PathVariable String category, HttpServletRequest request) {
    String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");
    logger.debug(
        "User {} fetching events for category: {}",
        authenticatedUserId != null ? authenticatedUserId : "anonymous",
        category);
    return ResponseEntity.ok(eventService.getEventsByCategory(category));
  }

  @Operation(
      summary = "Get upcoming events",
      description = "Retrieves events from today onwards. Public endpoint, no authentication required.")
  @ApiResponse(responseCode = "200", description = "Upcoming events retrieved successfully")
  @GetMapping("/upcoming")
  public ResponseEntity<List<Event>> getUpcomingEvents(HttpServletRequest request) {
    String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");
    logger.debug(
        "User {} fetching upcoming events",
        authenticatedUserId != null ? authenticatedUserId : "anonymous");
    return ResponseEntity.ok(eventService.getUpcomingEvents());
  }

  @Operation(
      summary = "Delete event",
      description = "Deletes an event by ID. Requires authentication.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Event deleted successfully"),
    @ApiResponse(responseCode = "401", description = "User not authenticated"),
    @ApiResponse(responseCode = "404", description = "Event not found")
  })
  @DeleteMapping("/{eventId}")
  public ResponseEntity<?> deleteEvent(
      @PathVariable String eventId, HttpServletRequest request) {

    // Get authenticated user information from JWT filter
    String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");
    String authenticatedUserEmail = (String) request.getAttribute("authenticatedUserEmail");

    if (authenticatedUserId == null) {
      logger.warn("Attempt to delete event {} without authentication", eventId);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Authentication required", "message", "Please log in to delete events"));
    }

    logger.info(
        "User {} ({}) deleting event: {}",
        authenticatedUserEmail,
        authenticatedUserId,
        eventId);

    try {
      eventService.deleteEvent(eventId);
      return ResponseEntity.ok(Map.of("message", "Event deleted successfully", "eventId", eventId));
    } catch (Exception e) {
      logger.error("Error deleting event {}: {}", eventId, e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to delete event", "message", e.getMessage()));
    }
  }

    @PostMapping("/bulk")
    public ResponseEntity<?> uploadEventsBulk(@RequestBody List<Event> events) {

        if (events == null || events.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Event list is empty"));
        }

        List<Event> recreated = events.stream()
                .map(e -> new Event(
                        e.getTitle(),
                        e.getDescription(),
                        e.getDate(),
                        e.getLocation(),
                        e.getCategory()
                ))
                .toList();

        List<Event> saved = eventService.saveAll(recreated);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Events uploaded successfully",
                        "count", saved.size()
                )
        );
    }

  @Operation(
      summary = "Confirm attendance",
      description = "Confirms the authenticated user's attendance to an event. Requires authentication.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Attendance confirmed successfully"),
    @ApiResponse(responseCode = "401", description = "User not authenticated"),
    @ApiResponse(responseCode = "404", description = "Event not found")
  })
  @PostMapping("/{eventId}/attend")
  public ResponseEntity<?> confirmAttendance(
      @PathVariable String eventId, HttpServletRequest request) {

    String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");
    String authenticatedUserEmail = (String) request.getAttribute("authenticatedUserEmail");

    if (authenticatedUserId == null) {
      logger.warn("Attempt to confirm attendance without authentication");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Authentication required", "message", "Please log in to confirm attendance"));
    }

    logger.info("User {} ({}) confirming attendance to event {}", 
        authenticatedUserEmail, authenticatedUserId, eventId);

    try {
      Event event = eventService.confirmAttendance(eventId, authenticatedUserId);
      return ResponseEntity.ok(Map.of(
          "message", "Attendance confirmed successfully",
          "eventId", eventId,
          "attendeeCount", event.getAttendeeCount()
      ));
    } catch (IllegalArgumentException e) {
      logger.warn("Event not found: {}", eventId);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Event not found", "eventId", eventId));
    } catch (Exception e) {
      logger.error("Error confirming attendance for event {}: {}", eventId, e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to confirm attendance", "message", e.getMessage()));
    }
  }

  @Operation(
      summary = "Cancel attendance",
      description = "Cancels the authenticated user's attendance to an event. Requires authentication.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Attendance canceled successfully"),
    @ApiResponse(responseCode = "401", description = "User not authenticated"),
    @ApiResponse(responseCode = "404", description = "Event not found")
  })
  @DeleteMapping("/{eventId}/attend")
  public ResponseEntity<?> cancelAttendance(
      @PathVariable String eventId, HttpServletRequest request) {

    String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");
    String authenticatedUserEmail = (String) request.getAttribute("authenticatedUserEmail");

    if (authenticatedUserId == null) {
      logger.warn("Attempt to cancel attendance without authentication");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Authentication required", "message", "Please log in to cancel attendance"));
    }

    logger.info("User {} ({}) canceling attendance to event {}", 
        authenticatedUserEmail, authenticatedUserId, eventId);

    try {
      Event event = eventService.cancelAttendance(eventId, authenticatedUserId);
      return ResponseEntity.ok(Map.of(
          "message", "Attendance canceled successfully",
          "eventId", eventId,
          "attendeeCount", event.getAttendeeCount()
      ));
    } catch (IllegalArgumentException e) {
      logger.warn("Event not found: {}", eventId);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Event not found", "eventId", eventId));
    } catch (Exception e) {
      logger.error("Error canceling attendance for event {}: {}", eventId, e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to cancel attendance", "message", e.getMessage()));
    }
  }

  @Operation(
      summary = "Get event attendees",
      description = "Retrieves the list of user IDs who confirmed attendance. Public endpoint.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Attendee list retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Event not found")
  })
  @GetMapping("/{eventId}/attendees")
  public ResponseEntity<?> getEventAttendees(
      @PathVariable String eventId, HttpServletRequest request) {

    logger.debug("Fetching attendees for event {}", eventId);

    try {
      List<String> attendees = eventService.getEventAttendees(eventId);
      return ResponseEntity.ok(Map.of(
          "eventId", eventId,
          "attendees", attendees,
          "count", attendees.size()
      ));
    } catch (IllegalArgumentException e) {
      logger.warn("Event not found: {}", eventId);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(Map.of("error", "Event not found", "eventId", eventId));
    } catch (Exception e) {
      logger.error("Error fetching attendees for event {}: {}", eventId, e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to fetch attendees", "message", e.getMessage()));
    }
  }

  @Operation(
      summary = "Get user's confirmed events",
      description = "Retrieves all events where the authenticated user has confirmed attendance. Requires authentication.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "User's confirmed events retrieved successfully"),
    @ApiResponse(responseCode = "401", description = "User not authenticated")
  })
  @GetMapping("/user/confirmed")
  public ResponseEntity<?> getUserConfirmedEvents(HttpServletRequest request) {

    String authenticatedUserId = (String) request.getAttribute("authenticatedUserId");
    String authenticatedUserEmail = (String) request.getAttribute("authenticatedUserEmail");

    if (authenticatedUserId == null) {
      logger.warn("Attempt to fetch confirmed events without authentication");
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Authentication required", "message", "Please log in to view your confirmed events"));
    }

    logger.info("User {} ({}) fetching confirmed events", authenticatedUserEmail, authenticatedUserId);

    try {
      List<Event> events = eventService.getEventsByAttendee(authenticatedUserId);
      return ResponseEntity.ok(events);
    } catch (Exception e) {
      logger.error("Error fetching confirmed events for user {}: {}", authenticatedUserId, e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to fetch confirmed events", "message", e.getMessage()));
    }
  }

  @Operation(
      summary = "Clear all attendees from all events",
      description = "⚠️ WARNING: Removes all attendees from all events. Public endpoint for one-time cleanup. DELETE THIS ENDPOINT AFTER USE!")
  @ApiResponse(responseCode = "200", description = "All attendees cleared successfully")
  @DeleteMapping("/admin/clear-attendees")
  public ResponseEntity<?> clearAllAttendees() {

    logger.warn("⚠️⚠️⚠️ DANGER: Clearing all attendees from all events - PUBLIC ENDPOINT");

    try {
      int updatedCount = eventService.clearAllAttendees();
      logger.info("Successfully cleared attendees from {} events", updatedCount);
      return ResponseEntity.ok(Map.of(
          "message", "All attendees cleared successfully",
          "eventsUpdated", updatedCount,
          "warning", "⚠️ REMEMBER TO DELETE THIS ENDPOINT AFTER USE!"
      ));
    } catch (Exception e) {
      logger.error("Error clearing all attendees: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "Failed to clear attendees", "message", e.getMessage()));
    }
  }

}