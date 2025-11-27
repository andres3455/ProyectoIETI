package com.ieti.proyectoieti.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ieti.proyectoieti.config.SecurityConfig;
import com.ieti.proyectoieti.models.User;
import com.ieti.proyectoieti.services.UserService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private User testUser;
    private final String USER_ID = "user-123";
    private final String PROVIDER_USER_ID = "google-12345";

    @BeforeEach
    void setUp() {
        testUser = new User(PROVIDER_USER_ID, "Test User", "test@example.com", "http://example.com/photo.jpg");
        testUser.setId(USER_ID);
    }

    @Test
    @WithMockUser
    void getCurrentUser_ValidProviderUserId_ReturnsUser() throws Exception {
        when(userService.getUserByProviderUserId(PROVIDER_USER_ID)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/me")
                        .param("providerUserId", PROVIDER_USER_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    void getCurrentUser_InvalidProviderUserId_ReturnsNotFound() throws Exception {
        when(userService.getUserByProviderUserId(PROVIDER_USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/me")
                        .param("providerUserId", PROVIDER_USER_ID)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getUserById_ValidId_ReturnsUser() throws Exception {
        when(userService.getUserById(USER_ID)).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/{userId}", USER_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID));
    }

    @Test
    @WithMockUser
    void getUserById_InvalidId_ReturnsNotFound() throws Exception {
        when(userService.getUserById(USER_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{userId}", USER_ID)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getUsersByGroup_ValidGroup_ReturnsUsers() throws Exception {
        String groupId = "group-123";
        List<User> users = List.of(testUser);
        when(userService.getUsersByGroup(groupId)).thenReturn(users);

        mockMvc.perform(get("/api/users/group/{groupId}", groupId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(USER_ID));
    }

    @Test
    @WithMockUser
    void addUserToGroup_ValidRequest_ReturnsUser() throws Exception {
        String groupId = "group-123";
        when(userService.addUserToGroup(USER_ID, groupId)).thenReturn(testUser);

        mockMvc.perform(post("/api/users/{userId}/groups/{groupId}", USER_ID, groupId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID));
    }

    @Test
    @WithMockUser
    void removeUserFromGroup_ValidRequest_ReturnsUser() throws Exception {
        String groupId = "group-123";
        when(userService.removeUserFromGroup(USER_ID, groupId)).thenReturn(testUser);

        mockMvc.perform(delete("/api/users/{userId}/groups/{groupId}", USER_ID, groupId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID));
    }

    @Test
    @WithMockUser
    void deleteUser_ValidId_ReturnsSuccess() throws Exception {
        doNothing().when(userService).deleteUser(USER_ID);

        mockMvc.perform(delete("/api/users/{userId}", USER_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deleted successfully"));
    }

    @Test
    @WithMockUser
    void getAllUsers_ReturnsUsers() throws Exception {
        List<User> users = List.of(testUser);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(USER_ID));
    }
}