package com.ieti.proyectoieti.config;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;

  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(
                    session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(
                    authz ->
                            authz
                                    // Public endpoints - no authentication required
                                    .requestMatchers(
                                            "/",
                                            "/health",
                                            "/login",
                                            "/oauth2/**",
                                            "/error",
                                            "/swagger-ui/**",
                                            "/swagger-ui.html",
                                            "/v3/api-docs/**",
                                            "/api-docs/**",
                                            "/webjars/**",
                                            "/swagger-resources/**",
                                            "/configuration/**",
                                            "/api/auth/status",
                                            "/api/auth/verify",
                                            "/api/auth/refresh")
                                    .permitAll()
                                    // Read-only endpoints - can be accessed without auth for browsing
                                    .requestMatchers(
                                            "GET",
                                            "/api/groups",
                                            "/api/groups/{groupId}",
                                            "/api/groups/invite/{inviteCode}",
                                            "/api/groups/event/{eventId}",
                                            "/api/events",
                                            "/api/events/{eventId}")
                                    .permitAll()
                                    // All other requests require authentication
                                    .anyRequest()
                                    .authenticated())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .oauth2Login(
                    oauth2 -> oauth2.loginPage("/login").successHandler(authenticationSuccessHandler()))
            .logout(
                    logout ->
                            logout
                                    .logoutSuccessUrl("/")
                                    .invalidateHttpSession(true)
                                    .deleteCookies("JSESSIONID")
                                    .permitAll())
            // Configure exception handling for authentication failures
            .exceptionHandling(
                    exceptions ->
                            exceptions
                                    .authenticationEntryPoint(
                                            (request, response, authException) -> {
                                              response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                              response.setContentType("application/json");
                                              response
                                                  .getWriter()
                                                  .write(
                                                      "{\"error\":\"Unauthorized\",\"message\":\"Authentication required\"}");
                                            }));

    return http.build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:7357",
            "http://localhost:8080",
            "https://*.github.io"
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public AuthenticationSuccessHandler authenticationSuccessHandler() {
    return (request, response, authentication) -> {
      response.sendRedirect("/api/auth/status");
    };
  }
}