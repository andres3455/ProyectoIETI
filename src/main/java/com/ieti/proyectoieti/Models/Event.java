package com.ieti.proyectoieti.Models;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.UUID;

@Document(collection = "events")
@Schema(description = "Event entity representing an event in the system")
public class Event {
    @Id
    @Schema(
            description = "Unique identifier of the event generated automatically",
            example = "123e4567-e89b-12d3-a456-426614174000",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String id;

    @Schema(
            description = "Title of the event",
            example = "Team Meeting",
            required = true,
            maxLength = 100
    )
    private String title;

    @Schema(
            description = "Detailed description of the event",
            example = "Weekly team sync meeting to discuss project progress",
            maxLength = 500
    )
    private String description;

    @Schema(
            description = "Date when the event will occur",
            example = "2024-12-25",
            required = true
    )
    private LocalDate date;

    @Schema(
            description = "Physical or virtual location of the event",
            example = "Conference Room A",
            required = true,
            maxLength = 200
    )
    private String location;

    @Schema(
            description = "Category or type of the event",
            example = "Meeting",
            allowableValues = {"Meeting", "Conference", "Workshop", "Social", "General"},
            defaultValue = "General"
    )
    private String category;

    @Schema(
            description = "Timestamp when the event was created in the system",
            example = "2024-01-15",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDate createdAt;

    public Event() {
    }

    public Event(String title, String description, LocalDate date, String location, String category) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.date = date;
        this.location = location;
        this.category = category != null ? category : "General";
        this.createdAt = LocalDate.now();
    }

    // Getters and setters with schema annotations
    @Schema(description = "Unique event identifier")
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    @Schema(description = "Event title", required = true)
    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    @Schema(description = "Event description")
    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }

    @Schema(description = "Event date", required = true)
    public LocalDate getDate() { return date; }

    public void setDate(LocalDate date) { this.date = date; }

    @Schema(description = "Event location", required = true)
    public String getLocation() { return location; }

    public void setLocation(String location) { this.location = location; }

    @Schema(description = "Event category", defaultValue = "General")
    public String getCategory() { return category; }

    public void setCategory(String category) { this.category = category; }

    @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    public LocalDate getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "Event{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", date=" + date +
                ", location='" + location + '\'' +
                '}';
    }
}