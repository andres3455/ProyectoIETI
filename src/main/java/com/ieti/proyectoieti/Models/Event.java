package com.ieti.proyectoieti.Models;
import java.time.LocalDate;
import java.util.UUID;

public class Event {
    private String id;
    private String title;
    private String description;
    private LocalDate date;
    private String location;
    private String category;
    private LocalDate createdAt;

    public Event(String title, String description, LocalDate date, String location, String category) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.category = category != null ? category : "General";
        this.createdAt = LocalDate.now();
    }

    // Getters y setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDate() { return date; }
    public String getLocation() { return location; }
    public String getCategory() { return category; }
    public LocalDate getCreatedAt() { return createdAt; }
}
