package com.ieti.proyectoieti.Services;



import com.ieti.proyectoieti.Models.Event;
import com.ieti.proyectoieti.Repositories.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    public Event createEvent(String title, String description, LocalDate date, String location, String category) {
        if (title == null || date == null || location == null) {
            throw new IllegalArgumentException("Missing required fields: title, date, location");
        }
        Event newEvent = new Event(title, description, date, location, category);
        return eventRepository.save(newEvent);
    }

    public List<Event> getEvents() {
        return eventRepository.findAll();
    }
}
