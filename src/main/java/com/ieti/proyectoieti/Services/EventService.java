package com.ieti.proyectoieti.Services;



import com.ieti.proyectoieti.Models.Event;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    private final List<Event> events = new ArrayList<>();

    public Event createEvent(String title, String description, LocalDate date, String location, String category) {
        if (title == null || date == null || location == null) {
            throw new IllegalArgumentException("Missing required fields: title, date, location");
        }
        Event newEvent = new Event(title, description, date, location, category);
        events.add(newEvent);
        return newEvent;
    }

    public List<Event> getEvents() {
        return events;
    }
}
