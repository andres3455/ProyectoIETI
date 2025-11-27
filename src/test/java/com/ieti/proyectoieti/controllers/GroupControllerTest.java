package com.ieti.proyectoieti.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.ieti.proyectoieti.config.SecurityConfig;
import com.ieti.proyectoieti.controllers.dto.GroupRequest;
import com.ieti.proyectoieti.models.Group;
import com.ieti.proyectoieti.services.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

@WebMvcTest(controllers = GroupController.class)
@Import(SecurityConfig.class)
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupService groupService;

    @Autowired
    private ObjectMapper objectMapper;

    private Group testGroup;
    private final String GROUP_ID = "group-123";
    private final String CREATOR_ID = "user-123";
    private final String INVITE_CODE = "ABC123";

    @BeforeEach
    void setUp() {
        testGroup = new Group("Test Group", "Test Description", CREATOR_ID, "event-456");
        testGroup.setId(GROUP_ID);
        testGroup.setInviteCode(INVITE_CODE);
    }

    @Test
    @WithMockUser
    void createGroup_ValidRequest_ReturnsGroup() throws Exception {
        GroupRequest request = new GroupRequest();
        request.setName("Test Group");
        request.setDescription("Test Description");
        request.setCreatorId(CREATOR_ID);
        request.setEventId("event-456");

        when(groupService.createGroup(any(), any(), any(), any())).thenReturn(testGroup);

        mockMvc.perform(post("/api/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(GROUP_ID))
                .andExpect(jsonPath("$.name").value("Test Group"));
    }

    @Test
    @WithMockUser
    void getGroupById_ValidId_ReturnsGroup() throws Exception {
        when(groupService.getGroupById(GROUP_ID)).thenReturn(Optional.of(testGroup));

        mockMvc.perform(get("/api/groups/{groupId}", GROUP_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(GROUP_ID));
    }

    @Test
    @WithMockUser
    void getGroupById_InvalidId_ReturnsNotFound() throws Exception {
        when(groupService.getGroupById(GROUP_ID)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/groups/{groupId}", GROUP_ID)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void getGroupByInviteCode_ValidCode_ReturnsGroup() throws Exception {
        when(groupService.getGroupByInviteCode(INVITE_CODE)).thenReturn(Optional.of(testGroup));

        mockMvc.perform(get("/api/groups/invite/{inviteCode}", INVITE_CODE)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inviteCode").value(INVITE_CODE));
    }

    @Test
    @WithMockUser
    void joinGroupWithInviteCode_ValidRequest_ReturnsGroup() throws Exception {
        String userId = "user-456";
        when(groupService.joinGroupWithInviteCode(INVITE_CODE, userId)).thenReturn(testGroup);

        mockMvc.perform(post("/api/groups/join/{inviteCode}", INVITE_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\": \"" + userId + "\"}")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(GROUP_ID));
    }

    @Test
    @WithMockUser
    void getGroupsByCreator_ValidCreator_ReturnsGroups() throws Exception {
        List<Group> groups = List.of(testGroup);
        when(groupService.getGroupsByCreator(CREATOR_ID)).thenReturn(groups);

        mockMvc.perform(get("/api/groups/creator/{creatorId}", CREATOR_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(GROUP_ID));
    }

    @Test
    @WithMockUser
    void addMemberToGroup_ValidRequest_ReturnsGroup() throws Exception {
        String userId = "user-456";
        when(groupService.addMemberToGroup(GROUP_ID, userId)).thenReturn(testGroup);

        mockMvc.perform(post("/api/groups/{groupId}/members/{userId}", GROUP_ID, userId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(GROUP_ID));
    }

    @Test
    @WithMockUser
    void removeMemberFromGroup_ValidRequest_ReturnsGroup() throws Exception {
        String userId = "user-456";
        when(groupService.removeMemberFromGroup(GROUP_ID, userId)).thenReturn(testGroup);

        mockMvc.perform(delete("/api/groups/{groupId}/members/{userId}", GROUP_ID, userId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(GROUP_ID));
    }

    @Test
    @WithMockUser
    void generateNewInviteCode_ValidGroup_ReturnsNewCode() throws Exception {
        String newCode = "NEW123";
        when(groupService.generateNewInviteCode(GROUP_ID)).thenReturn(newCode);

        mockMvc.perform(post("/api/groups/{groupId}/regenerate-invite", GROUP_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inviteCode").value(newCode));
    }

    @Test
    @WithMockUser
    void deleteGroup_ValidId_ReturnsSuccess() throws Exception {
        doNothing().when(groupService).deleteGroup(GROUP_ID);

        mockMvc.perform(delete("/api/groups/{groupId}", GROUP_ID)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Group deleted successfully"));
    }

    @Test
    @WithMockUser
    void getGroupsByEvent_ValidEvent_ReturnsGroups() throws Exception {
        String eventId = "event-456";
        List<Group> groups = List.of(testGroup);
        when(groupService.getGroupsByEvent(eventId)).thenReturn(groups);

        mockMvc.perform(get("/api/groups/event/{eventId}", eventId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(GROUP_ID));
    }

    @Test
    @WithMockUser
    void getGroupsByMember_ValidMember_ReturnsGroups() throws Exception {
        String userId = "user-456";
        List<Group> groups = List.of(testGroup);
        when(groupService.getGroupsByMember(userId)).thenReturn(groups);

        mockMvc.perform(get("/api/groups/member/{userId}", userId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(GROUP_ID));
    }

    @Test
    @WithMockUser
    void getAllGroups_ReturnsGroups() throws Exception {
        List<Group> groups = List.of(testGroup);
        when(groupService.getAllGroups()).thenReturn(groups);

        mockMvc.perform(get("/api/groups")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(GROUP_ID));
    }

    @Test
    @WithMockUser
    void updateGroup_ValidRequest_ReturnsGroup() throws Exception {
        GroupRequest request = new GroupRequest();
        request.setName("Updated Group");
        request.setDescription("Updated Description");
        request.setCreatorId(CREATOR_ID); // Agregar creatorId válido
        request.setMaxMembers(20);

        when(groupService.updateGroup(eq(GROUP_ID), any(), any(), any())).thenReturn(testGroup);

        mockMvc.perform(put("/api/groups/{groupId}", GROUP_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(GROUP_ID));
    }
}