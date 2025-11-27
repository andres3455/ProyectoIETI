package com.ieti.proyectoieti.repositories;

import com.ieti.proyectoieti.models.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByProviderUserId(String providerUserId);
    Optional<User> findByEmail(String email);
    List<User> findByGroupIdsContaining(String groupId);
    boolean existsByEmail(String email);
    boolean existsByProviderUserId(String providerUserId);
}