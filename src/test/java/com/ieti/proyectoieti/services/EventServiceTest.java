package com.ieti.proyectoieti.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ieti.proyectoieti.models.Event;
import com.ieti.proyectoieti.repositories.EventRepository;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

  @Mock
  private EventRepository eventRepository;

  @InjectMocks
  private EventService eventService;

  private Event testEvent;
  private final String TITLE = "Test Event";
  private final String DESCRIPTION = "Test Description";
  private final LocalDate DATE = LocalDate.now().plusDays(7);
  private final String LOCATION = "Test Location";
  private final String CATEGORY = "Test Category";

  @BeforeEach
  void setUp() {
    testEvent = new Event(TITLE, DESCRIPTION, DATE, LOCATION, CATEGORY);
  }

  @Test
  void createEvent_ValidParameters_ReturnsEvent() {
    when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

    Event event = eventService.createEvent(TITLE, DESCRIPTION, DATE, LOCATION, CATEGORY);

    assertNotNull(event);
    assertEquals(TITLE, event.getTitle());
    assertEquals(DESCRIPTION, event.getDescription());
    assertEquals(DATE, event.getDate());
    assertEquals(LOCATION, event.getLocation());
    assertEquals(CATEGORY, event.getCategory());
    verify(eventRepository).save(any(Event.class));
  }

  @Test
  void createEvent_NullCategory_UsesDefaultCategory() {
    when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

    Event event = eventService.createEvent(TITLE, DESCRIPTION, DATE, LOCATION, null);

    assertNotNull(event);
    verify(eventRepository).save(any(Event.class));
  }

  @Test
  void createEvent_MissingRequiredFields_ThrowsException() {
    assertThrows(
            IllegalArgumentException.class,
            () -> eventService.createEvent(null, DESCRIPTION, DATE, LOCATION, CATEGORY));

    assertThrows(
            IllegalArgumentException.class,
            () -> eventService.createEvent(TITLE, DESCRIPTION, null, LOCATION, CATEGORY));

    assertThrows(
            IllegalArgumentException.class,
            () -> eventService.createEvent(TITLE, DESCRIPTION, DATE, null, CATEGORY));

    verify(eventRepository, never()).save(any(Event.class));
  }

  @Test
  void getEvents_ReturnsAllEvents() {
    // Arrange
    List<Event> events = Arrays.asList(testEvent);
    when(eventRepository.findAll()).thenReturn(events);

    List<Event> result = eventService.getEvents();

    assertEquals(1, result.size());
    assertTrue(result.contains(testEvent));
  }

  @Test
  void getEventsByCategory_ValidCategory_ReturnsEvents() {
    List<Event> events = Arrays.asList(testEvent);
    when(eventRepository.findByCategory(CATEGORY)).thenReturn(events);

    List<Event> result = eventService.getEventsByCategory(CATEGORY);

    assertEquals(1, result.size());
    verify(eventRepository).findByCategory(CATEGORY);
  }

  @Test
  void getUpcomingEvents_ReturnsUpcomingEvents() {
    List<Event> events = Arrays.asList(testEvent);
    when(eventRepository.findByDateAfter(any(LocalDate.class))).thenReturn(events);

    List<Event> result = eventService.getUpcomingEvents();

    assertEquals(1, result.size());
    verify(eventRepository).findByDateAfter(any(LocalDate.class));
  }

  @Test
  void searchEventsByLocation_ValidLocation_ReturnsEvents() {
    List<Event> events = Arrays.asList(testEvent);
    when(eventRepository.findByLocationContainingIgnoreCase(LOCATION)).thenReturn(events);

    List<Event> result = eventService.searchEventsByLocation(LOCATION);

    assertEquals(1, result.size());
    verify(eventRepository).findByLocationContainingIgnoreCase(LOCATION);
  }

  @Test
  void deleteEvent_ExistingEvent_DeletesEvent() {
    String eventId = "event-123";
    when(eventRepository.existsById(eventId)).thenReturn(true);
    doNothing().when(eventRepository).deleteById(eventId);

    assertDoesNotThrow(() -> eventService.deleteEvent(eventId));
    verify(eventRepository).deleteById(eventId);
  }

  @Test
  void deleteEvent_NonExistingEvent_ThrowsException() {
    String eventId = "non-existing-event";
    when(eventRepository.existsById(eventId)).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> eventService.deleteEvent(eventId));
    verify(eventRepository, never()).deleteById(eventId);
  }
}