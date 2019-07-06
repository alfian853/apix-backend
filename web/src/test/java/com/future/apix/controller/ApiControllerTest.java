package com.future.apix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.config.filter.CorsFilter;
import com.future.apix.controlleradvice.DefaultControllerAdvice;
import com.future.apix.entity.ApiProject;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.request.ProjectCreateRequest;
import com.future.apix.response.DownloadResponse;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.ProjectUpdateResponse;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.ApiDataService;
import com.future.apix.service.ApiDataUpdateService;
import com.future.apix.service.CommandExecutorService;
import com.future.apix.service.command.Swagger2ExportCommand;
import com.future.apix.service.command.Swagger2ImportCommand;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class ApiControllerTest {
    @Autowired
    private MockMvc mvc;

    @Mock
    private ApiDataService apiDataService;

    @Mock
    private ApiDataUpdateService updateService;

    @Mock
    private CommandExecutorService commandExecutor;

    @Spy
    private ObjectMapper mapper;

    @InjectMocks
    private ApiController apiController;

    private ApiProject project;
    private Optional<ApiProject> optionalApiProject;

    @Before
    public void init() throws IOException, URISyntaxException {
        MockitoAnnotations.initMocks(this);
        mvc = MockMvcBuilders.standaloneSetup(apiController)
                .setControllerAdvice(new DefaultControllerAdvice())
                .addFilter(new CorsFilter())
                .build();

        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        project = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
        optionalApiProject = Optional.of(project);
    }

    @Test
    public void importFromFile_success() throws Exception {
        when(commandExecutor.execute(any(), any())).thenReturn(project);
        MockMultipartFile jsonFile = new MockMultipartFile("json", "", "application/json", "{\"json\": \"someValue\"}".getBytes());
        mvc.perform(multipart("/projects/import")
                .file("file", jsonFile.getBytes())
                .param("type", "oas-swagger2")
                .param("team", "team"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("")));
    }

    @Test
    public void importFromFile_isNull() throws Exception {
        when(commandExecutor.execute(any(), any())).thenReturn(null);
        MockMultipartFile jsonFile = new MockMultipartFile("json", "", "application/json", "{\"json\": \"someValue\"}".getBytes());
        mvc.perform(multipart("/projects/import")
                .file("file", jsonFile.getBytes())
                .param("type", "oas-swagger2")
                .param("team", "team"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("")));
    }

    @Test
    public void importFromFile_typeNotEqual() throws Exception {
        when(commandExecutor.execute(any(), any())).thenReturn(new ApiProject());
        MockMultipartFile jsonFile = new MockMultipartFile("json", "", "application/json", "{\"json\": \"someValue\"}".getBytes());
        mvc.perform(multipart("/projects/import")
                .file("file", jsonFile.getBytes())
                .param("type", "not-oas")
                .param("team", "team"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("oas format is not supported")));
        verify(commandExecutor, times(0)).execute(any(),any());

    }

    @Test
    public void exportToOas_success() throws Exception {
        DownloadResponse response = new DownloadResponse();
        response.setStatusToSuccess();
        response.setMessage("File has been exported!");
        response.setFileUrl("url");
        when(commandExecutor.execute(any(), any())).thenReturn(response);
        mvc.perform(post("/projects/{id}/export", 1)
        .param("type", "oas-swagger2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("File has been exported!")))
                .andExpect(jsonPath("$.file_url", is("url")));
        verify(commandExecutor, times(1)).execute(any(),any());
    }

    @Test
    public void exportToOas_typeNotEqual() throws Exception {
        when(commandExecutor.execute(any(), any())).thenReturn(new DownloadResponse());
        mvc.perform(post("/projects/{id}/export", 1)
        .param("type", "not-oas"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("oas format is not supported")));
        verify(commandExecutor, times(0)).execute(any(),any());

    }

    @Test
    public void getById_success() throws Exception {
        when(apiDataService.findById(anyString())).thenReturn(project);
        mvc.perform(get("/projects/{id}", 1))
                .andExpect(status().isOk());
    }

    @Test
    public void getById_failed() throws Exception {
        when(apiDataService.findById(anyString())).thenThrow(new DataNotFoundException("Project does not exists!"));
        mvc.perform(get("/projects/{id}", 1))
                .andExpect(status().isNotFound());
    }

    @Test
    public void doApiDataQuery_success() throws Exception {
        URI uri = getClass().getClassLoader().getResource("success-query.json").toURI();
        HashMap<String,Object> query = mapper.readValue(Files.readAllBytes(Paths.get(uri)), HashMap.class);

        ProjectUpdateResponse response = new ProjectUpdateResponse();
        response.setStatusToSuccess();
        response.setNewSignature("_signature");
        when(updateService.doQuery(anyString(), any())).thenReturn(response);
        mvc.perform(put("/projects/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(query)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("")))
                .andExpect(jsonPath("$.new_signature", is("_signature")));

    }

    @Test
    public void findAllProjects_test() throws Exception {
        when(apiDataService.findAllProjects()).thenReturn(Arrays.asList(project));
        mvc.perform(get("/projects/all/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        verify(apiDataService, times(1)).findAllProjects();
    }

    @Test
    public void deleteById_success() throws Exception {
        when(apiDataService.deleteById(anyString())).thenReturn(RequestResponse.success("Project has been deleted!"));
        mvc.perform(delete("/projects/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Project has been deleted!")));
        verify(apiDataService, times(1)).deleteById(anyString());
    }

    @Test
    public void createProject_test() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setHost("host"); request.setBasePath("/base");
        request.setTeam("team");
        ProjectCreateResponse response = new ProjectCreateResponse();
        response.setStatusToSuccess(); response.setMessage("Project has been created!");
        response.setProjectId("1");
        when(apiDataService.createProject(request)).thenReturn(response);
        mvc.perform(post("/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Project has been created!")))
                .andExpect(jsonPath("$.new_project", is("1")));
        verify(apiDataService, times(1)).createProject(any());
    }

    @Test
    public void getCodegen() throws Exception {
        DownloadResponse response = new DownloadResponse();
        response.setStatusToSuccess();
        response.setFileUrl("url");
        when(commandExecutor.execute(any(), any())).thenReturn(response);
        mvc.perform(get("/projects/{id}/codegen",1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("")))
                .andExpect(jsonPath("$.file_url", is("url")));
        verify(commandExecutor, times(1)).execute(any(),any());
    }

    @Test
    public void assignTeamToProject() throws Exception {
        when(apiDataService.grantTeamAccess(anyString(), anyString())).thenReturn(RequestResponse.success("Team has been assigned to project!"));
        mvc.perform(post("/projects/{id}/assign", 1)
                .param("teamName", "team"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.message", is("Team has been assigned to project!")));
        verify(apiDataService, times(1)).grantTeamAccess(anyString(), anyString());
    }

}
