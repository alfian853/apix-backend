package com.future.apix.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.Team;
import com.future.apix.entity.User;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidAuthenticationException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
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

import javax.swing.text.html.Option;
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
    ApiRepository apiRepository;

    @InjectMocks
    ApiTeamServiceImpl mock;

    private ApiProject project;

    private static final Team TEAM = Team.builder()
        .id("test-id")
        .name("TeamTest")
        .division("division")
        .access("public")
        .creator("test")
        .build();

    private static final Team TEAM_DIFFCREATOR = Team.builder()
        .id("test-id")
        .name("TeamTest")
        .division("division")
        .access("public")
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

        /*
        ApiProject project1 = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
        ApiProject project2 = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
        ApiProject project3 = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
        project1.setProjectOwner(TEAM); project2.setProjectOwner(TEAM);
        projectWithoutTeam = Optional.of(project1);
        project2.getTeams().add("AdditionalTeam");
        projectWithTeam = Optional.of(project2);
        project3.setProjectOwner(TEAM_DIFFCREATOR);
        projectDifferentCreator = Optional.of(project3);

         */
    }

    /*
        public RequestResponse grantTeamAccess(String id, ProjectAssignTeamRequest request)
     */
    @Test
    public void grantTeamAccess_teamNotFound() {
        ProjectAssignTeamRequest request = new ProjectAssignTeamRequest("grant", "TeamTest");
        when(teamRepository.findByName(anyString())).thenReturn(null);
        try {
            mock.grantTeamAccess("123", request);
        } catch (DataNotFoundException e) {
            Assert.assertEquals("Team does not exists!", e.getMessage());
        }
    }

    @Test
    public void grantTeamAccess_projectNotFound() {
        ProjectAssignTeamRequest request = new ProjectAssignTeamRequest("grant", "TeamTest");
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);
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

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER);
        UserProfileResponse expected = new UserProfileResponse();
        expected.setStatusToSuccess(); expected.setMessage("User is authenticated");
        expected.setUsername(USER_USERNAME); expected.setRoles(USER_ROLES); expected.setTeams(USER_TEAMS);
        doReturn(expected).when(mapper).convertValue(any(), eq(UserProfileResponse.class));

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
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);
        when(apiRepository.findById(anyString())).thenReturn(projectWithTeam);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER);
        UserProfileResponse profile = new UserProfileResponse();
        profile.setStatusToSuccess(); profile.setMessage("User is authenticated");
        profile.setUsername(USER_USERNAME); profile.setRoles(USER_ROLES); profile.setTeams(USER_TEAMS);
        doReturn(profile).when(mapper).convertValue(any(), eq(UserProfileResponse.class));

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

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER);
        UserProfileResponse profile = new UserProfileResponse();
        profile.setStatusToSuccess(); profile.setMessage("User is authenticated");
        profile.setUsername(USER_USERNAME); profile.setRoles(USER_ROLES); profile.setTeams(USER_TEAMS);
        doReturn(profile).when(mapper).convertValue(any(), eq(UserProfileResponse.class));

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
            .division("division")
            .access("public")
            .creator("not-test")
            .build();
        project.setProjectOwner(teamDiff);
        Optional<ApiProject> projectDifferentCreator = Optional.of(project);
        ProjectAssignTeamRequest request = new ProjectAssignTeamRequest("grant", "AdditionalTeam");
        when(teamRepository.findByName(anyString())).thenReturn(TEAM);
        when(apiRepository.findById(anyString())).thenReturn(projectDifferentCreator);

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER);
        UserProfileResponse profile = new UserProfileResponse();
        profile.setStatusToSuccess(); profile.setMessage("User is authenticated");
        profile.setUsername(USER_USERNAME); profile.setRoles(USER_ROLES); profile.setTeams(USER_TEAMS);
        doReturn(profile).when(mapper).convertValue(any(), eq(UserProfileResponse.class));

        try {
            mock.grantTeamAccess("123", request);
        } catch (InvalidAuthenticationException e) {
            Assert.assertEquals("You are unauthorized to grant team!", e.getMessage());
        }
    }
}
