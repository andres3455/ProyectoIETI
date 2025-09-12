package com.ieti.proyectoieti.Controllers;

import com.ieti.proyectoieti.Models.Event;
import com.ieti.proyectoieti.Services.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
@Tag(name = "Events", description = "Event management APIs")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @Operation(
            summary = "Create a new event",
            description = "Creates a new event with the provided details"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event created successfully",
                    content = @Content(schema = @Schema(implementation = Event.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    })
    @PostMapping
    public Event createEvent(
            @Parameter(description = "Event creation details", required = true)
            @RequestBody Map<String, String> body) {
        String title = body.get("title");
        String description = body.getOrDefault("description", "");
        LocalDate date = LocalDate.parse(body.get("date"));
        String location = body.get("location");
        String category = body.getOrDefault("category", "General");

        return eventService.createEvent(title, description, date, location, category);
    }

    @Operation(
            summary = "Get all events",
            description = "Retrieves a list of all events"
    )
    @ApiResponse(responseCode = "200", description = "List of events retrieved successfully",
            content = @Content(schema = @Schema(implementation = Event[].class)))
    @GetMapping
    public List<Event> getEvents() {
        return eventService.getEvents();
    }
}