package com.ieti.proyectoieti.services;

import static org.junit.jupiter.api.Assertions.*;

import com.ieti.proyectoieti.models.Event;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventServiceTest {

  private EventService eventService;
  private final String TITLE = "Test Event";
  private final String DESCRIPTION = "Test Description";
  private final LocalDate DATE = LocalDate.now().plusDays(7);
  private final String LOCATION = "Test Location";
  private final String CATEGORY = "Test Category";

  @BeforeEach
  void setUp() {
    eventService = new EventService();
  }

  @Test
  void createEvent_ValidParameters_ReturnsEvent() {
    Event event = eventService.createEvent(TITLE, DESCRIPTION, DATE, LOCATION, CATEGORY);

    assertNotNull(event);
    assertEquals(TITLE, event.getTitle());
    assertEquals(DESCRIPTION, event.getDescription());
    assertEquals(DATE, event.getDate());
    assertEquals(LOCATION, event.getLocation());
    assertEquals(CATEGORY, event.getCategory());
  }

  @Test
  void createEvent_NullCategory_UsesDefaultCategory() {
    Event event = eventService.createEvent(TITLE, DESCRIPTION, DATE, LOCATION, null);

    assertNotNull(event);
    assertEquals("General", event.getCategory());
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
  }

  @Test
  void getEvents_AfterCreatingEvents_ReturnsAllEvents() {
    Event event1 = eventService.createEvent("Event 1", "Desc 1", DATE, "Loc 1", "Cat 1");
    Event event2 = eventService.createEvent("Event 2", "Desc 2", DATE, "Loc 2", "Cat 2");

    List<Event> events = eventService.getEvents();

    assertEquals(2, events.size());
    assertTrue(events.contains(event1));
    assertTrue(events.contains(event2));
  }

  @Test
  void getEvents_NoEvents_ReturnsEmptyList() {
    List<Event> events = eventService.getEvents();

    assertNotNull(events);
    assertTrue(events.isEmpty());
  }
}
