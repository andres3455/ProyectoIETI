package com.ieti.proyectoieti.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.ieti.proyectoieti.controllers.dto.EventRequest;
import com.ieti.proyectoieti.models.Event;
import com.ieti.proyectoieti.services.EventService;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private Event testEvent;
    private EventRequest eventRequest;

    @BeforeEach
    void setUp() {
        testEvent = new Event("Test Event", "Test Description", LocalDate.now().plusDays(7), "Test Location", "Meeting");

        eventRequest = new EventRequest();
        eventRequest.setTitle("Test Event");
        eventRequest.setDescription("Test Description");
        eventRequest.setDate(LocalDate.now().plusDays(7));
        eventRequest.setLocation("Test Location");
        eventRequest.setCategory("Meeting");
    }

    @Test
    void createEvent_ValidRequest_ReturnsEvent() {
        when(eventService.createEvent(any(), any(), any(), any(), any())).thenReturn(testEvent);

        ResponseEntity<Event> response = eventController.createEvent(eventRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(testEvent, response.getBody());
    }

    @Test
    void getEvents_ReturnsAllEvents() {
        List<Event> events = Arrays.asList(testEvent);
        when(eventService.getEvents()).thenReturn(events);

        ResponseEntity<List<Event>> response = eventController.getEvents();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testEvent, response.getBody().get(0));
    }

    @Test
    void getEventsByCategory_ValidCategory_ReturnsEvents() {
        List<Event> events = Arrays.asList(testEvent);
        when(eventService.getEventsByCategory("Meeting")).thenReturn(events);

        ResponseEntity<List<Event>> response = eventController.getEventsByCategory("Meeting");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getUpcomingEvents_ReturnsUpcomingEvents() {
        List<Event> events = Arrays.asList(testEvent);
        when(eventService.getUpcomingEvents()).thenReturn(events);

        ResponseEntity<List<Event>> response = eventController.getUpcomingEvents();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void deleteEvent_ValidEventId_ReturnsOk() {
        String eventId = "event-123";
        doNothing().when(eventService).deleteEvent(eventId);

        ResponseEntity<Void> response = eventController.deleteEvent(eventId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(eventService).deleteEvent(eventId);
    }
}