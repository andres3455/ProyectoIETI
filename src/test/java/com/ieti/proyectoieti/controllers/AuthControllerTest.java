package com.ieti.proyectoieti.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ieti.proyectoieti.config.SecurityConfig;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  private OAuth2User createOAuth2User() {
    Map<String, Object> attributes = new HashMap<>();
    attributes.put("sub", "12345");
    attributes.put("name", "Test User");
    attributes.put("email", "test@example.com");
    attributes.put("picture", "http://example.com/pic.jpg");

    List<SimpleGrantedAuthority> authorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

    return new DefaultOAuth2User(authorities, attributes, "sub");
  }

  @Test
  void getUserProfile_AuthenticatedUser_ReturnsUserProfile() throws Exception {
    OAuth2User oauth2User = createOAuth2User();

    mockMvc
        .perform(get("/api/user/profile").with(oauth2Login().oauth2User(oauth2User)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Test User"))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.picture").value("http://example.com/pic.jpg"))
        .andExpect(jsonPath("$.sub").value("12345"));
  }

  @Test
  void getUserProfile_UnauthenticatedUser_ReturnsError() throws Exception {
    mockMvc
        .perform(get("/api/user/profile"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  void getAuthStatus_AuthenticatedUser_ReturnsTrue() throws Exception {
    OAuth2User oauth2User = createOAuth2User();

    mockMvc
        .perform(get("/api/auth/status").with(oauth2Login().oauth2User(oauth2User)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authenticated").value(true));
  }

  @Test
  void getAuthStatus_UnauthenticatedUser_ReturnsFalse() throws Exception {
    mockMvc
        .perform(get("/api/auth/status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.authenticated").value(false));
  }
}
