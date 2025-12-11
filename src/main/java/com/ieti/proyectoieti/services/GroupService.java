package com.ieti.proyectoieti.services;

import com.ieti.proyectoieti.models.Group;
import com.ieti.proyectoieti.repositories.GroupRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class GroupService {

    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public Group createGroup(String name, String description, String creatorId, String eventId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }
        if (creatorId == null || creatorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Creator ID cannot be empty");
        }

        if (eventId != null && groupRepository.existsByNameAndEventId(name, eventId)) {
            throw new IllegalArgumentException("Group name already exists for this event");
        }

        Group group = new Group(name, description, creatorId, eventId);

        while (groupRepository.existsByInviteCode(group.getInviteCode())) {
            group.setInviteCode(generateUniqueInviteCode());
        }

        return groupRepository.save(group);
    }

    public Optional<Group> getGroupById(String id) {
        return groupRepository.findById(id);
    }

    public Optional<Group> getGroupByInviteCode(String inviteCode) {
        return groupRepository.findByInviteCode(inviteCode);
    }

    public List<Group> getGroupsByCreator(String creatorId) {
        return groupRepository.findByCreatorId(creatorId);
    }

    public List<Group> getGroupsByEvent(String eventId) {
        return groupRepository.findByEventId(eventId);
    }

    public List<Group> getGroupsByMember(String userId) {
        return groupRepository.findByMemberIdsContaining(userId);
    }

    public Group joinGroupWithInviteCode(String inviteCode, String userId) {
        Group group = getGroupByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("Invalid invite code"));

        if (!group.hasSpace()) {
            throw new IllegalArgumentException("Group is full");
        }

        if (group.isMember(userId)) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        if (!group.addMember(userId)) {
            throw new IllegalArgumentException("Failed to add user to group");
        }

        return groupRepository.save(group);
    }

    public Group addMemberToGroup(String groupId, String userId) {
        Group group = getGroupById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        if (!group.hasSpace()) {
            throw new IllegalArgumentException("Group is full");
        }

        if (group.isMember(userId)) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        if (!group.addMember(userId)) {
            throw new IllegalArgumentException("Failed to add user to group");
        }

        return groupRepository.save(group);
    }

    public Group removeMemberFromGroup(String groupId, String userId) {
        Group group = getGroupById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        if (!group.removeMember(userId)) {
            throw new IllegalArgumentException("User is not a member of this group");
        }

        return groupRepository.save(group);
    }

    public Group updateGroup(String groupId, String name, String description, Integer maxMembers) {
        Group group = getGroupById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        if (name != null && !name.trim().isEmpty()) {
            group.setName(name);
        }

        if (description != null) {
            group.setDescription(description);
        }

        if (maxMembers != null && maxMembers > 0) {
            if (maxMembers < group.getMemberCount()) {
                throw new IllegalArgumentException("Max members cannot be less than current member count");
            }
            group.setMaxMembers(maxMembers);
        }

        return groupRepository.save(group);
    }

    public void deleteGroup(String groupId) {
        if (!groupRepository.existsById(groupId)) {
            throw new IllegalArgumentException("Group not found with ID: " + groupId);
        }
        groupRepository.deleteById(groupId);
    }

    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public String generateNewInviteCode(String groupId) {
        Group group = getGroupById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));

        String newInviteCode;
        do {
            newInviteCode = generateUniqueInviteCode();
        } while (groupRepository.existsByInviteCode(newInviteCode));

        group.setInviteCode(newInviteCode);
        groupRepository.save(group);

        return newInviteCode;
    }

    private String generateUniqueInviteCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return code.toString();
    }

    /**
     * Assigns an event to a group
     * @param groupId The ID of the group
     * @param eventId The ID of the event to assign
     * @return The updated group
     * @throws IllegalArgumentException if group not found
     */
    public Group assignEventToGroup(String groupId, String eventId) {
        Group group = getGroupById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));
        
        group.setEventId(eventId);
        return groupRepository.save(group);
    }

    /**
     * Removes the event assignment from a group
     * @param groupId The ID of the group
     * @return The updated group
     * @throws IllegalArgumentException if group not found
     */
    public Group removeEventFromGroup(String groupId) {
        Group group = getGroupById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));
        
        group.setEventId(null);
        return groupRepository.save(group);
    }
}