package com.future.apix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.config.filter.CorsFilter;
import com.future.apix.controller.controlleradvice.DefaultControllerAdvice;
import com.future.apix.entity.Team;
import com.future.apix.entity.User;
import com.future.apix.entity.enumeration.TeamAccess;
import com.future.apix.entity.teamdetail.Member;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.TeamService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.PrintingResultHandler;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class TeamControllerTest {
    private MockMvc mvc;

    @Mock
    TeamService teamService;

    @InjectMocks
    TeamController teamController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String TEAM_ID = "test-id";
    private static final String TEAM_NAME = "TeamTest";
    private static final String TEAM_DIVISION = "division";
    private static final TeamAccess TEAM_ACCESS = TeamAccess.PUBLIC;
    private static final String TEAM_CREATOR = "test";

    private static final String USER_USERNAME = "test";
    private static final List<String> USER_ROLES = new ArrayList<>(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
    private static final List<String> USER_TEAMS = new ArrayList<>(Arrays.asList("TeamTest"));
    private static final User USER = new User("", USER_USERNAME, "", USER_ROLES, USER_TEAMS);

    private static final List<Member> TEAM_MEMBER = Collections.singletonList(new Member(USER_USERNAME, true));
    private static final Team TEAM = Team.builder()
            .id(TEAM_ID)
            .name(TEAM_NAME)
            .access(TEAM_ACCESS)
            .creator(TEAM_CREATOR)
            .members(TEAM_MEMBER)
            .build();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mvc = MockMvcBuilders.standaloneSetup(teamController)
                .setControllerAdvice(new DefaultControllerAdvice())
                .addFilter(new CorsFilter())
                .build();
    }

    @Test
    public void getTeams_Found() throws Exception {
        when(teamService.getTeams()).thenReturn(Collections.singletonList(TEAM));
        mvc.perform(get("/teams"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("test-id")))
                .andExpect(jsonPath("$[0].name", is("TeamTest")))
                .andExpect(jsonPath("$[0].division", is("division")))
                .andExpect(jsonPath("$[0].access", is("public")))
                .andExpect(jsonPath("$[0].creator", is("test")))
                .andExpect(jsonPath("$[0].members", hasSize(1)));
        verify(teamService, times(1)).getTeams();
    }

    @Test
    public void getMyTeams_test() throws Exception {
        when(teamService.getMyTeam(any())).thenReturn(Collections.singletonList(TEAM));

        mvc.perform(get("/teams/my-team"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("test-id")))
                .andExpect(jsonPath("$[0].name", is("TeamTest")))
                .andExpect(jsonPath("$[0].division", is("division")))
                .andExpect(jsonPath("$[0].access", is("public")))
                .andExpect(jsonPath("$[0].creator", is("test")))
                .andExpect(jsonPath("$[0].members", hasSize(1)));
        verify(teamService, times(1)).getMyTeam(any());
    }

    @Test
    public void createTeam_success() throws Exception {
        when(teamService.createTeam(any())).thenReturn(new Team());

        mvc.perform(post("/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TEAM)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Team is created!")));
        verify(teamService, times(1)).createTeam(any());
    }

    @Test
    public void editTeam_test() throws Exception {
        when(teamService.editTeam(anyString(), any())).thenReturn(RequestResponse.success("Members have been invited!"));
        mvc.perform(put("/teams/{name}","TeamTest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TEAM)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Members have been invited!")));
        verify(teamService, times(1)).editTeam("TeamTest", TEAM);
    }

    @Test
    public void getTeamByName_test() throws Exception {
        when(teamService.getTeamByName(TEAM_NAME)).thenReturn(TEAM);
        mvc.perform(get("/teams/{name}", "TeamTest")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("test-id")))
                .andExpect(jsonPath("$.name", is("TeamTest")))
                .andExpect(jsonPath("$.division", is("division")))
                .andExpect(jsonPath("$.access", is("public")))
                .andExpect(jsonPath("$.creator", is("test")))
                .andExpect(jsonPath("$.members", hasSize(1)));
        verify(teamService, times(1)).getTeamByName(TEAM_NAME);
    }

    /*
    @Test
    public void grantTeam_test() throws Exception {
        when(teamService.grantTeamAccess(TEAM_NAME, TEAM_MEMBER))
                .thenReturn(RequestResponse.success("Team members grant have been updated!"));

        mvc.perform(put("/teams/{name}", "TeamTest")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(TEAM_MEMBER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Team members grant have been updated!")));
        verify(teamService, times(1)).grantTeamAccess(TEAM_NAME, TEAM_MEMBER);
    }

     */
}
