package com.ieti.proyectoieti.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
@Schema(description = "User entity representing a user in the system")
public class User {
    @Id
    @Schema(description = "Unique identifier of the user", example = "user-123", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Schema(description = "OAuth2 provider user ID (e.g., Google sub)", example = "google-12345", required = true)
    private String providerUserId;

    @Schema(description = "User's full name", example = "John Doe", required = true)
    private String name;

    @Schema(description = "User's email address", example = "john@example.com", required = true)
    private String email;

    @Schema(description = "User's profile picture URL", example = "https://example.com/photo.jpg")
    private String picture;

    @Schema(description = "List of group IDs the user belongs to", accessMode = Schema.AccessMode.READ_ONLY)
    private List<String> groupIds;

    @Schema(description = "Timestamp when the user was created", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the user was last updated", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    public User() {
        this.groupIds = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public User(String providerUserId, String name, String email, String picture) {
        this();
        this.providerUserId = providerUserId;
        this.name = name;
        this.email = email;
        this.picture = picture;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public List<String> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<String> groupIds) {
        this.groupIds = groupIds;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void addGroup(String groupId) {
        if (!this.groupIds.contains(groupId)) {
            this.groupIds.add(groupId);
            this.updatedAt = LocalDateTime.now();
        }
    }

    public void removeGroup(String groupId) {
        this.groupIds.remove(groupId);
        this.updatedAt = LocalDateTime.now();
    }
}