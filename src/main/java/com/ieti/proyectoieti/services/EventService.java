package com.ieti.proyectoieti.services;

import com.ieti.proyectoieti.models.Event;
import com.ieti.proyectoieti.repositories.EventRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EventService {

  private final EventRepository eventRepository;

  public EventService(EventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  public Event createEvent(
          String title, String description, LocalDate date, String location, String category) {
    if (title == null || date == null || location == null) {
      throw new IllegalArgumentException("Missing required fields: title, date, location");
    }
    Event newEvent = new Event(title, description, date, location, category);
    return eventRepository.save(newEvent);
  }

  public List<Event> getEvents() {
    return eventRepository.findAll();
  }

  public List<Event> getEventsByCategory(String category) {
    return eventRepository.findByCategory(category);
  }

  public List<Event> getUpcomingEvents() {
    return eventRepository.findByDateAfter(LocalDate.now());
  }

  public List<Event> searchEventsByLocation(String location) {
    return eventRepository.findByLocationContainingIgnoreCase(location);
  }

  public void deleteEvent(String eventId) {
    if (!eventRepository.existsById(eventId)) {
      throw new IllegalArgumentException("Event not found with ID: " + eventId);
    }
    eventRepository.deleteById(eventId);
  }
}