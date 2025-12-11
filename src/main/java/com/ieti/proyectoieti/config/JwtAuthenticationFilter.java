package com.ieti.proyectoieti.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.ieti.proyectoieti.models.User;
import com.ieti.proyectoieti.services.GoogleTokenVerifier;
import com.ieti.proyectoieti.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
  private final GoogleTokenVerifier googleTokenVerifier;
  private final UserService userService;

  public JwtAuthenticationFilter(GoogleTokenVerifier googleTokenVerifier, UserService userService) {
    this.googleTokenVerifier = googleTokenVerifier;
    this.userService = userService;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");
    String requestUri = request.getRequestURI();

    logger.debug("Processing request to: {} with auth header: {}", 
        requestUri, authHeader != null ? "Present" : "Missing");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      logger.debug("Extracted JWT token of length: {}", token.length());

      try {
        // Verify the Google ID token
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(token);

        if (payload != null) {
          // Get Google Provider ID and email
          String providerUserId = payload.getSubject();
          String email = payload.getEmail();
          String name = (String) payload.get("name");

          logger.info("Token verified for Google user: {} ({})", email, providerUserId);

          // Find the MongoDB user by Provider ID
          Optional<User> userOptional = userService.getUserByProviderUserId(providerUserId);
          
          if (userOptional.isPresent()) {
            User user = userOptional.get();
            String mongoUserId = user.getId();

            logger.info("Successfully authenticated user: {} - MongoDB ID: {}, Provider ID: {}", 
                email, mongoUserId, providerUserId);

            // Create a simple authentication token with MongoDB user ID
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    mongoUserId, // Principal (MongoDB user ID, not Provider ID)
                    null, // Credentials (not needed after verification)
                    Collections.emptyList() // Authorities (roles)
                    );

            // Set additional details
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Store MONGODB user ID in request attributes for controller access
            request.setAttribute("googlePayload", payload);
            request.setAttribute("authenticatedUserId", mongoUserId); // ← MongoDB ID
            request.setAttribute("authenticatedUserEmail", email);
            request.setAttribute("providerUserId", providerUserId); // Also store Provider ID if needed
          } else {
            logger.warn("User not found in database for Provider ID: {}", providerUserId);
            logger.warn("User must call /api/auth/verify first to create account");
          }
        } else {
          logger.warn("Token verification returned null payload");
        }
      } catch (Exception e) {
        // Token verification failed - log and continue without authentication
        logger.warn("Failed to verify Google ID token for {}: {} - {}", 
            requestUri, e.getClass().getSimpleName(), e.getMessage());
        // Set error attribute that can be checked by controllers
        request.setAttribute("authError", e.getMessage());
      }
    } else {
      logger.debug("No Bearer token found for request to: {}", requestUri);
    }

    filterChain.doFilter(request, response);
  }
}
