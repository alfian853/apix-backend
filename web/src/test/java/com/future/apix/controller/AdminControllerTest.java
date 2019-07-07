package com.future.apix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.config.SecurityTestConfig;
import com.future.apix.config.filter.CorsFilter;
import com.future.apix.controller.controlleradvice.DefaultControllerAdvice;
import com.future.apix.entity.User;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = SecurityTestConfig.class
)
@AutoConfigureMockMvc

public class AdminControllerTest {
    @Autowired
    private MockMvc mvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private AdminController adminController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private static final String USER_ID = "test-id";
    private static final String USER_USERNAME = "test";
    private static final String USER_PASSWORD = new BCryptPasswordEncoder().encode("test");
    private static final List<String> USER_ROLES = new ArrayList<>(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
    private static final List<String> USER_TEAMS = new ArrayList<>(Arrays.asList("TeamTest"));
    private static final User USER = new User(USER_ID, USER_USERNAME, USER_PASSWORD, USER_ROLES, USER_TEAMS);


    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mvc = MockMvcBuilders.standaloneSetup(adminController)
                .setControllerAdvice(new DefaultControllerAdvice())
                .addFilter(new CorsFilter())
                .build();
    }

    @Test
    public void getUsers() throws Exception {
        when(userService.getUsers()).thenReturn(Arrays.asList(USER));
        mvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is("test-id")))
                .andExpect(jsonPath("$[0].username", is("test")))
                .andExpect(jsonPath("$[0].password", is(USER_PASSWORD)))
                .andExpect(jsonPath("$[0].roles", hasSize(2)))
                .andExpect(jsonPath("$[0].roles[0]", is("ROLE_USER")))
                .andExpect(jsonPath("$[0].roles[1]", is("ROLE_ADMIN")))
                .andExpect(jsonPath("$[0].teams", hasSize(1)))
                .andExpect(jsonPath("$[0].teams[0]", is("TeamTest")));
        verify(userService, times(1)).getUsers();
    }

    private GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");

//    https://eliux.github.io/java/spring/testing/how-to-mock-authentication-in-spring/
    @Test
//    @WithMockUser(username = "user", roles = {"ADMIN"})
    @WithUserDetails("admin")
    public void createUser_success() throws Exception {
        when(userService.createUser(USER)).thenReturn(RequestResponse.success("User is created!"));
        mvc.perform(post("/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(USER)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User is created!")));
    }

    @Test
    @WithUserDetails("admin")
    public void deleteUser_success() throws Exception {
        when(userService.deleteUser(anyString())).thenReturn(RequestResponse.success("User has been deleted!"));
        mvc.perform(delete("/admin/users/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("User has been deleted!")));
    }
}
