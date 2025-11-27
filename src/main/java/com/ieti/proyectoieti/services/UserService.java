package com.ieti.proyectoieti.services;

import com.ieti.proyectoieti.models.User;
import com.ieti.proyectoieti.repositories.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createOrUpdateUser(String providerUserId, String name, String email, String picture) {
        Optional<User> existingUser = userRepository.findByProviderUserId(providerUserId);

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setName(name);
            user.setEmail(email);
            user.setPicture(picture);
            return userRepository.save(user);
        } else {
            User newUser = new User(providerUserId, name, email, picture);
            return userRepository.save(newUser);
        }
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByProviderUserId(String providerUserId) {
        return userRepository.findByProviderUserId(providerUserId);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getUsersByGroup(String groupId) {
        return userRepository.findByGroupIdsContaining(groupId);
    }

    public User addUserToGroup(String userId, String groupId) {
        User user = getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        user.addGroup(groupId);
        return userRepository.save(user);
    }

    public User removeUserFromGroup(String userId, String groupId) {
        User user = getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

        user.removeGroup(groupId);
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
    }

    public boolean userExists(String providerUserId) {
        return userRepository.existsByProviderUserId(providerUserId);
    }
}