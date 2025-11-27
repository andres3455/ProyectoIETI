package com.ieti.proyectoieti.controllers;

import com.ieti.proyectoieti.models.User;
import com.ieti.proyectoieti.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Authentication", description = "Authentication and user profile APIs")
public class AuthController {

  private final UserService userService;

  public AuthController(UserService userService) {
    this.userService = userService;
  }

  @Operation(
          summary = "Get user profile",
          description = "Retrieves the authenticated user's profile information")
  @ApiResponse(responseCode = "200", description = "User profile retrieved successfully")
  @GetMapping("/api/user/profile")
  public Map<String, Object> getUserProfile(@AuthenticationPrincipal OAuth2User principal) {
    if (principal == null) {
      return Collections.singletonMap("error", "User not authenticated");
    }

    // Create or update user in our database
    String providerUserId = principal.getAttribute("sub");
    String name = principal.getAttribute("name");
    String email = principal.getAttribute("email");
    String picture = principal.getAttribute("picture");

    User user = userService.createOrUpdateUser(providerUserId, name, email, picture);

    return Map.of(
            "id", user.getId(),
            "name", user.getName(),
            "email", user.getEmail(),
            "picture", user.getPicture(),
            "providerUserId", user.getProviderUserId(),
            "groupIds", user.getGroupIds(),
            "createdAt", user.getCreatedAt()
    );
  }

  @Operation(
          summary = "Check authentication status",
          description = "Returns whether the user is authenticated")
  @ApiResponse(responseCode = "200", description = "Authentication status retrieved")
  @GetMapping("/api/auth/status")
  public Map<String, Boolean> getAuthStatus(@AuthenticationPrincipal OAuth2User principal) {
    return Collections.singletonMap("authenticated", principal != null);
  }
}