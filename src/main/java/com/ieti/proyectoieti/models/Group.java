package com.ieti.proyectoieti.models;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "groups")
@Schema(description = "Group entity for event participation")
public class Group {
    @Id
    @Schema(description = "Unique identifier of the group", example = "group-123", accessMode = Schema.AccessMode.READ_ONLY)
    private String id;

    @Schema(description = "Name of the group", example = "Team A", required = true)
    private String name;

    @Schema(description = "Description of the group", example = "Group for team A event participation")
    private String description;

    @Schema(description = "ID of the user who created the group", example = "user-123", required = true)
    private String creatorId;

    @Schema(description = "Event ID associated with this group", example = "event-456")
    private String eventId;

    @Schema(description = "Unique alphanumeric code for inviting members", example = "ABC123", accessMode = Schema.AccessMode.READ_ONLY)
    private String inviteCode;

    @Schema(description = "List of user IDs who are members of this group", accessMode = Schema.AccessMode.READ_ONLY)
    private List<String> memberIds;

    @Schema(description = "Maximum number of members allowed in the group", example = "10", defaultValue = "50")
    private int maxMembers;

    @Schema(description = "Timestamp when the group was created", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the group was last updated", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime updatedAt;

    public Group() {
        this.memberIds = new ArrayList<>();
        this.maxMembers = 50;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Group(String name, String description, String creatorId, String eventId) {
        this();
        this.name = name;
        this.description = description;
        this.creatorId = creatorId;
        this.eventId = eventId;
        this.inviteCode = generateInviteCode();

        if (this.memberIds == null) {
            this.memberIds = new ArrayList<>();
        }

        this.memberIds.add(creatorId);
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
        this.updatedAt = LocalDateTime.now();
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
        this.updatedAt = LocalDateTime.now();
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public List<String> getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(List<String> memberIds) {
        this.memberIds = memberIds;
        this.updatedAt = LocalDateTime.now();
    }

    public int getMaxMembers() {
        return maxMembers;
    }

    public void setMaxMembers(int maxMembers) {
        this.maxMembers = maxMembers;
        this.updatedAt = LocalDateTime.now();
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

    public boolean addMember(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        if (this.memberIds == null) {
            this.memberIds = new ArrayList<>();
        }
        if (this.memberIds.size() >= this.maxMembers) {
            return false;
        }
        if (!this.memberIds.contains(userId)) {
            this.memberIds.add(userId);
            this.updatedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }
    public boolean removeMember(String userId) {
        boolean removed = memberIds.remove(userId);
        if (removed) {
            this.updatedAt = LocalDateTime.now();
        }
        return removed;
    }

    public boolean isMember(String userId) {
        return memberIds.contains(userId);
    }

    public int getMemberCount() {
        return memberIds != null ? memberIds.size() : 0;
    }

    public boolean hasSpace() {
        return memberIds.size() < maxMembers;
    }

    private String generateInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return code.toString();
    }
}