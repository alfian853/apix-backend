package com.future.apix.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.config.filter.CorsFilter;
import com.future.apix.controller.controlleradvice.DefaultControllerAdvice;
import com.future.apix.request.GithubContentsRequest;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.github.GithubCommitResponse;
import com.future.apix.response.github.GithubContentResponse;
import com.future.apix.response.github.GithubRepoResponse;
import com.future.apix.response.github.GithubUserResponse;
import com.future.apix.service.GithubApiService;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class GithubApiControllerTest {
    private MockMvc mvc;

    @Mock
    private GithubApiService apiService;

    @InjectMocks
    private GithubApiController apiController;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mvc = MockMvcBuilders.standaloneSetup(apiController)
            .setControllerAdvice(new DefaultControllerAdvice())
            .addFilter(new CorsFilter())
            .build();
    }

    @Test
    public void getMyself() throws Exception {
        GithubUserResponse response = new GithubUserResponse();
        Date date = new Date();
        response.setLogin("johndoe");
        response.setId(123);
        response.setName("John Doe");
        response.setEmail("john@doe.com");
        when(apiService.getMyself()).thenReturn(response);

        mvc.perform(get("/github/api/user"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login", is("johndoe")))
            .andExpect(jsonPath("$.id", is(123)))
            .andExpect(jsonPath("$.name", is("John Doe")))
            .andExpect(jsonPath("$.email", is("john@doe.com")));
        verify(apiService, times(1)).getMyself();
    }

    @Test
    public void getMyselfRepositories() throws Exception {
        GithubRepoResponse res = new GithubRepoResponse();
        res.setId(123);
        res.setName("Apix");
        res.setFullName("johndoe/Apix");
        res.setDescription("John Apix");
        res.setOwnerName("johndoe");
        when(apiService.getMyselfRepositories()).thenReturn(Arrays.asList(res));
        mvc.perform(get("/github/api/user/repos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(123)))
            .andExpect(jsonPath("$[0].name", is("Apix")))
            .andExpect(jsonPath("$[0].fullName", is("johndoe/Apix")))
            .andExpect(jsonPath("$[0].description", is("John Apix")))
            .andExpect(jsonPath("$[0].ownerName", is("johndoe")));
        verify(apiService, times(1)).getMyselfRepositories();
    }

    @Test
    public void getBranches() throws Exception {
        when(apiService.getBranches(anyString())).thenReturn(Arrays.asList("branch0", "branch1"));
        mvc.perform(get("/github/api/repos/{owner}/{repo}/branches", "owner", "repo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0]", is("branch0")))
            .andExpect(jsonPath("$[1]", is("branch1")));
        verify(apiService, times(1)).getBranches(anyString());
    }

    @Test
    public void getFiles() throws Exception {
        when(apiService.getFiles(anyString(), anyString())).thenReturn(Arrays.asList("file0.txt", "file1.txt"));
        mvc.perform(get("/github/api/repos/{owner}/{repo}/git/trees/{branch}", "owner", "repo", "branch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0]", is("file0.txt")))
                .andExpect(jsonPath("$[1]", is("file1.txt")));
        verify(apiService, times(1)).getFiles(anyString(), anyString());
    }

    @Test
    public void getFileContent() throws Exception {
        GithubContentResponse res = new GithubContentResponse();
        res.setType("file"); res.setEncoding("base64"); res.setSize(123);
        res.setName("README.md"); res.setRepoName("Apix"); res.setSha("sha");
        res.setPath("README.md"); res.setContent("content");
        when(apiService.getFileContent(anyString(), anyString(), anyString())).thenReturn(res);

        mvc.perform(get("/github/api/repos/{owner}/{repo}/contents/{path}", "johndoe", "Apix", "README.md")
            .param("ref", "master"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.type", is("file")))
            .andExpect(jsonPath("$.encoding", is("base64")))
            .andExpect(jsonPath("$.size", is(123)))
            .andExpect(jsonPath("$.name", is("README.md")))
            .andExpect(jsonPath("$.repoName", is("Apix")))
            .andExpect(jsonPath("$.sha", is("sha")))
            .andExpect(jsonPath("$.path", is("README.md")))
            .andExpect(jsonPath("$.content", is("content")));
        verify(apiService, times(1)).getFileContent(anyString(), anyString(), anyString());
    }

    @Test
    public void pullFileContent() throws Exception {
        ProjectCreateResponse response = new ProjectCreateResponse();
        response.setStatusToSuccess(); response.setMessage("Project from github successfully pulled!");
        response.setProjectId("project-id");
        when(apiService.pullFileContent(anyString(), anyString(), anyString(), anyString())).thenReturn(response);

        mvc.perform(post("/github/api/repos/{owner}/{repo}/contents/{path}", "johndoe", "Apix", "README.md")
            .param("ref", "master")
            .param("projectId", "project-id"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.message", is("Project from github successfully pulled!")))
            .andExpect(jsonPath("$.new_project", is("project-id")));
        verify(apiService, times(1)).pullFileContent(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    public void updateFileContent() throws Exception {
        String message = "commit message";
        String sha = "sha";
        GithubContentsRequest req = new GithubContentsRequest();
        req.setMessage(message); req.setSha(sha);
        req.setProjectId("id"); req.setBranch("branch");
        GithubCommitResponse res = new GithubCommitResponse();
        res.setSha(sha); res.setMessage(message);

        when(apiService.updateFile(anyString(), anyString(), ArgumentMatchers.any())).thenReturn(res);

        mvc.perform(put("/github/api/repos/{owner}/{repo}/contents/{path}", "johndoe", "Apix", "README.md")
            .contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.sha", is("sha")))
            .andExpect(jsonPath("$.message", is("commit message")));
        verify(apiService, times(1)).updateFile(anyString(), anyString(), ArgumentMatchers.any());
    }

}
