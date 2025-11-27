package com.ieti.proyectoieti.controllers;

import com.ieti.proyectoieti.controllers.dto.GroupRequest;
import com.ieti.proyectoieti.models.Group;
import com.ieti.proyectoieti.services.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
@Tag(name = "Groups", description = "Group management APIs")
public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @Operation(summary = "Create a new group", description = "Creates a new group for event participation")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    })
    @PostMapping
    public ResponseEntity<Group> createGroup(@Valid @RequestBody GroupRequest groupRequest) {
        Group group = groupService.createGroup(
                groupRequest.getName(),
                groupRequest.getDescription(),
                groupRequest.getCreatorId(),
                groupRequest.getEventId()
        );
        return ResponseEntity.ok(group);
    }

    @Operation(summary = "Get group by ID", description = "Retrieves a group by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/{groupId}")
    public ResponseEntity<Group> getGroupById(@PathVariable String groupId) {
        Group group = groupService.getGroupById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));
        return ResponseEntity.ok(group);
    }

    @Operation(summary = "Get group by invite code", description = "Retrieves a group by its invite code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @GetMapping("/invite/{inviteCode}")
    public ResponseEntity<Group> getGroupByInviteCode(@PathVariable String inviteCode) {
        Group group = groupService.getGroupByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with invite code: " + inviteCode));
        return ResponseEntity.ok(group);
    }

    @Operation(summary = "Join group with invite code", description = "Allows a user to join a group using an invite code")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User joined group successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid invite code or group is full"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/join/{inviteCode}")
    public ResponseEntity<Group> joinGroupWithInviteCode(
            @PathVariable String inviteCode,
            @RequestBody Map<String, String> request) {
        String userId = request.get("userId");
        Group group = groupService.joinGroupWithInviteCode(inviteCode, userId);
        return ResponseEntity.ok(group);
    }

    @Operation(summary = "Get groups by creator", description = "Retrieves all groups created by a specific user")
    @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<List<Group>> getGroupsByCreator(@PathVariable String creatorId) {
        List<Group> groups = groupService.getGroupsByCreator(creatorId);
        return ResponseEntity.ok(groups);
    }

    @Operation(summary = "Get groups by event", description = "Retrieves all groups for a specific event")
    @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Group>> getGroupsByEvent(@PathVariable String eventId) {
        List<Group> groups = groupService.getGroupsByEvent(eventId);
        return ResponseEntity.ok(groups);
    }

    @Operation(summary = "Get groups by member", description = "Retrieves all groups where a user is a member")
    @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    @GetMapping("/member/{userId}")
    public ResponseEntity<List<Group>> getGroupsByMember(@PathVariable String userId) {
        List<Group> groups = groupService.getGroupsByMember(userId);
        return ResponseEntity.ok(groups);
    }

    @Operation(summary = "Add member to group", description = "Adds a user as a member to an existing group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member added successfully"),
            @ApiResponse(responseCode = "400", description = "Group is full or user is already a member"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Group> addMemberToGroup(@PathVariable String groupId, @PathVariable String userId) {
        Group group = groupService.addMemberToGroup(groupId, userId);
        return ResponseEntity.ok(group);
    }

    @Operation(summary = "Remove member from group", description = "Removes a user from a group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Member removed successfully"),
            @ApiResponse(responseCode = "400", description = "User is not a member of the group"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Group> removeMemberFromGroup(@PathVariable String groupId, @PathVariable String userId) {
        Group group = groupService.removeMemberFromGroup(groupId, userId);
        return ResponseEntity.ok(group);
    }

    @Operation(summary = "Update group", description = "Updates group information")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PutMapping("/{groupId}")
    public ResponseEntity<Group> updateGroup(
            @PathVariable String groupId,
            @Valid @RequestBody GroupRequest groupRequest) {
        Group group = groupService.updateGroup(
                groupId,
                groupRequest.getName(),
                groupRequest.getDescription(),
                groupRequest.getMaxMembers()
        );
        return ResponseEntity.ok(group);
    }

    @Operation(summary = "Generate new invite code", description = "Generates a new invite code for a group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New invite code generated successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @PostMapping("/{groupId}/regenerate-invite")
    public ResponseEntity<Map<String, String>> generateNewInviteCode(@PathVariable String groupId) {
        String newInviteCode = groupService.generateNewInviteCode(groupId);
        return ResponseEntity.ok(Map.of("inviteCode", newInviteCode));
    }

    @Operation(summary = "Delete group", description = "Deletes a group from the system")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Group deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Group not found")
    })
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Map<String, String>> deleteGroup(@PathVariable String groupId) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.ok(Map.of("message", "Group deleted successfully"));
    }

    @Operation(summary = "Get all groups", description = "Retrieves all groups in the system")
    @ApiResponse(responseCode = "200", description = "Groups retrieved successfully")
    @GetMapping
    public ResponseEntity<List<Group>> getAllGroups() {
        List<Group> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }
}