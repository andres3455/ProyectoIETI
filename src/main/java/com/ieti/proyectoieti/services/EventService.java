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

  public Event getEventById(String eventId) {
    return eventRepository.findById(eventId)
        .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
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

  public List<Event> saveAll(List<Event> events) {
      return eventRepository.saveAll(events);
  }

  /**
   * Confirms a user's attendance to an event
   * @param eventId The ID of the event
   * @param userId The ID of the user confirming attendance
   * @return The updated event
   * @throws IllegalArgumentException if event not found
   */
  public Event confirmAttendance(String eventId, String userId) {
    Event event = getEventById(eventId);
    event.addAttendee(userId);
    return eventRepository.save(event);
  }

  /**
   * Cancels a user's attendance to an event
   * @param eventId The ID of the event
   * @param userId The ID of the user canceling attendance
   * @return The updated event
   * @throws IllegalArgumentException if event not found
   */
  public Event cancelAttendance(String eventId, String userId) {
    Event event = getEventById(eventId);
    event.removeAttendee(userId);
    return eventRepository.save(event);
  }

  /**
   * Confirms attendance for multiple users to an event
   * @param eventId The ID of the event
   * @param userIds List of user IDs confirming attendance
   * @return The updated event
   * @throws IllegalArgumentException if event not found
   */
  public Event confirmGroupAttendance(String eventId, List<String> userIds) {
    Event event = getEventById(eventId);
    for (String userId : userIds) {
      event.addAttendee(userId);
    }
    return eventRepository.save(event);
  }

  /**
   * Gets the list of attendees for an event
   * @param eventId The ID of the event
   * @return List of user IDs who confirmed attendance
   * @throws IllegalArgumentException if event not found
   */
  public List<String> getEventAttendees(String eventId) {
    Event event = getEventById(eventId);
    return event.getAttendeeIds();
  }

  /**
   * Gets all events where the specified user has confirmed attendance
   * @param userId The ID of the user
   * @return List of events the user is attending
   */
  public List<Event> getEventsByAttendee(String userId) {
    return eventRepository.findByAttendeeIdsContaining(userId);
  }
}