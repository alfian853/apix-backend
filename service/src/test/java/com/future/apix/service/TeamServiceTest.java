package com.future.apix.service;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.Team;
import com.future.apix.entity.User;
import com.future.apix.entity.enumeration.TeamAccess;
import com.future.apix.entity.teamdetail.Member;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.DuplicateEntryException;
import com.future.apix.exception.InvalidAuthenticationException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ProjectRepository;
import com.future.apix.repository.TeamRepository;
import com.future.apix.repository.UserRepository;
import com.future.apix.request.TeamCreateRequest;
import com.future.apix.request.TeamInviteRequest;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.UserProfileResponse;
import com.future.apix.service.impl.TeamServiceImpl;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonValue;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TeamServiceTest {
    @InjectMocks
    private TeamServiceImpl teamService;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    ObjectMapper oMapper;

    private static final String TEAM_ID = "test-id";
    private static final String TEAM_NAME = "TeamTest";
    private static final String TEAM_DIVISION = "division";
    private static final TeamAccess TEAM_ACCESS = TeamAccess.PUBLIC;
    private static final String TEAM_CREATOR = "test";

    private static final String USER_USERNAME = "test";
    private static final List<String> USER_ROLES = new ArrayList<>(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
    private static final List<String> USER_TEAMS = new ArrayList<>(Arrays.asList("TeamExist"));
    private static final User USER = new User("", USER_USERNAME, "", USER_ROLES, USER_TEAMS);

    private static final List<Member> TEAM_MEMBER = new ArrayList<>(Arrays.asList(new Member(USER_USERNAME, true)));
    private static final Team TEAM = Team.builder()
            .id(TEAM_ID)
            .name(TEAM_NAME)
            .access(TEAM_ACCESS)
            .creator(TEAM_CREATOR)
            .members(TEAM_MEMBER)
            .build();

    /*
        public List<Team> getTeams()
     */
    @Test
    public void getTeams_test(){
        when(teamRepository.findAll()).thenReturn(Arrays.asList(TEAM));
        List<Team> teams = teamService.getTeams();
        Assert.assertEquals(Arrays.asList(TEAM), teams);
        Assert.assertEquals(1, teams.size());
    }

    /*
        public List<Team> getMyTeam(Authentication authentication)
     */
    @Test
    public void getMyTeam_authenticationNull(){
        try {
            teamService.getMyTeam(null);
        } catch (InvalidRequestException e) {
            Assert.assertEquals("User is not authenticated!", e.getMessage());
        }
    }

    @Test
    public void getMyTeam_success(){
        Authentication authentication = mock(Authentication.class);
//        when(authentication.getPrincipal()).thenReturn(TEAM);

        UserProfileResponse expected = new UserProfileResponse();
        expected.setStatusToSuccess();
        expected.setUsername(USER_USERNAME); expected.setRoles(USER_ROLES); expected.setTeams(USER_TEAMS);

        when(authentication.getPrincipal()).thenReturn(USER);
        when(oMapper.convertValue(Mockito.any(), eq(UserProfileResponse.class))).thenReturn(expected);
        when(teamRepository.findByMembersUsername(anyString())).thenReturn(Arrays.asList(TEAM));

        List<Team> teams = teamService.getMyTeam(authentication);
        Assert.assertEquals(Arrays.asList(TEAM), teams);
        Assert.assertEquals(1, teams.size());
    }

    @Test
    public void getMyTeam_notBelongToAnyTeams(){
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(new User("", "not-test", "", USER_ROLES, USER_TEAMS));

        UserProfileResponse expected = new UserProfileResponse();
        expected.setStatusToSuccess();
        expected.setUsername("not-test"); expected.setRoles(USER_ROLES); expected.setTeams(USER_TEAMS);
        when(oMapper.convertValue(Mockito.any(), eq(UserProfileResponse.class))).thenReturn(expected);
//        when(teamRepository.findByMembersUsername(USER_USERNAME)).thenReturn(Arrays.asList(TEAM));

        List<Team> teams = teamService.getMyTeam(authentication);
        Assert.assertEquals(0, teams.size());
    }

    /*
        public Team getTeamByName(String name)
     */
    @Test
    public void getTeamByName_failed(){
        try {
            teamService.getTeamByName("not-TeamTest");
        } catch (DataNotFoundException e){
            Assert.assertEquals("Team is not found!", e.getMessage());
        }
    }

    @Test
    public void getTeamByName_success(){
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);
        Team team = teamService.getTeamByName(TEAM_NAME);
        Assert.assertEquals("test-id", team.getId());
        Assert.assertEquals("TeamTest", team.getName());
        Assert.assertEquals(TeamAccess.PUBLIC, team.getAccess());
        Assert.assertEquals("test", team.getCreator());
    }

    /*
        public RequestResponse createTeam(Team team)
     */
    @Test
    public void createTeam_teamAlreadyExists(){
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);
        TeamCreateRequest request = TeamCreateRequest.builder()
            .creator(TEAM_CREATOR)
            .teamName(TEAM_NAME)
            .members(Collections.emptyList())
            .access(TEAM_ACCESS)
            .build();
        try {
            teamService.createTeam(request);
        } catch (DuplicateEntryException e) {
            Assert.assertEquals("Team name is already exists!", e.getMessage());
        }
    }

    @Test
    public void createTeam_creatorNotFound(){
        when(teamRepository.findByName(anyString())).thenReturn(null);
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        TeamCreateRequest request = TeamCreateRequest.builder()
            .creator(TEAM_CREATOR)
            .teamName(TEAM_NAME)
            .access(TEAM_ACCESS)
            .members(Collections.singletonList(USER_USERNAME))
            .build();
        try {
            teamService.createTeam(request);
        } catch (DataNotFoundException e) {
            Assert.assertEquals("User creator not found!", e.getMessage());
        }
    }

    @Test
    public void createTeam_success(){
        when(teamRepository.findByName(anyString())).thenReturn(null);
        when(teamRepository.save(any(Team.class))).thenReturn(TEAM);
        when(userRepository.findByUsername(anyString())).thenReturn(USER);
        TeamCreateRequest request = TeamCreateRequest.builder()
            .creator(TEAM_CREATOR)
            .teamName(TEAM_NAME)
            .access(TEAM_ACCESS)
            .members(Collections.singletonList(USER_USERNAME))
            .build();
        Team response = teamService.createTeam(request);
        Assert.assertTrue(USER.getTeams().contains(TEAM_NAME));
        verify(teamRepository).save(any());
    }

    @Test
    public void deleteTeam_TeamNotFound() {
        try {
            teamService.deleteTeam(TEAM_NAME);
        } catch (DataNotFoundException e) {
            Assert.assertEquals("Team does not exist!", e.getMessage());
        }
    }

    @Test
    public void deleteTeam_NotCreatorOfTeam() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER);
        UserProfileResponse expected = new UserProfileResponse();
        expected.setStatusToSuccess(); expected.setMessage("User is authenticated");
        expected.setUsername("not username"); expected.setRoles(USER_ROLES); expected.setTeams(USER_TEAMS);
        when(oMapper.convertValue(Mockito.any(), eq(UserProfileResponse.class))).thenReturn(expected);
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);
        try {
            teamService.deleteTeam(TEAM_NAME);
        } catch (InvalidRequestException e) {
            Assert.assertEquals("You are not allowed to delete this team!", e.getMessage());
        }
    }

    @Test
    public void deleteTeam_success() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER);
        UserProfileResponse expected = new UserProfileResponse();
        expected.setStatusToSuccess(); expected.setMessage("User is authenticated");
        expected.setUsername("test"); expected.setRoles(USER_ROLES); expected.setTeams(USER_TEAMS);
        when(oMapper.convertValue(Mockito.any(), eq(UserProfileResponse.class))).thenReturn(expected);
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);

        RequestResponse response = teamService.deleteTeam("TeamTest");
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("Team has been deleted!", response.getMessage());
        verify(teamRepository, times(1)).deleteById(anyString());
    }
    
    /*
        public RequestResponse grantTeamAccess(String name, TeamInviteRequest request)
     */
    @Test
    public void grantTeamAccess_TeamNotFound(){
        when(teamRepository.findByName(anyString())).thenReturn(null);
        TeamInviteRequest request = TeamInviteRequest.builder()
            .teamName(TEAM_NAME)
            .members(Arrays.asList(USER_USERNAME))
            .invite(false)
            .build();
        try {
            teamService.grantTeamAccess("TeamTest", request);
        } catch (DataNotFoundException e){
            Assert.assertEquals("Team does not exist!", e.getMessage());
        }
    }

    @Test
    public void grantTeamAccess_MemberSizeIsZero(){
        TeamInviteRequest request = TeamInviteRequest.builder()
            .teamName(TEAM_NAME)
            .members(Collections.emptyList())
            .invite(false)
            .build();
        try {
            teamService.grantTeamAccess("TeamTest", request);
        } catch (DataNotFoundException e){
            Assert.assertEquals("There is no member to be granted!", e.getMessage());
        }
    }

    @Test
    public void grantTeamAccess_MemberIsNull(){
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        TeamInviteRequest request = TeamInviteRequest.builder()
            .teamName(TEAM_NAME)
            .members(Arrays.asList(USER_USERNAME))
            .invite(true)
            .build();
        RequestResponse response = teamService.grantTeamAccess(TEAM_NAME, request);
        Assert.assertEquals(false, response.getSuccess());
        Assert.assertEquals("Members: test, is failed to updated!", response.getMessage());
    }

    @Test
    public void grantTeamAccess_Success(){
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);
        when(userRepository.findByUsername(anyString())).thenReturn(USER);
        TeamInviteRequest request = TeamInviteRequest.builder()
            .teamName(TEAM_NAME)
            .members(Arrays.asList(USER_USERNAME))
            .invite(true)
            .build();
        RequestResponse response = teamService.grantTeamAccess(TEAM_NAME, request);
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("Members have joined team TeamTest!", response.getMessage());
        Assert.assertTrue(USER.getTeams().contains(TEAM_NAME));
    }

    @Test
    public void inviteMembersToTeam_TeamNotFound(){
        when(teamRepository.findByName(anyString())).thenReturn(null);
        try {
            teamService.inviteMembersToTeam(TEAM_NAME, new TeamInviteRequest());
        } catch (DataNotFoundException e) {
            Assert.assertEquals("Team does not exist!", e.getMessage());
        }
    }

    @Test
    public void inviteMembersToTeam_NotTeamCreator() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER);
        UserProfileResponse expected = new UserProfileResponse();
        expected.setStatusToSuccess(); expected.setMessage("User is authenticated");
        expected.setUsername(USER_USERNAME); expected.setRoles(USER_ROLES); expected.setTeams(USER_TEAMS);
        when(oMapper.convertValue(Mockito.any(), eq(UserProfileResponse.class))).thenReturn(expected);
        Team diffTeam = Team.builder().name("TeamTest").creator("diffCreator").build();
        when(teamRepository.findByName(anyString())).thenReturn(diffTeam);
        try {
            teamService.inviteMembersToTeam(TEAM_NAME, new TeamInviteRequest());
        } catch (InvalidRequestException e){
            Assert.assertEquals("You do not have privilege to add members!", e.getMessage());
        }
    }


    @Test
    public void inviteMembersToTeam_FailedAddingMembers() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER);
        UserProfileResponse expected = new UserProfileResponse();
        expected.setStatusToSuccess(); expected.setMessage("User is authenticated");
        expected.setUsername(USER_USERNAME); expected.setRoles(USER_ROLES); expected.setTeams(USER_TEAMS);
        when(oMapper.convertValue(Mockito.any(), eq(UserProfileResponse.class))).thenReturn(expected);
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);
        when(userRepository.findByUsername(anyString())).thenReturn(USER);

        UpdateResult updateResult = mock(UpdateResult.class);
        when(teamRepository.inviteMemberToTeam(anyString(), anyString(), anyBoolean()))
            .thenReturn(updateResult);

        TeamInviteRequest request = TeamInviteRequest.builder()
            .teamName(TEAM_NAME)
            .members(Arrays.asList(USER_USERNAME))
            .invite(true)
            .build();
        RequestResponse response = teamService.inviteMembersToTeam(TEAM_NAME, request);
        Assert.assertFalse(response.getSuccess());
        Assert.assertEquals("Failed in adding members!", response.getMessage());
    }

    @Test
    public void inviteMembersToTeam_SuccessAddingMembers() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER);
        UserProfileResponse expected = new UserProfileResponse();
        expected.setStatusToSuccess(); expected.setMessage("User is authenticated");
        expected.setUsername(USER_USERNAME); expected.setRoles(USER_ROLES); expected.setTeams(USER_TEAMS);
        when(oMapper.convertValue(Mockito.any(), eq(UserProfileResponse.class))).thenReturn(expected);
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);
        when(userRepository.findByUsername(anyString())).thenReturn(USER);

        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.getMatchedCount()).thenReturn(1L);
        when(teamRepository.inviteMemberToTeam(anyString(), anyString(), anyBoolean()))
            .thenReturn(updateResult);
        TeamInviteRequest request = TeamInviteRequest.builder()
            .teamName(TEAM_NAME)
            .members(Arrays.asList(USER_USERNAME))
            .invite(true)
            .build();
        RequestResponse response = teamService.inviteMembersToTeam(TEAM_NAME, request);
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("Members have been invited!", response.getMessage());
    }

    /*
        public RequestResponse removeMembersFromTeam(String name, TeamInviteRequest request)
     */
    @Test
    public void removeMembersFromTeam_TeamNotFound(){
        try {
            teamService.removeMembersFromTeam("TeamTest", new TeamInviteRequest());
        } catch (DataNotFoundException e){
            Assert.assertEquals("Team does not exist!", e.getMessage());
        }
    }

    @Test
    public void removeMembersFromTeam_NotTeamCreator() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER);
        UserProfileResponse expected = new UserProfileResponse();
        expected.setStatusToSuccess(); expected.setMessage("User is authenticated");
        expected.setUsername(USER_USERNAME); expected.setRoles(USER_ROLES); expected.setTeams(USER_TEAMS);
        when(oMapper.convertValue(Mockito.any(), eq(UserProfileResponse.class))).thenReturn(expected);
        Team diffTeam = Team.builder().name("TeamTest").creator("diffCreator").build();
        when(teamRepository.findByName(anyString())).thenReturn(diffTeam);
        try {
            teamService.removeMembersFromTeam(TEAM_NAME, new TeamInviteRequest());
        } catch (InvalidRequestException e){
            Assert.assertEquals("You do not have privilege to remove members!", e.getMessage());
        }
    }

    @Test
    public void removeMembersFromTeam_FailedMembers() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER);
        UserProfileResponse expected = new UserProfileResponse();
        expected.setStatusToSuccess(); expected.setMessage("User is authenticated");
        expected.setUsername(USER_USERNAME); expected.setRoles(USER_ROLES); expected.setTeams(USER_TEAMS);
        when(oMapper.convertValue(Mockito.any(), eq(UserProfileResponse.class))).thenReturn(expected);
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);
        when(userRepository.findByUsername(anyString())).thenReturn(USER);

        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.getModifiedCount()).thenReturn(0L);
        when(teamRepository.removeMemberFromTeam(anyString(), anyString()))
            .thenReturn(updateResult);

        TeamInviteRequest request = TeamInviteRequest.builder()
            .teamName(TEAM_NAME)
            .members(Arrays.asList(USER_USERNAME))
            .invite(true)
            .build();
        RequestResponse response = teamService.removeMembersFromTeam(TEAM_NAME, request);
        Assert.assertFalse(response.getSuccess());
        Assert.assertEquals("Failed in removing members!", response.getMessage());
    }

    @Test
    public void removeMembersFromTeam_successMember() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER);
        UserProfileResponse expected = new UserProfileResponse();
        expected.setStatusToSuccess(); expected.setMessage("User is authenticated");
        expected.setUsername(USER_USERNAME); expected.setRoles(USER_ROLES); expected.setTeams(USER_TEAMS);
        when(oMapper.convertValue(Mockito.any(), eq(UserProfileResponse.class))).thenReturn(expected);
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);
        when(userRepository.findByUsername(anyString())).thenReturn(USER);

        UpdateResult updateResult = mock(UpdateResult.class);
        when(updateResult.getModifiedCount()).thenReturn(new Long(1));
        when(teamRepository.removeMemberFromTeam(anyString(), anyString()))
            .thenReturn(updateResult);
        TeamInviteRequest request = TeamInviteRequest.builder()
            .teamName(TEAM_NAME)
            .members(Arrays.asList(USER_USERNAME))
            .invite(true)
            .build();
        RequestResponse response = teamService.removeMembersFromTeam(TEAM_NAME, request);
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("Members have been removed from team TeamTest!", response.getMessage());
    }


    /*
        public RequestResponse editTeam(String name, Team team)

    @Test
    public void editTeam_teamAlreadyExists(){
        when(teamRepository.findByName(anyString())).thenReturn(null);
        try {
            teamService.inviteMembers("TeamTest", TEAM);
        } catch (DataNotFoundException e) {
            Assert.assertEquals("Team does not exist!", e.getMessage());
        }
    }

    @Test
    public void editTeam_successMemberAlreadyExist(){
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);
        RequestResponse response = teamService.inviteMembers("TeamTest", TEAM);
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("Members have been invited!", response.getMessage());
    }

    @Test
    public void editTeam_successAddMember(){
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);
        Team teamNewMember = Team.builder()
            .id(TEAM_ID)
            .name(TEAM_NAME)
            .access(TEAM_ACCESS)
            .creator(TEAM_CREATOR)
            .members(Arrays.asList(new Member("JohnDoe", false)))
            .build();

//        https://stackoverflow.com/questions/5755477/java-list-add-unsupportedoperationexception

        RequestResponse response = teamService.inviteMembers("TeamTest",teamNewMember);
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("Members have been invited!", response.getMessage());
    }
     */
}
