package com.future.apix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.config.filter.CorsFilter;
import com.future.apix.config.jwt.JwtTokenProvider;
import com.future.apix.controlleradvice.DefaultControllerAdvice;
import com.future.apix.entity.User;
import com.future.apix.request.AuthenticationRequest;
import com.future.apix.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class AuthControllerTest {
    private MockMvc mvc;

    @Mock
    private UserService userService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String USER_ID = "test-id";
    private static final String USER_USERNAME = "username";
    private static final String USER_PASSWORD = new BCryptPasswordEncoder().encode("password");
    private static final List<String> USER_ROLES = new ArrayList<>(Arrays.asList("ROLE_USER"));
    private static final List<String> USER_TEAMS = new ArrayList<>(Arrays.asList("TeamTest"));
    private static final User USER = new User(USER_ID, USER_USERNAME, USER_PASSWORD, USER_ROLES, USER_TEAMS);


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new DefaultControllerAdvice())
                .addFilter(new CorsFilter())
                .build();
    }

    @Test
    public void login_success() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userService.getUser(anyString())).thenReturn(USER);
        when(jwtTokenProvider.createToken(anyString(), anyList())).thenReturn("token");
        AuthenticationRequest request = new AuthenticationRequest("username", "password");

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User is authenticated!")))
                .andExpect(jsonPath("$.username", is("username")))
                .andExpect(jsonPath("$.token", is("token")));
    }

    @Test
    public void login_fail() throws Exception {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(userService.getUser(anyString())).thenReturn(USER);
        when(jwtTokenProvider.createToken(anyString(), anyList()))
                .thenThrow(new BadCredentialsException("Invalid username or password!"));
        AuthenticationRequest request = new AuthenticationRequest("username", "password");

        mvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Invalid username or password!")));
    }
}
