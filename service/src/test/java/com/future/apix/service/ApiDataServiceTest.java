package com.future.apix.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.Team;
import com.future.apix.entity.User;
import com.future.apix.entity.enumeration.TeamAccess;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.repository.ProjectRepository;
import com.future.apix.repository.enums.ProjectField;
import com.future.apix.repository.request.ProjectAdvancedQuery;
import com.future.apix.request.ProjectCreateRequest;
import com.future.apix.response.*;
import com.future.apix.service.impl.ApiDataServiceImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ApiDataServiceTest {

    @InjectMocks
    ApiDataServiceImpl apiService;

    @Spy
    ObjectMapper mapper;

    @Mock
    ProjectRepository apiRepository;

    @Mock
    TeamService teamService;

    private Optional<ApiProject> optionalApiProject;
    private ApiProject project;

    private static final Team TEAM = Team.builder()
        .id("test-id")
        .name("teamTest")
        .access(TeamAccess.PUBLIC)
        .creator("test")
        .build();

    private static final String USER_ID = "test-id";
    private static final String USER_USERNAME = "test";
    private static final String USER_PASSWORD = new BCryptPasswordEncoder().encode("test");
    private static final List<String> USER_ROLES = new ArrayList<>(Arrays.asList("ROLE_USER", "ROLE_ADMIN"));
    private static final List<String> USER_TEAMS = new ArrayList<>(Arrays.asList("TeamTest"));
    private static final User USER = new User(USER_ID, USER_USERNAME, USER_PASSWORD, USER_ROLES, USER_TEAMS);

    private static final ProjectDto PROJECT_DTO = ProjectDto.builder()
        .id(null)
        .host("petstore.swagger.io")
        .title("Swagger Petstore")
        .owner("creator")
        .githubUsername("")
        .repository("")
        .build();

    private static final PagedResponse<ProjectDto> PAGED_DTO = PagedResponse.<ProjectDto>builder()
        .totalPages(1)
        .pageSize(1)
        .pageNumber(1)
        .numberOfElements(1)
        .totalElements(1L)
        .offset(0L)
        .first(true)
        .last(true)
        .contents(Arrays.asList(PROJECT_DTO))
        .build();

    @Before
    public void init() throws IOException, URISyntaxException {
        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        project = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
        optionalApiProject = Optional.of(project);
    }

    /*
        public ApiProject findById(String id)
     */

    @Test
    public void findById_NotFound(){
        try {
            apiService.findById("test-id");
        } catch (DataNotFoundException e) {
            Assert.assertEquals("Project does not exists!", e.getMessage());
        }
    }

    @Test
    public void findById_Found(){
        when(apiRepository.findById(anyString())).thenReturn(optionalApiProject);
        ApiProject expected = apiService.findById("test-id");
        Assert.assertEquals(expected, project);
    }

    /*
        public RequestResponse deleteById(String id)
     */
    @Test
    public void deleteById_NotFound(){
        try {
            apiService.deleteById("test-id");
        } catch (DataNotFoundException e){
            Assert.assertEquals("Project does not exists!", e.getMessage());
        }
    }

    @Test
    public void deleteById_Found(){
        when(apiRepository.findById(anyString())).thenReturn(optionalApiProject);
        RequestResponse response = apiService.deleteById("test-id");
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("Project has been deleted!", response.getMessage());
    }

    @Test
    public void createProject_success_teamExists() {
        ProjectCreateRequest request = ProjectCreateRequest.builder()
            .basePath("/v2")
            .host("petstore.swagger.io")
            .info(
                ProjectCreateRequest.ProjectInfo.builder()
                    .title("Petstore API")
                    .version("1.0.0")
                    //                        .termsOfService("http://swagger.io/terms/")
                    //                        .description("This is a sample server Petstore server.  You can find out more about Swagger at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).  For this sample, you can use the api key `special-key` to test the authorization filters.")
                    .build()
            )
            .isNewTeam(false)
            .team("TeamTest")
            .build();
        when(teamService.getTeamByName(anyString())).thenReturn(TEAM);
        when(apiRepository.save(any(ApiProject.class))).thenReturn(project);
        ProjectCreateResponse response = apiService.createProject(request);
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("Project has been created!", response.getMessage());
    }

    @Test
    public void createProject_success_teamNotExists() {
        ProjectCreateRequest request = ProjectCreateRequest.builder()
            .basePath("/v2")
            .host("petstore.swagger.io")
            .info(
                ProjectCreateRequest.ProjectInfo.builder()
                    .title("Petstore API")
                    .version("1.0.0")
                    .build()
            )
            .isNewTeam(true)
            .team("TeamTest")
            .build();

        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).thenReturn(USER);

        UserProfileResponse expected = new UserProfileResponse();
        expected.setStatusToSuccess(); expected.setMessage("User is authenticated");
        expected.setUsername(USER_USERNAME); expected.setRoles(USER_ROLES); expected.setTeams(USER_TEAMS);
//        when(mapper.convertValue(any(), eq(UserProfileResponse.class))).thenReturn(expected);
//        doReturn(expected).when(mapper).convertValue(any(), eq(UserProfileResponse.class));

        when(teamService.createTeam(any())).thenReturn(TEAM);
        when(apiRepository.save(any(ApiProject.class))).thenReturn(project);
        ProjectCreateResponse response = apiService.createProject(request);
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("Project has been created!", response.getMessage());
    }

    @Test
    public void getByQuery() {
        Pageable pageable = mock(Pageable.class);
        when(pageable.getOffset()).thenReturn(new Long(0));
        when(apiRepository.findByQuery(any())).thenReturn(new PageImpl<ApiProject>(Arrays.asList(project)){
            @Override
            public Pageable getPageable() {
                return pageable;
            }
        });
        ProjectAdvancedQuery request = ProjectAdvancedQuery.builder()
            .page(0)
            .size(10)
            .sortBy(ProjectField.UPDATED_AT)
            .direction(Sort.Direction.DESC)
            .search("")
            .build();
        PagedResponse<ProjectDto> response = apiService.getByQuery(request);
        Assert.assertEquals(response.getTotalElements(), new Long(1));
        Assert.assertEquals(response.getTotalPages(), new Integer(1));
        Assert.assertEquals(response.getPageSize(), new Integer(0));
        Assert.assertEquals(response.getContents(), Collections.singletonList(PROJECT_DTO));
    }

    @Test
    public void getByTeamAndQuery() {
        Pageable pageable = mock(Pageable.class);
        when(pageable.getOffset()).thenReturn(new Long(0));
        when(apiRepository.findByQuery(any(), any())).thenReturn(new PageImpl<ApiProject>(Arrays.asList(project)){
            @Override
            public Pageable getPageable() {
                return pageable;
            }
        });
        ProjectAdvancedQuery request = ProjectAdvancedQuery.builder()
            .page(0)
            .size(10)
            .sortBy(ProjectField.UPDATED_AT)
            .direction(Sort.Direction.DESC)
            .search("")
            .build();
        PagedResponse<ProjectDto> response = apiService.getByTeamAndQuery(request, TEAM.getName());
        Assert.assertEquals(response.getTotalElements(), new Long(1));
        Assert.assertEquals(response.getTotalPages(), new Integer(1));
        Assert.assertEquals(response.getPageSize(), new Integer(0));
        Assert.assertEquals(response.getContents(), Collections.singletonList(PROJECT_DTO));
    }

}
