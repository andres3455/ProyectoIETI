package com.ieti.proyectoieti.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.ieti.proyectoieti.services.GoogleTokenVerifier;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final GoogleTokenVerifier googleTokenVerifier;

  public JwtAuthenticationFilter(GoogleTokenVerifier googleTokenVerifier) {
    this.googleTokenVerifier = googleTokenVerifier;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);

      try {
        // Verify the Google ID token
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(token);

        if (payload != null) {
          // Create authentication object with user information
          String userId = payload.getSubject();
          String email = payload.getEmail();

          // Create a simple authentication token
          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(
                  userId, // Principal (user ID)
                  null, // Credentials (not needed after verification)
                  Collections.emptyList() // Authorities (roles)
                  );

          // Set additional details
          authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          // Set authentication in security context
          SecurityContextHolder.getContext().setAuthentication(authentication);

          // Store the payload in request attribute for controller access
          request.setAttribute("googlePayload", payload);
        }
      } catch (Exception e) {
        // Token verification failed - log and continue without authentication
        logger.warn("Failed to verify Google ID token: " + e.getMessage());
      }
    }

    filterChain.doFilter(request, response);
  }
}
