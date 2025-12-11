package com.ieti.proyectoieti.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "events")
@Schema(description = "Event entity representing an event in the system")
public class Event {
  @Id
  @Schema(
          description = "Unique identifier of the event generated automatically",
          example = "123e4567-e89b-12d3-a456-426614174000",
          accessMode = Schema.AccessMode.READ_ONLY)
  private String id;

  @Schema(
          description = "Title of the event",
          example = "Team Meeting",
          required = true,
          maxLength = 100)
  private String title;

  @Schema(
          description = "Detailed description of the event",
          example = "Weekly team sync meeting to discuss project progress",
          maxLength = 500)
  private String description;

  @Schema(description = "Date when the event will occur", example = "2024-12-25", required = true)
  private LocalDate date;

  @Schema(
          description = "Physical or virtual location of the event",
          example = "Conference Room A",
          required = true,
          maxLength = 200)
  private String location;

  @Schema(
          description = "Category or type of the event",
          example = "Meeting",
          allowableValues = {"Meeting", "Conference", "Workshop", "Social", "General"},
          defaultValue = "General")
  private String category;

  @Schema(
          description = "Timestamp when the event was created in the system",
          example = "2024-01-15",
          accessMode = Schema.AccessMode.READ_ONLY)
  private LocalDate createdAt;

  @Schema(
          description = "List of user IDs who confirmed attendance to this event",
          accessMode = Schema.AccessMode.READ_ONLY)
  private java.util.List<String> attendeeIds;

  /**
   * Default constructor for Event entity.
   * Required for MongoDB document mapping and object deserialization.
   */
  public Event() {
    this.attendeeIds = new java.util.ArrayList<>();
  }

  public Event(String title, String description, LocalDate date, String location, String category) {
    this();
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
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Schema(description = "Event title", required = true)
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  @Schema(description = "Event description")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Schema(description = "Event date", required = true)
  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  @Schema(description = "Event location", required = true)
  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  @Schema(description = "Event category", defaultValue = "General")
  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  @Schema(description = "Creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
  public LocalDate getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDate createdAt) {
    this.createdAt = createdAt;
  }

  @Schema(description = "List of attendee user IDs")
  public java.util.List<String> getAttendeeIds() {
    return attendeeIds;
  }

  public void setAttendeeIds(java.util.List<String> attendeeIds) {
    this.attendeeIds = attendeeIds;
  }

  /**
   * Adds a user to the event's attendee list
   * @param userId The ID of the user confirming attendance
   * @return true if the user was added, false if already in the list
   */
  public boolean addAttendee(String userId) {
    if (attendeeIds == null) {
      attendeeIds = new java.util.ArrayList<>();
    }
    if (!attendeeIds.contains(userId)) {
      attendeeIds.add(userId);
      return true;
    }
    return false;
  }

  /**
   * Removes a user from the event's attendee list
   * @param userId The ID of the user canceling attendance
   * @return true if the user was removed, false if not in the list
   */
  public boolean removeAttendee(String userId) {
    if (attendeeIds != null) {
      return attendeeIds.remove(userId);
    }
    return false;
  }

  /**
   * Gets the count of confirmed attendees
   * @return number of attendees
   */
  public int getAttendeeCount() {
    return attendeeIds != null ? attendeeIds.size() : 0;
  }

  /**
   * Checks if a user is attending this event
   * @param userId The ID of the user to check
   * @return true if the user is attending
   */
  public boolean isAttending(String userId) {
    return attendeeIds != null && attendeeIds.contains(userId);
  }

  @Override
  public String toString() {
    return "Event{"
            + "id='"
            + id
            + '\''
            + ", title='"
            + title
            + '\''
            + ", date="
            + date
            + ", location='"
            + location
            + '\''
            + ", attendees="
            + getAttendeeCount()
            + '}';
  }
}