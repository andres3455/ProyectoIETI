package com.ieti.proyectoieti.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@Tag(name = "Authentication", description = "Authentication and user profile APIs")
public class AuthController {

    @Operation(
            summary = "Get user profile",
            description = "Retrieves the authenticated user's profile information"
    )
    @ApiResponse(responseCode = "200", description = "User profile retrieved successfully")
    @GetMapping("/api/user/profile")
    public Map<String, Object> getUserProfile(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return Collections.singletonMap("error", "User not authenticated");
        }

        return Map.of(
                "name", principal.getAttribute("name"),
                "email", principal.getAttribute("email"),
                "picture", principal.getAttribute("picture"),
                "sub", principal.getAttribute("sub")
        );
    }

    @Operation(
            summary = "Check authentication status",
            description = "Returns whether the user is authenticated"
    )
    @ApiResponse(responseCode = "200", description = "Authentication status retrieved")
    @GetMapping("/api/auth/status")
    public Map<String, Boolean> getAuthStatus(@AuthenticationPrincipal OAuth2User principal) {
        return Collections.singletonMap("authenticated", principal != null);
    }
}