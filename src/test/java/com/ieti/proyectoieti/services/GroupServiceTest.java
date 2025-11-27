package com.ieti.proyectoieti.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ieti.proyectoieti.models.Group;
import com.ieti.proyectoieti.repositories.GroupRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private GroupService groupService;

    private Group testGroup;
    private final String GROUP_ID = "group-123";
    private final String CREATOR_ID = "user-123";
    private final String NAME = "Test Group";
    private final String DESCRIPTION = "Test Description";
    private final String EVENT_ID = "event-456";
    private final String INVITE_CODE = "ABC123";

    @BeforeEach
    void setUp() {
        testGroup = new Group(NAME, DESCRIPTION, CREATOR_ID, EVENT_ID);
        testGroup.setId(GROUP_ID);
        testGroup.setInviteCode(INVITE_CODE);
    }

    @Test
    void createGroup_ValidParameters_CreatesGroup() {
        when(groupRepository.existsByNameAndEventId(NAME, EVENT_ID)).thenReturn(false);
        when(groupRepository.existsByInviteCode(any())).thenReturn(false);

        Group savedGroup = new Group(NAME, DESCRIPTION, CREATOR_ID, EVENT_ID);
        savedGroup.setId(GROUP_ID);
        when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);

        Group result = groupService.createGroup(NAME, DESCRIPTION, CREATOR_ID, EVENT_ID);

        assertNotNull(result);
        assertEquals(NAME, result.getName());
        assertEquals(DESCRIPTION, result.getDescription());
        assertEquals(CREATOR_ID, result.getCreatorId());
        assertEquals(EVENT_ID, result.getEventId());

        assertTrue(result.isMember(CREATOR_ID));

        verify(groupRepository).save(any(Group.class));
    }
    @Test
    void createGroup_DuplicateName_ThrowsException() {
        when(groupRepository.existsByNameAndEventId(NAME, EVENT_ID)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> groupService.createGroup(NAME, DESCRIPTION, CREATOR_ID, EVENT_ID));
    }

    @Test
    void joinGroupWithInviteCode_ValidCode_AddsMember() {
        String userId = "user-456";
        when(groupRepository.findByInviteCode(INVITE_CODE)).thenReturn(Optional.of(testGroup));
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        Group result = groupService.joinGroupWithInviteCode(INVITE_CODE, userId);

        assertTrue(result.isMember(userId));
        verify(groupRepository).save(testGroup);
    }

    @Test
    void joinGroupWithInviteCode_InvalidCode_ThrowsException() {
        when(groupRepository.findByInviteCode(INVITE_CODE)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> groupService.joinGroupWithInviteCode(INVITE_CODE, "user-456"));
    }

    @Test
    void joinGroupWithInviteCode_FullGroup_ThrowsException() {
        String userId = "user-456";

        Group fullGroup = new Group(NAME, DESCRIPTION, CREATOR_ID, EVENT_ID);
        fullGroup.setId(GROUP_ID);
        fullGroup.setInviteCode(INVITE_CODE);
        fullGroup.setMaxMembers(1);

        if (!fullGroup.isMember(CREATOR_ID)) {
            fullGroup.addMember(CREATOR_ID);
        }

        assertEquals(1, fullGroup.getMemberCount(), "Group should have 1 member (the creator)");
        assertFalse(fullGroup.hasSpace(), "Group should be full");

        when(groupRepository.findByInviteCode(INVITE_CODE)).thenReturn(Optional.of(fullGroup));

        assertThrows(IllegalArgumentException.class,
                () -> groupService.joinGroupWithInviteCode(INVITE_CODE, userId));
    }

    @Test
    void joinGroupWithInviteCode_AlreadyMember_ThrowsException() {
        String userId = "user-456";

        testGroup.addMember(userId);
        assertTrue(testGroup.isMember(userId));

        when(groupRepository.findByInviteCode(INVITE_CODE)).thenReturn(Optional.of(testGroup));

        assertThrows(IllegalArgumentException.class,
                () -> groupService.joinGroupWithInviteCode(INVITE_CODE, userId));
    }

    @Test
    void addMemberToGroup_ValidUser_AddsMember() {
        String userId = "user-456";
        when(groupRepository.findById(GROUP_ID)).thenReturn(Optional.of(testGroup));
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        Group result = groupService.addMemberToGroup(GROUP_ID, userId);

        assertTrue(result.isMember(userId));
        verify(groupRepository).save(testGroup);
    }

    @Test
    void removeMemberFromGroup_ValidUser_RemovesMember() {
        String userId = "user-456";
        testGroup.addMember(userId);

        when(groupRepository.findById(GROUP_ID)).thenReturn(Optional.of(testGroup));
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        Group result = groupService.removeMemberFromGroup(GROUP_ID, userId);

        assertFalse(result.isMember(userId));
        verify(groupRepository).save(testGroup);
    }

    @Test
    void updateGroup_ValidParameters_UpdatesGroup() {
        String newName = "Updated Group";
        String newDescription = "Updated Description";
        int newMaxMembers = 20;

        when(groupRepository.findById(GROUP_ID)).thenReturn(Optional.of(testGroup));
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        Group result = groupService.updateGroup(GROUP_ID, newName, newDescription, newMaxMembers);

        assertEquals(newName, result.getName());
        assertEquals(newDescription, result.getDescription());
        assertEquals(newMaxMembers, result.getMaxMembers());
        verify(groupRepository).save(testGroup);
    }

    @Test
    void updateGroup_MaxMembersLessThanCurrent_ThrowsException() {
        testGroup.addMember("user-2");
        testGroup.addMember("user-3");
        testGroup.addMember("user-4");

        int newMaxMembers = 2;

        when(groupRepository.findById(GROUP_ID)).thenReturn(Optional.of(testGroup));

        assertThrows(IllegalArgumentException.class,
                () -> groupService.updateGroup(GROUP_ID, "New Name", "New Desc", newMaxMembers));
    }

    @Test
    void updateGroup_MaxMembersEqualToCurrent_UpdatesSuccessfully() {
        testGroup.addMember("user-2");
        testGroup.addMember("user-3");

        int newMaxMembers = 3;

        when(groupRepository.findById(GROUP_ID)).thenReturn(Optional.of(testGroup));
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        assertDoesNotThrow(() ->
                groupService.updateGroup(GROUP_ID, "New Name", "New Desc", newMaxMembers));
    }

    @Test
    void generateNewInviteCode_ValidGroup_GeneratesNewCode() {
        when(groupRepository.findById(GROUP_ID)).thenReturn(Optional.of(testGroup));
        when(groupRepository.existsByInviteCode(any())).thenReturn(false);
        when(groupRepository.save(any(Group.class))).thenReturn(testGroup);

        String newCode = groupService.generateNewInviteCode(GROUP_ID);

        assertNotNull(newCode);
        assertNotEquals(INVITE_CODE, newCode);
        verify(groupRepository).save(testGroup);
    }

    @Test
    void getGroupsByCreator_ValidCreator_ReturnsGroups() {
        List<Group> groups = List.of(testGroup);
        when(groupRepository.findByCreatorId(CREATOR_ID)).thenReturn(groups);

        List<Group> result = groupService.getGroupsByCreator(CREATOR_ID);

        assertEquals(1, result.size());
        assertEquals(testGroup, result.get(0));
    }

    @Test
    void getGroupsByEvent_ValidEvent_ReturnsGroups() {
        List<Group> groups = List.of(testGroup);
        when(groupRepository.findByEventId(EVENT_ID)).thenReturn(groups);

        List<Group> result = groupService.getGroupsByEvent(EVENT_ID);

        assertEquals(1, result.size());
        assertEquals(testGroup, result.get(0));
    }

    @Test
    void getGroupsByMember_ValidMember_ReturnsGroups() {
        String userId = "user-456";
        testGroup.addMember(userId);
        List<Group> groups = List.of(testGroup);

        when(groupRepository.findByMemberIdsContaining(userId)).thenReturn(groups);

        List<Group> result = groupService.getGroupsByMember(userId);

        assertEquals(1, result.size());
        assertEquals(testGroup, result.get(0));
    }
}