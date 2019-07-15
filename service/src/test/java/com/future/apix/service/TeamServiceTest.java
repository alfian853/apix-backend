package com.future.apix.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.Team;
import com.future.apix.entity.User;
import com.future.apix.entity.teamdetail.Member;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.DuplicateEntryException;
import com.future.apix.exception.InvalidAuthenticationException;
import com.future.apix.repository.TeamRepository;
import com.future.apix.repository.UserRepository;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.UserProfileResponse;
import com.future.apix.service.impl.TeamServiceImpl;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Array;
import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TeamServiceTest {
    @InjectMocks
    TeamServiceImpl serviceMock;

    @Mock
    TeamRepository teamRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ObjectMapper oMapper;

    private static final String TEAM_ID = "test-id";
    private static final String TEAM_NAME = "TeamTest";
    private static final String TEAM_DIVISION = "division";
    private static final String TEAM_ACCESS = "public";
    private static final String TEAM_CREATOR = "test";

    private static final String USER_USERNAME = "test";
    private static final List<String> USER_ROLES = new ArrayList<>(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
    private static final List<String> USER_TEAMS = new ArrayList<>(Arrays.asList("TeamExist"));
    private static final User USER = new User("", USER_USERNAME, "", USER_ROLES, USER_TEAMS);

    private static final List<Member> TEAM_MEMBER = new ArrayList<>(Arrays.asList(new Member(USER_USERNAME, true)));
    private static final Team TEAM = Team.builder()
            .id(TEAM_ID)
            .name(TEAM_NAME)
            .division(TEAM_DIVISION)
            .access(TEAM_ACCESS)
            .creator(TEAM_CREATOR)
            .members(TEAM_MEMBER)
            .build();

    /*
        public List<Team> getTeams()
     */
    @Test
    public void getTeams_test(){
        when(teamRepository.findAll()). thenReturn(Arrays.asList(TEAM));
        List<Team> teams = serviceMock.getTeams();
        Assert.assertEquals(Arrays.asList(TEAM), teams);
        Assert.assertEquals(1, teams.size());
    }

    /*
        public List<Team> getMyTeam(Authentication authentication)
     */
    @Test
    public void getMyTeam_authenticationNull(){
        try {
            serviceMock.getMyTeam(null);
        } catch (InvalidAuthenticationException e) {
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
        when(teamRepository.findByMembersUsername(USER_USERNAME)).thenReturn(Arrays.asList(TEAM));

        List<Team> teams = serviceMock.getMyTeam(authentication);
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

        List<Team> teams = serviceMock.getMyTeam(authentication);
        Assert.assertEquals(0, teams.size());
    }

    /*
        public Team getTeamByName(String name)
     */
    @Test
    public void getTeamByName_failed(){
        try {
            serviceMock.getTeamByName("not-TeamTest");
        } catch (DataNotFoundException e){
            Assert.assertEquals("Team is not found!", e.getMessage());
        }
    }

    @Test
    public void getTeamByName_success(){
        when(teamRepository.findByName(TEAM_NAME)).thenReturn(TEAM);
        Team team = serviceMock.getTeamByName(TEAM_NAME);
        Assert.assertEquals("test-id", team.getId());
        Assert.assertEquals("TeamTest", team.getName());
        Assert.assertEquals("division", team.getDivision());
        Assert.assertEquals("public", team.getAccess());
        Assert.assertEquals("test", team.getCreator());
    }

    /*
        public RequestResponse createTeam(Team team)
     */
    @Test
    public void createTeam_teamAlreadyExists(){
        when(teamRepository.findByName(TEAM_NAME)).thenReturn(TEAM);
        try {
            serviceMock.createTeam(TEAM);
        } catch (DuplicateEntryException e) {
            Assert.assertEquals("Team name is already exists!", e.getMessage());
        }
    }

    @Test
    public void createTeam_success(){
        when(teamRepository.findByName(TEAM_NAME)).thenReturn(null);
        when(teamRepository.save(any(Team.class))).thenReturn(TEAM);
        when(userRepository.findByUsername(USER_USERNAME)).thenReturn(USER);
        RequestResponse response = serviceMock.createTeam(TEAM);
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("Team is created!", response.getMessage());
    }

    /*
        public RequestResponse editTeam(String name, Team team)
     */
    @Test
    public void editTeam_teamAlreadyExists(){
        when(teamRepository.findByName(TEAM_NAME)).thenReturn(null);
        try {
            serviceMock.editTeam("TeamTest", TEAM);
        } catch (DataNotFoundException e) {
            Assert.assertEquals("Team does not exist!", e.getMessage());
        }
    }

    @Test
    public void editTeam_successMemberAlreadyExist(){
        when(teamRepository.findByName(TEAM_NAME)).thenReturn(TEAM);
        RequestResponse response = serviceMock.editTeam("TeamTest", TEAM);
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("Members have been invited!", response.getMessage());
    }

    @Test
    public void editTeam_successAddMember(){
        when(teamRepository.findByName(TEAM_NAME)).thenReturn(TEAM);
        Team teamNewMember = Team.builder()
            .id(TEAM_ID)
            .name(TEAM_NAME)
            .division(TEAM_DIVISION)
            .access(TEAM_ACCESS)
            .creator(TEAM_CREATOR)
            .members(Arrays.asList(new Member("JohnDoe", false)))
            .build();

//        https://stackoverflow.com/questions/5755477/java-list-add-unsupportedoperationexception

        RequestResponse response = serviceMock.editTeam("TeamTest",teamNewMember);
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("Members have been invited!", response.getMessage());
    }
}
