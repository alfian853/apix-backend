package com.future.apix.service;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.future.apix.entity.User;
import com.future.apix.repository.UserRepository;
import com.future.apix.service.impl.MongoUserDetailsService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class MongoUserDetailsServiceTest {
    @Mock
    UserRepository userRepository;

    @InjectMocks
    MongoUserDetailsService serviceMock;

    private static final String USER_ID = "test-id";
    private static final String USER_USERNAME = "test";
    private static final String USER_PASSWORD = new BCryptPasswordEncoder().encode("test");
    private static final List<String> USER_ROLES = new ArrayList<>(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
    private static final List<String> USER_TEAMS = new ArrayList<>(Arrays.asList("TeamTest"));
    private static final User USER = new User(USER_ID, USER_USERNAME, USER_PASSWORD, USER_ROLES, USER_TEAMS);

    /*
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    */
    @Test
    public void loadByUsername_failed(){
        try {
            serviceMock.loadUserByUsername(USER_USERNAME);
        } catch (UsernameNotFoundException e){
            Assert.assertEquals("Username: " + USER_USERNAME + " not found!", e.getMessage());
        }
    }

    @Test
    public void loadByUsername_success(){
        Mockito.when(userRepository.findByUsername(USER_USERNAME)).thenReturn(USER);
        UserDetails user = serviceMock.loadUserByUsername(USER_USERNAME);
        Assert.assertEquals(USER, user);
    }

}
