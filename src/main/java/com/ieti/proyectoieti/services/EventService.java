package com.ieti.proyectoieti.services;

import com.ieti.proyectoieti.models.Event;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EventService {

  private final List<Event> events = new ArrayList<>();

  public Event createEvent(
      String title, String description, LocalDate date, String location, String category) {
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
