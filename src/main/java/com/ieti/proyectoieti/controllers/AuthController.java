package com.ieti.proyectoieti.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.ieti.proyectoieti.controllers.dto.TokenRequest;
import com.ieti.proyectoieti.models.User;
import com.ieti.proyectoieti.services.GoogleTokenVerifier;
import com.ieti.proyectoieti.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Authentication", description = "Authentication and user profile APIs")
public class AuthController {

  private final UserService userService;
  private final GoogleTokenVerifier googleTokenVerifier;

  public AuthController(UserService userService, GoogleTokenVerifier googleTokenVerifier) {
    this.userService = userService;
    this.googleTokenVerifier = googleTokenVerifier;
  }

  @Operation(
      summary = "Verify Google ID token",
      description =
          "Verifies a Google ID token from Flutter and returns user profile. Creates user if doesn't exist.")
  @ApiResponse(responseCode = "200", description = "Token verified and user profile returned")
  @ApiResponse(responseCode = "401", description = "Invalid token")
  @PostMapping("/api/auth/verify")
  public ResponseEntity<?> verifyToken(@RequestBody TokenRequest tokenRequest) {
    try {
      // Verify the Google ID token
      GoogleIdToken.Payload payload = googleTokenVerifier.verify(tokenRequest.getIdToken());

      if (payload == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "Invalid token"));
      }

      // Extract user information from token
      String providerUserId = payload.getSubject();
      String name = (String) payload.get("name");
      String email = payload.getEmail();
      String picture = (String) payload.get("picture");

      // Create or update user in database
      User user = userService.createOrUpdateUser(providerUserId, name, email, picture);

      // Return user profile with token
      return ResponseEntity.ok(
          Map.of(
              "authenticated",
              true,
              "token",
              tokenRequest.getIdToken(), // Return the same token for storage
              "user",
              Map.of(
                  "id", user.getId(),
                  "name", user.getName(),
                  "email", user.getEmail(),
                  "picture", user.getPicture(),
                  "providerUserId", user.getProviderUserId(),
                  "groupIds", user.getGroupIds(),
                  "createdAt", user.getCreatedAt())));

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Token verification failed: " + e.getMessage()));
    }
  }

  @Operation(
      summary = "Get user profile",
      description = "Retrieves the authenticated user's profile information")
  @ApiResponse(responseCode = "200", description = "User profile retrieved successfully")
  @GetMapping("/api/user/profile")
  public ResponseEntity<?> getUserProfile(HttpServletRequest request) {
    // Try to get payload from request attribute (set by JwtAuthenticationFilter)
    GoogleIdToken.Payload payload = (GoogleIdToken.Payload) request.getAttribute("googlePayload");

    if (payload != null) {
      // JWT authentication
      String providerUserId = payload.getSubject();
      String name = (String) payload.get("name");
      String email = payload.getEmail();
      String picture = (String) payload.get("picture");

      User user = userService.createOrUpdateUser(providerUserId, name, email, picture);

      return ResponseEntity.ok(
          Map.of(
              "id", user.getId(),
              "name", user.getName(),
              "email", user.getEmail(),
              "picture", user.getPicture(),
              "providerUserId", user.getProviderUserId(),
              "groupIds", user.getGroupIds(),
              "createdAt", user.getCreatedAt()));
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("error", "User not authenticated"));
  }

  @Operation(
      summary = "Get user profile (OAuth2)",
      description = "Retrieves the authenticated user's profile information using OAuth2")
  @ApiResponse(responseCode = "200", description = "User profile retrieved successfully")
  @GetMapping("/api/user/profile/oauth2")
  public Map<String, Object> getUserProfileOAuth2(@AuthenticationPrincipal OAuth2User principal) {
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
        "createdAt", user.getCreatedAt());
  }

  @Operation(
      summary = "Check authentication status",
      description = "Returns whether the user is authenticated")
  @ApiResponse(responseCode = "200", description = "Authentication status retrieved")
  @GetMapping("/api/auth/status")
  public Map<String, Boolean> getAuthStatus(
      @AuthenticationPrincipal OAuth2User principal, HttpServletRequest request) {
    // Check both JWT and OAuth2 authentication
    GoogleIdToken.Payload payload = (GoogleIdToken.Payload) request.getAttribute("googlePayload");
    boolean isAuthenticated = principal != null || payload != null;
    return Collections.singletonMap("authenticated", isAuthenticated);
  }

  @Operation(
      summary = "Refresh authentication token",
      description =
          "Validates the current token and returns refreshed user information. Google ID tokens are long-lived, but this endpoint can be used to verify the token is still valid.")
  @ApiResponse(responseCode = "200", description = "Token is valid, user info returned")
  @ApiResponse(responseCode = "401", description = "Token is invalid or expired")
  @PostMapping("/api/auth/refresh")
  public ResponseEntity<?> refreshToken(@RequestBody TokenRequest tokenRequest) {
    try {
      // Verify the Google ID token
      GoogleIdToken.Payload payload = googleTokenVerifier.verify(tokenRequest.getIdToken());

      if (payload == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("error", "Token is invalid or expired", "needsReauth", true));
      }

      // Extract user information from token
      String providerUserId = payload.getSubject();
      String name = (String) payload.get("name");
      String email = payload.getEmail();
      String picture = (String) payload.get("picture");

      // Get or update user from database
      User user = userService.createOrUpdateUser(providerUserId, name, email, picture);

      // Return user profile and token validity info
      return ResponseEntity.ok(
          Map.of(
              "valid",
              true,
              "user",
              Map.of(
                  "id", user.getId(),
                  "name", user.getName(),
                  "email", user.getEmail(),
                  "picture", user.getPicture(),
                  "providerUserId", user.getProviderUserId(),
                  "groupIds", user.getGroupIds(),
                  "createdAt", user.getCreatedAt()),
              "tokenInfo",
              Map.of(
                  "expiresAt", payload.getExpirationTimeSeconds(),
                  "issuedAt", payload.getIssuedAtTimeSeconds())));

    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(
              Map.of(
                  "error", "Token validation failed: " + e.getMessage(),
                  "needsReauth", true));
    }
  }
}