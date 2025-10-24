package com.ieti.proyectoieti.controllers;

import com.ieti.proyectoieti.controllers.dto.EventRequest;
import com.ieti.proyectoieti.models.Event;
import com.ieti.proyectoieti.services.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
@Tag(name = "Events", description = "Event management APIs")
public class EventController {

  private final EventService eventService;

  public EventController(EventService eventService) {
    this.eventService = eventService;
  }

  @Operation(summary = "Create a new event", description = "Creates a new event with the provided details")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Event created successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid input parameters")
  })
  @PostMapping
  public ResponseEntity<Event> createEvent(@Valid @RequestBody EventRequest eventRequest) {
    Event event = eventService.createEvent(
            eventRequest.getTitle(),
            eventRequest.getDescription(),
            eventRequest.getDate(),
            eventRequest.getLocation(),
            eventRequest.getCategory());
    return ResponseEntity.ok(event);
  }

  @Operation(summary = "Get all events", description = "Retrieves a list of all events")
  @ApiResponse(responseCode = "200", description = "List of events retrieved successfully")
  @GetMapping
  public ResponseEntity<List<Event>> getEvents() {
    return ResponseEntity.ok(eventService.getEvents());
  }

  @Operation(summary = "Get events by category", description = "Retrieves events filtered by category")
  @GetMapping("/category/{category}")
  public ResponseEntity<List<Event>> getEventsByCategory(@PathVariable String category) {
    return ResponseEntity.ok(eventService.getEventsByCategory(category));
  }

  @Operation(summary = "Get upcoming events", description = "Retrieves events from today onwards")
  @GetMapping("/upcoming")
  public ResponseEntity<List<Event>> getUpcomingEvents() {
    return ResponseEntity.ok(eventService.getUpcomingEvents());
  }

  @Operation(summary = "Delete event", description = "Deletes an event by ID")
  @DeleteMapping("/{eventId}")
  public ResponseEntity<Void> deleteEvent(@PathVariable String eventId) {
    eventService.deleteEvent(eventId);
    return ResponseEntity.ok().build();
  }
}