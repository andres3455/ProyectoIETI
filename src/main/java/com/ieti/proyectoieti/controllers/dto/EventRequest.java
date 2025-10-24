package com.ieti.proyectoieti.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class EventRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    @Schema(description = "Title of the event", example = "Team Meeting", required = true)
    private String title;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Schema(description = "Detailed description of the event", example = "Weekly team sync meeting")
    private String description;

    @NotNull(message = "Date is required")
    @FutureOrPresent(message = "Event date must be in the present or future")
    @Schema(description = "Date when the event will occur", example = "2024-12-25", required = true)
    private LocalDate date;

    @NotBlank(message = "Location is required")
    @Size(max = 200, message = "Location must not exceed 200 characters")
    @Schema(description = "Physical or virtual location", example = "Conference Room A", required = true)
    private String location;

    @Size(max = 50, message = "Category must not exceed 50 characters")
    @Schema(description = "Category or type of the event", example = "Meeting", defaultValue = "General")
    private String category = "General";

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}