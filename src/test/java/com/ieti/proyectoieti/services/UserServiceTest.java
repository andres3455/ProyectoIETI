package com.ieti.proyectoieti.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ieti.proyectoieti.models.User;
import com.ieti.proyectoieti.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private final String PROVIDER_USER_ID = "google-12345";
    private final String USER_ID = "user-123";
    private final String NAME = "Test User";
    private final String EMAIL = "test@example.com";
    private final String PICTURE = "http://example.com/photo.jpg";

    @BeforeEach
    void setUp() {
        testUser = new User(PROVIDER_USER_ID, NAME, EMAIL, PICTURE);
        testUser.setId(USER_ID);
    }

    @Test
    void createOrUpdateUser_NewUser_CreatesUser() {
        when(userRepository.findByProviderUserId(PROVIDER_USER_ID)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createOrUpdateUser(PROVIDER_USER_ID, NAME, EMAIL, PICTURE);

        assertNotNull(result);
        assertEquals(NAME, result.getName());
        assertEquals(EMAIL, result.getEmail());
        assertEquals(PICTURE, result.getPicture());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createOrUpdateUser_ExistingUser_UpdatesUser() {
        User existingUser = new User(PROVIDER_USER_ID, "Old Name", "old@example.com", "old.jpg");
        existingUser.setId(USER_ID);

        when(userRepository.findByProviderUserId(PROVIDER_USER_ID)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createOrUpdateUser(PROVIDER_USER_ID, NAME, EMAIL, PICTURE);

        assertNotNull(result);
        assertEquals(NAME, result.getName());
        assertEquals(EMAIL, result.getEmail());
        assertEquals(PICTURE, result.getPicture());
        verify(userRepository).save(existingUser);
    }

    @Test
    void getUserById_ExistingUser_ReturnsUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.getUserById(USER_ID);

        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
    }

    @Test
    void getUserById_NonExistingUser_ReturnsEmpty() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(USER_ID);

        assertFalse(result.isPresent());
    }

    @Test
    void addUserToGroup_ValidUser_AddsGroup() {
        String groupId = "group-123";
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.addUserToGroup(USER_ID, groupId);

        assertTrue(result.getGroupIds().contains(groupId));
        verify(userRepository).save(testUser);
    }

    @Test
    void addUserToGroup_NonExistingUser_ThrowsException() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.addUserToGroup(USER_ID, "group-123"));
    }

    @Test
    void removeUserFromGroup_ValidUser_RemovesGroup() {
        String groupId = "group-123";
        testUser.addGroup(groupId);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.removeUserFromGroup(USER_ID, groupId);

        assertFalse(result.getGroupIds().contains(groupId));
        verify(userRepository).save(testUser);
    }

    @Test
    void getUsersByGroup_ValidGroup_ReturnsUsers() {
        String groupId = "group-123";
        List<User> users = List.of(testUser);
        when(userRepository.findByGroupIdsContaining(groupId)).thenReturn(users);

        List<User> result = userService.getUsersByGroup(groupId);

        assertEquals(1, result.size());
        assertEquals(testUser, result.get(0));
    }

    @Test
    void deleteUser_ExistingUser_DeletesUser() {
        when(userRepository.existsById(USER_ID)).thenReturn(true);
        doNothing().when(userRepository).deleteById(USER_ID);

        assertDoesNotThrow(() -> userService.deleteUser(USER_ID));
        verify(userRepository).deleteById(USER_ID);
    }

    @Test
    void deleteUser_NonExistingUser_ThrowsException() {
        when(userRepository.existsById(USER_ID)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(USER_ID));
    }
}