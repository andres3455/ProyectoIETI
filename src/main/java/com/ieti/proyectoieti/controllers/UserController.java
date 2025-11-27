package com.ieti.proyectoieti.controllers;

import com.ieti.proyectoieti.models.User;
import com.ieti.proyectoieti.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get current user profile", description = "Retrieves the authenticated user's profile")
    @ApiResponse(responseCode = "200", description = "User profile retrieved successfully")
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@RequestParam String providerUserId) {
        User user = userService.getUserByProviderUserId(providerUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get users by group", description = "Retrieves all users belonging to a specific group")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<User>> getUsersByGroup(@PathVariable String groupId) {
        List<User> users = userService.getUsersByGroup(groupId);
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get all users", description = "Retrieves all users in the system")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Add user to group", description = "Adds a user to a specific group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User added to group successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "User or group not found")
    })
    @PostMapping("/{userId}/groups/{groupId}")
    public ResponseEntity<User> addUserToGroup(@PathVariable String userId, @PathVariable String groupId) {
        User user = userService.addUserToGroup(userId, groupId);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Remove user from group", description = "Removes a user from a specific group")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User removed from group successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "404", description = "User or group not found")
    })
    @DeleteMapping("/{userId}/groups/{groupId}")
    public ResponseEntity<User> removeUserFromGroup(@PathVariable String userId, @PathVariable String groupId) {
        User user = userService.removeUserFromGroup(userId, groupId);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Delete user", description = "Deletes a user from the system")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{userId}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }
}