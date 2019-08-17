package com.future.apix.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.Team;
import com.future.apix.entity.User;
import com.future.apix.entity.enumeration.TeamAccess;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ProjectRepository;
import com.future.apix.repository.TeamRepository;
import com.future.apix.request.ProjectAssignTeamRequest;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.UserProfileResponse;
import com.future.apix.service.impl.ApiTeamServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApiTeamServiceTest {
    @Mock
    TeamRepository teamRepository;

    @Spy
    ObjectMapper mapper;

    @Mock
    ProjectRepository apiRepository;

    @InjectMocks
    ApiTeamServiceImpl mock;

    private ApiProject project;

    private static final Team TEAM = Team.builder()
        .id("test-id")
        .name("TeamTest")
        .access(TeamAccess.PUBLIC)
        .creator("test")
        .build();

    private static final Team TEAM_DIFFCREATOR = Team.builder()
        .id("test-id")
        .name("TeamTest")
        .access(TeamAccess.PUBLIC)
        .creator("not-test")
        .build();

    private static final String USER_ID = "test-id";
    private static final String USER_USERNAME = "test";
    private static final String USER_PASSWORD = new BCryptPasswordEncoder().encode("test");
    private static final List<String>
        USER_ROLES = new ArrayList<>(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
    private static final List<String> USER_TEAMS = new ArrayList<>(Arrays.asList("TeamTest"));
    private static final User
        USER = new User(USER_ID, USER_USERNAME, USER_PASSWORD, USER_ROLES, USER_TEAMS);


    @Before
    public void setUp() throws IOException, URISyntaxException {
        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        project = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
    }

    /*
        public RequestResponse grantTeamAccess(String id, ProjectAssignTeamRequest request)
     */
    @Test
    public void grantTeamAccess_teamNotFound() {
        project.setProjectOwner(TEAM);
        ProjectAssignTeamRequest request = new ProjectAssignTeamRequest("grant", "TeamTest");
        when(apiRepository.findById(anyString())).thenReturn(Optional.of(project));
        mockAuth();
        try {
            mock.grantTeamAccess("123", request);
        } catch (DataNotFoundException e) {
            Assert.assertEquals("Team does not exists!", e.getMessage());
        }
    }

    @Test
    public void grantTeamAccess_projectNotFound() {
        ProjectAssignTeamRequest request = new ProjectAssignTeamRequest("grant", "TeamTest");
        when(apiRepository.findById(anyString())).thenReturn(Optional.empty());
        try {
            mock.grantTeamAccess("123", request);
        } catch (DataNotFoundException e) {
            Assert.assertEquals("Api project does not exists!", e.getMessage());
        }
    }

    @Test
    public void grantTeamAccess_addTeam(){
        project.setProjectOwner(TEAM);
        Optional<ApiProject> projectWithoutTeam = Optional.of(project);
        ProjectAssignTeamRequest request = new ProjectAssignTeamRequest("grant", "AdditionalTeam");
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);
        when(apiRepository.findById(anyString())).thenReturn(projectWithoutTeam);
        mockAuth();

        RequestResponse response = mock.grantTeamAccess("123", request);
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("Team has been assigned to project!", response.getMessage());
    }

    @Test
    public void grantTeamAccess_removeTeam(){
        project.setProjectOwner(TEAM);
        project.getTeams().add("AdditionalTeam");
        Optional<ApiProject> projectWithTeam = Optional.of(project);
        ProjectAssignTeamRequest request = new ProjectAssignTeamRequest("ungrant", "AdditionalTeam");
        when(apiRepository.findById(anyString())).thenReturn(projectWithTeam);

        mockAuth();

        RequestResponse response = mock.grantTeamAccess("123", request);
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("Team has been removed from project!", response.getMessage());
    }

    @Test
    public void grantTeamAccess_addTeamButAlreadyExists(){
        project.setProjectOwner(TEAM);
        project.getTeams().add("AdditionalTeam");
        Optional<ApiProject> projectWithTeam = Optional.of(project);
        ProjectAssignTeamRequest request = new ProjectAssignTeamRequest("grant", "AdditionalTeam");
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);
        when(apiRepository.findById(anyString())).thenReturn(projectWithTeam);

//        mockAuth();

       try {
           mock.grantTeamAccess("123", request);
       } catch (InvalidRequestException e) {
           Assert.assertEquals("Team is already in the project!", e.getMessage());
       }
    }

    @Test
    public void grantTeamAccess_unauthorized(){
        Team teamDiff = Team.builder()
            .id("test-id")
            .name("TeamTest")
            .access(TeamAccess.PUBLIC)
            .creator("not-test")
            .build();
        project.setProjectOwner(teamDiff);
        Optional<ApiProject> projectDifferentCreator = Optional.of(project);
        ProjectAssignTeamRequest request = new ProjectAssignTeamRequest("grant", "AdditionalTeam");
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);
        when(apiRepository.findById(anyString())).thenReturn(projectDifferentCreator);
        mockAuth();

        try {
            mock.grantTeamAccess("123", request);
        } catch (InvalidRequestException e) {
            Assert.assertEquals("You are unauthorized to grant team!", e.getMessage());
        }
    }

    private void mockAuth(){
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER);
        UserProfileResponse profile = new UserProfileResponse();
        profile.setStatusToSuccess(); profile.setMessage("User is authenticated");
        profile.setUsername(USER_USERNAME); profile.setRoles(USER_ROLES); profile.setTeams(USER_TEAMS);
        doReturn(profile).when(mapper).convertValue(any(), eq(UserProfileResponse.class));
    }
}
