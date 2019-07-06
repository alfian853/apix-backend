package com.future.apix.controller;

import com.future.apix.config.filter.CorsFilter;
import com.future.apix.controlleradvice.DefaultControllerAdvice;
import com.future.apix.response.UserProfileResponse;
import com.future.apix.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {
    private MockMvc mvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private static final String USER_USERNAME = "test";
    private static final List<String> USER_ROLES = new ArrayList<>(Arrays.asList("ROLE_USER"));
    private static final List<String> USER_TEAMS = new ArrayList<>(Arrays.asList("TeamTest"));


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new DefaultControllerAdvice())
                .addFilter(new CorsFilter())
                .build();
    }

    @Test
    public void getAuth() throws Exception {
        UserProfileResponse response = new UserProfileResponse();
        response.setStatusToSuccess();
        response.setMessage("User is authenticated!");
        response.setUsername(USER_USERNAME);
        response.setRoles(USER_ROLES);
        response.setTeams(USER_TEAMS);
        when(userService.userProfile(any())).thenReturn(response);

        mvc.perform(get("/user/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User is authenticated!")))
                .andExpect(jsonPath("$.username", is("test")))
                .andExpect(jsonPath("$.roles", hasSize(1)))
                .andExpect(jsonPath("$.roles[0]", is("ROLE_USER")))
                .andExpect(jsonPath("$.teams", hasSize(1)))
                .andExpect(jsonPath("$.teams[0]", is("TeamTest")));
        verify(userService, times(1)).userProfile(any());
    }

}
