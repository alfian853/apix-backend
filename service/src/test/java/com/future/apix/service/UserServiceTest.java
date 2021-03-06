package com.future.apix.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.Team;
import com.future.apix.entity.User;
import com.future.apix.entity.enumeration.TeamAccess;
import com.future.apix.entity.teamdetail.Member;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.DuplicateEntryException;
import com.future.apix.exception.InvalidAuthenticationException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.TeamRepository;
import com.future.apix.repository.UserRepository;
import com.future.apix.request.UserCreateRequest;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.UserProfileResponse;
import com.future.apix.service.UserService;
import com.future.apix.service.impl.UserServiceImpl;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.validation.constraints.AssertFalse;
import java.util.*;

import static org.mockito.Mockito.*;

// Refer to Invenger / AccountServiceTest

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @InjectMocks
    UserServiceImpl serviceMock = new UserServiceImpl();

    private static final String USER_ID = "test-id";
    private static final String USER_USERNAME = "test";
    private static final String USER_PASSWORD = new BCryptPasswordEncoder().encode("test");
    private static final List<String> USER_ROLES = new ArrayList<>(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
    private static final List<String> USER_TEAMS = new ArrayList<>(Arrays.asList("TeamTest"));
    private static final User USER = new User(USER_ID, USER_USERNAME, USER_PASSWORD, USER_ROLES, USER_TEAMS);

    private static final List<Member> TEAM_MEMBER = new ArrayList<>(Arrays.asList(new Member(USER_USERNAME, true)));
    private static final Team TEAM = Team.builder()
            .id("team-id")
            .name("team-name")
            .access(TeamAccess.PUBLIC)
            .creator(USER_USERNAME)
            .members(TEAM_MEMBER)
            .build();

    private Optional<User> userOpt;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ObjectMapper oMapper;

    @Before
    public void setUp(){
        userOpt = Optional.of(USER);
    }

    /*
        public List<User> getUsers()
     */
    @Test
    public void getUsers_test(){
        List<User> users = Collections.singletonList(USER);
        when(userRepository.findAll()).thenReturn(users);
        List<User> returned = serviceMock.getUsers();
        Assert.assertEquals(users, returned);
        Assert.assertEquals(1, returned.size());
    }

    /*
        public RequestResponse deleteUser(String id)
     */
    @Test(expected = DataNotFoundException.class)
    public void deleteUser_idNotFound(){
        RequestResponse response = serviceMock.deleteUser("not-test-id");
        Assert.assertFalse(response.getSuccess());
        Assert.assertEquals("User does not exists!", response.getMessage());
    }

    @Test
    public void deleteUser_success(){
        when(userRepository.findById(anyString())).thenReturn(userOpt);
        when(teamRepository.findByMembersUsername(anyString())).thenReturn(Collections.singletonList(TEAM));
        RequestResponse response = serviceMock.deleteUser("test-id");
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("User has been deleted!", response.getMessage());
    }

    /*
        public UserProfileResponse userProfile (Authentication authentication)
     */
    @Test
    public void userProfile_success(){
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(USER);
        UserProfileResponse expected = new UserProfileResponse();
        expected.setStatusToSuccess(); expected.setMessage("User is authenticated");
        expected.setUsername(USER_USERNAME); expected.setRoles(USER_ROLES); expected.setTeams(USER_TEAMS);
        when(oMapper.convertValue(Mockito.any(), eq(UserProfileResponse.class))).thenReturn(expected);
        UserProfileResponse response = serviceMock.userProfile(authentication);
        Assert.assertNotNull(response);
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("User is authenticated!", response.getMessage());
        Assert.assertEquals("test", response.getUsername());
        Assert.assertEquals(Arrays.asList("ROLE_USER", "ROLE_ADMIN"), response.getRoles());
        Assert.assertEquals(Arrays.asList("TeamTest"), response.getTeams());
    }

    @Test
    public void userProfile_authenticationNull(){
        try {
            serviceMock.userProfile(null);
        } catch (InvalidAuthenticationException e){
            Assert.assertEquals("User is not authenticated!", e.getMessage());
        }
    }

    /*
        public RequestResponse createUser(User user)
     */

    @Test
    public void createUser_userAlreadyExist(){
        when(userRepository.findByUsername(anyString())).thenReturn(USER);
        UserCreateRequest request = new UserCreateRequest("test", "password", "password", USER_ROLES);
        try {
            serviceMock.createUser(request);
        } catch (DuplicateEntryException e) {
            Assert.assertEquals("Username is already exists!", e.getMessage());
        }
    }

    @Test
    public void createUser_success(){
        when(userRepository.findByUsername(anyString())).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(USER);
        UserCreateRequest request = new UserCreateRequest("username", "password", "password", USER_ROLES);
        RequestResponse response = serviceMock.createUser(request);
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("User is created!", response.getMessage());
    }

    @Test
    public void createUser_passwordNotMatch(){
        UserCreateRequest request = new UserCreateRequest("username", "password", "not-password", USER_ROLES);
        try {
            serviceMock.createUser(request);
        } catch (InvalidRequestException e) {
            Assert.assertEquals("Password does not match!", e.getMessage());
        }
    }

    @Test
    public void createUser_userLengthNotSufficient(){
        UserCreateRequest request = new UserCreateRequest("me", "password", "password", USER_ROLES);
        try {
            serviceMock.createUser(request);
        } catch (InvalidRequestException e) {
            Assert.assertEquals("Username is too short!", e.getMessage());
        }
    }

    /*
        User getUser(String username)
     */
    @Test
    public void getUser_fail(){
        try {
            serviceMock.getUser(USER_USERNAME);
        } catch (DataNotFoundException e) {
            Assert.assertEquals("User is not registered!", e.getMessage());
        }
    }

    @Test
    public void getUser_success(){
        when(userRepository.findByUsername(anyString())).thenReturn(USER);
        User user = serviceMock.getUser(USER_USERNAME);
        Assert.assertEquals(USER_ID, user.getId());
        Assert.assertEquals(USER_USERNAME, user.getUsername());
        Assert.assertEquals(USER_PASSWORD, user.getPassword());
        Assert.assertEquals(USER_ROLES, user.getRoles());
        Assert.assertEquals(USER_TEAMS, user.getTeams());
    }


}
