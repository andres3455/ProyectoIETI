package com.ieti.proyectoieti.repositories;

import com.ieti.proyectoieti.models.Group;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {
    Optional<Group> findByInviteCode(String inviteCode);
    List<Group> findByCreatorId(String creatorId);
    List<Group> findByEventId(String eventId);
    List<Group> findByMemberIdsContaining(String userId);
    boolean existsByNameAndEventId(String name, String eventId);
    boolean existsByInviteCode(String inviteCode);
}