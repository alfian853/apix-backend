package com.future.apix.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.exception.ConflictException;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.request.ProjectCreateRequest;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.ProjectUpdateResponse;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.impl.ApiDataServiceImpl;
import com.future.apix.service.impl.ApiDataUpdateImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ApiDataTest {

    @InjectMocks
    ApiDataServiceImpl serviceMock;

    @Spy
    ObjectMapper mapper;

    @Mock
    ApiRepository apiRepository;

    private Optional<ApiProject> optionalApiProject;
    private ApiProject project;


    @Before
    public void init() throws IOException, URISyntaxException {
        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        project = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
        optionalApiProject = Optional.of(project);

//        when(apiRepository.findById(anyString())).thenReturn(optionalApiProject);
    }

    /*
        public ApiProject findById(String id)
     */

    @Test
    public void findById_NotFound(){
        try {
            serviceMock.findById("test-id");
        } catch (DataNotFoundException e) {
            Assert.assertEquals("Project does not exists!", e.getMessage());
        }
    }

    @Test
    public void findById_Found(){
        when(apiRepository.findById(anyString())).thenReturn(optionalApiProject);
        ApiProject expected = serviceMock.findById("test-id");
        Assert.assertEquals(expected, project);
    }

    /*
        public List<ApiProject> findAllProject()
     */
    @Test
    public void findAllProjects_test(){
        when(apiRepository.findAllProjects()).thenReturn(Arrays.asList(project));
        List<ApiProject> projects = serviceMock.findAllProjects();
        Assert.assertEquals(Arrays.asList(project), projects);
    }

    /*
        public RequestResponse deleteById(String id)
     */
    @Test
    public void deleteById_NotFound(){
        try {
            serviceMock.deleteById("test-id");
        } catch (DataNotFoundException e){
            Assert.assertEquals("Project does not exists!", e.getMessage());
        }
    }

    @Test
    public void deleteById_Found(){
        when(apiRepository.findById(anyString())).thenReturn(optionalApiProject);
        RequestResponse response = serviceMock.deleteById("test-id");
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("Project has been deleted!", response.getMessage());
    }

    @Test
    public void createProject_success() throws URISyntaxException, IOException {
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
                .build();

//        URI uri = getClass().getClassLoader().getResource("apix-oas-create.json").toURI();
//        ApiProject projectTest = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);

        when(apiRepository.save(any(ApiProject.class))).thenReturn(project);
        ProjectCreateResponse response = serviceMock.createProject(request);
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals("Project has been created!", response.getMessage());
    }


    /*
        public List<ApiProject> findByUser(String teamName)
     */
    @Test
    public void findByUser(){
        when(apiRepository.findByTeamsIn(anyString())).thenReturn(Arrays.asList(project));
        List<ApiProject> projects = serviceMock.findByUser("teamTest");
        Assert.assertEquals(1, projects.size());
    }
}
