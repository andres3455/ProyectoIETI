package com.ieti.proyectoieti.Controllers;



import com.ieti.proyectoieti.Models.Event;
import com.ieti.proyectoieti.Services.EventService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public Event createEvent(@RequestBody Map<String, String> body) {
        String title = body.get("title");
        String description = body.getOrDefault("description", "");
        LocalDate date = LocalDate.parse(body.get("date"));
        String location = body.get("location");
        String category = body.getOrDefault("category", "General");

        return eventService.createEvent(title, description, date, location, category);
    }

    @GetMapping
    public List<Event> getEvents() {
        return eventService.getEvents();
    }
}
