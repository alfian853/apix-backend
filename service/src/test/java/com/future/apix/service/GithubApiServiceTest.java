package com.future.apix.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.ProjectOasSwagger2;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ProjectRepository;
import com.future.apix.repository.OasSwagger2Repository;
import com.future.apix.request.GithubContentsRequest;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.github.GithubCommitResponse;
import com.future.apix.response.github.GithubContentResponse;
import com.future.apix.service.impl.GithubApiServiceImpl;
import com.future.apix.util.LazyObjectWrapper;
import com.future.apix.util.converter.SwaggerToApixOasConverter;
import org.junit.*;
import org.junit.runner.RunWith;
import org.kohsuke.github.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GithubApiServiceTest {
    //    https://github.com/kohsuke/github-api/blob/master/src/test/java/org/kohsuke/github/AbstractGitHubApiTestBase.java
    //    https://github.com/kohsuke/github-api/blob/master/src/test/java/org/kohsuke/github/AppTest.java
    //    https://github.com/kohsuke/github-api/blob/master/src/test/java/org/kohsuke/github/GitHubTest.java
    @Spy
    @InjectMocks
    GithubApiServiceImpl service;

    @Mock
    LazyObjectWrapper<GitHub> gitHub;

    @Mock
    ProjectRepository apiRepository;

    @Mock
    OasSwagger2Repository oasRepository;

    @Mock
    SwaggerToApixOasConverter converter;

    @Mock
    CommandExecutorService commandExecutor;

    @Spy
    ObjectMapper mapper;

    private ApiProject project;

    @Before
    public void setUp() throws Exception {
        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        project = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
    }

    @Test
    public void getMyselfTest() throws IOException {
        GHMyself ghMyself = new GHMyself();
        when(gitHub.get()).thenReturn(mock(GitHub.class));
        when(gitHub.get().getMyself()).thenReturn(ghMyself);
        service.getMyself();
        verify(gitHub.get()).getMyself();
    }

    @Test
    @Ignore
    public void getMyselfRepositoriesTest()throws IOException{
        /*
        GHRepository ghRepository = mock(GHRepository.class);
//        when(ghRepository.getName()).thenReturn("myName");
        doReturn(new Long(2)).when(ghRepository).getId();

        when(gitHub.getMyself()).thenReturn(mock(GHMyself.class));
        PagedIterable<GHRepository> repositories = mock(PagedIterable.class);
        when(gitHub.getMyself().listRepositories()).thenReturn(repositories);
        when(repositories.iterator()).thenReturn(mock(PagedIterator.class));

        when(repositories.iterator().next()).thenReturn(ghRepository);
        when(repositories.iterator().hasNext()).then(new Answer<Object>() {
            int counter = 2;
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                if(counter > 0){
                    counter--;
                    return true;
                }
                return false;
            }
        });
        service.getMyselfRepositories();

         */
        GHRepository repository = new GHRepository();
        //        GHMyself myself = new GHMyself();
        when(gitHub.get()).thenReturn(mock(GitHub.class));
        when(gitHub.get().getMyself()).thenReturn(mock(GHMyself.class));
        PagedIterable<GHRepository> repositories = mock(PagedIterable.class);
        doReturn(repositories).when(gitHub.get()).getMyself().listRepositories();

        service.getMyselfRepositories();
        verify(gitHub.get()).getMyself();
        verify(gitHub.get()).getMyself().listRepositories();
    }

    @Test
    public void getBranchesTest() throws IOException {
        mockRepository();
        Map<String, GHBranch> branches = new HashMap<>();
        branches.put("dev", new GHBranch());
        when(gitHub.get().getRepository(anyString()).getBranches()).thenReturn(branches);
        List<String> result = service.getBranches("owner/repo");
        verify(gitHub.get().getRepository(anyString())).getBranches();
        Assert.assertEquals(result.get(0), "dev");
    }

    @Test
    public void getFilesTest() throws IOException {
//        List<String> files = Arrays.asList("file0.txt", "file1.txt");
        GHTreeEntry treeEntry = mock(GHTreeEntry.class);
        mockRepository();
        GHTree tree = mock(GHTree.class);
        when(gitHub.get().getRepository(anyString()).getTree(anyString())).thenReturn(tree);
        when(gitHub.get().getRepository(anyString()).getTree(anyString()).getTree())
                .thenReturn(Collections.singletonList(treeEntry));
        when(treeEntry.getPath()).thenReturn("README.md");
        List<String> result = service.getFiles("owner/repo", "master");
        verify(gitHub.get().getRepository(anyString()).getTree(anyString())).getTree();
        Assert.assertEquals(result.get(0), "README.md");
    }

    @Test
    public void getFileContent_Success() throws IOException {
        mockRepository();
        GHContent ghContent = mock(GHContent.class);
        when(gitHub.get().getRepository(anyString()).getFileContent(anyString(), anyString()))
            .thenReturn(ghContent);
        when(ghContent.isFile()).thenReturn(true);
        when(ghContent.getOwner()).thenReturn(mock(GHRepository.class));
        when(ghContent.getOwner().getName()).thenReturn("owner");
        String contentString = "{\"swagger\" : \"2.0\",\"host\" : \"petstore.swagger.io\"}";
        InputStream is = new ByteArrayInputStream(contentString.getBytes(StandardCharsets.UTF_8));
        when(ghContent.read()).thenReturn(is);
        GithubContentResponse response = service.getFileContent("owner/repo", "test.md", "master");
        verify(gitHub.get().getRepository(anyString())).getFileContent(anyString(), anyString());
        Assert.assertEquals(response.getContent(), contentString);
    }

    @Test
    public void getFileContent_NotFound() throws IOException {
        mockRepository();
        GHContent ghContent = mock(GHContent.class);
        when(gitHub.get().getRepository(anyString()).getFileContent(anyString(), anyString()))
            .thenReturn(ghContent);
        try {
            service.getFileContent("owner/repo", "test.md", "master");
        } catch (DataNotFoundException e) {
            Assert.assertEquals(e.getMessage(), "File is not available!");
        }
    }

    @Test
    public void updateFile_NotFound() throws IOException {
        mockRepository();
        GHContent ghContent = mock(GHContent.class);
        when(gitHub.get().getRepository(anyString()).getFileContent(anyString(), anyString()))
            .thenReturn(ghContent);
        GithubContentsRequest request = new GithubContentsRequest();
        request.setMessage("commit message");
        request.setProjectId("123");
        request.setSha("sha-123");
        try {
            service.updateFile("owner/repo", "test.md", request);
        } catch (InvalidRequestException e) {
            Assert.assertEquals(e.getMessage(), "Content is not a file!");
        }
    }

    @Test
    public void updateFile_contentIsAlreadyEqual() throws IOException {
        mockRepository();
        GHContent ghContent = mock(GHContent.class);
        when(gitHub.get().getRepository(anyString()).getFileContent(anyString(), anyString()))
            .thenReturn(ghContent);
        when(ghContent.isFile()).thenReturn(true);
        String contentString = "{\"swagger\" : \"2.0\",\"host\" : \"petstore.swagger.io\"}";
        InputStream is = new ByteArrayInputStream(contentString.getBytes(StandardCharsets.UTF_8));
        when(ghContent.read()).thenReturn(is);
        ProjectOasSwagger2 swagger2 = new ProjectOasSwagger2();
        swagger2.setOasFileName("oas-file-name");
        when(oasRepository.findProjectOasSwagger2ByProjectId(anyString())).thenReturn(Optional.of(swagger2));
        doReturn(contentString).when(service).readFromFile(any());

        GithubContentsRequest request = new GithubContentsRequest();
        request.setMessage("message"); request.setSha("sha"); request.setProjectId("123");
        try {
            service.updateFile("owner/repo", "test.txt", request);
        } catch (InvalidRequestException e) {
            Assert.assertEquals(e.getMessage(), "Content of OAS in Github is already equal");
        }
    }

    @Test
    public void updateFile_contentIsReplaced() throws IOException {
        mockRepository();
        GHContent ghContent = mock(GHContent.class);
        when(gitHub.get().getRepository(anyString()).getFileContent(anyString(), anyString()))
            .thenReturn(ghContent);
        when(ghContent.isFile()).thenReturn(true);
        String contentString = "{\"swagger\" : \"2.0\",\"host\" : \"petstore.swagger.io\"}";
        String differString = "{\"swagger\" : \"3.0\",\"host\" : \"test.test.com\"}";
        InputStream is = new ByteArrayInputStream(contentString.getBytes(StandardCharsets.UTF_8));
        when(ghContent.read()).thenReturn(is);
        ProjectOasSwagger2 swagger2 = new ProjectOasSwagger2();
        swagger2.setOasFileName("oas-file-name");
        when(oasRepository.findProjectOasSwagger2ByProjectId(anyString())).thenReturn(Optional.of(swagger2));
        doReturn(differString).when(service).readFromFile(any());

        GithubContentsRequest request = new GithubContentsRequest();
        request.setMessage("message"); request.setSha("sha"); request.setProjectId("123");
        GHContentUpdateResponse updateResponse = mock(GHContentUpdateResponse.class);
        when(ghContent.update(anyString(), anyString(), anyString())).thenReturn(updateResponse);
        GHCommit commit = mock(GHCommit.class);
        GHCommit.ShortInfo shortInfo = mock(GHCommit.ShortInfo.class);
        when(updateResponse.getCommit()).thenReturn(commit);
        when(commit.getCommitShortInfo()).thenReturn(shortInfo);
        when(shortInfo.getMessage()).thenReturn("Commit Message");
        GithubCommitResponse response = service.updateFile("owner/repo", "test.txt", request);
        Assert.assertEquals(response.getMessage(), "Commit Message");

    }

    @Test
    public void pullFileContent_ContentIsNotFile() throws IOException {
        mockRepository();
        GHContent ghContent = mock(GHContent.class);
        when(gitHub.get().getRepository(anyString()).getFileContent(anyString(), anyString()))
            .thenReturn(ghContent);
        try {
            service.pullFileContent("owner/repo", "test.txt", "master", "123");
        } catch (InvalidRequestException e) {
            Assert.assertEquals(e.getMessage(), "Content is not a file!");
        }
    }

    @Test
    public void pullFileContent_ApiRepoNotFound() throws IOException {
        mockRepository();
        GHContent ghContent = mock(GHContent.class);
        when(gitHub.get().getRepository(anyString()).getFileContent(anyString(), anyString()))
            .thenReturn(ghContent);
        when(ghContent.isFile()).thenReturn(true);
        when(ghContent.getOwner()).thenReturn(mock(GHRepository.class));
        when(ghContent.getOwner().getName()).thenReturn("owner");
        String contentString = "{\"swagger\" : \"2.0\",\"host\" : \"petstore.swagger.io\"}";
        InputStream is = new ByteArrayInputStream(contentString.getBytes(StandardCharsets.UTF_8));
        when(ghContent.read()).thenReturn(is);
        when(converter.convert(any())).thenReturn(project);
        try {
            service.pullFileContent("owner/repo", "test.txt", "master", "123");
        } catch (DataNotFoundException e) {
            Assert.assertEquals(e.getMessage(), "Project is not found!");
        }
    }

    @Test
    public void pullFileContent_Success() throws IOException {
        mockRepository();
        GHContent ghContent = mock(GHContent.class);
        when(gitHub.get().getRepository(anyString()).getFileContent(anyString(), anyString()))
            .thenReturn(ghContent);
        when(ghContent.isFile()).thenReturn(true);
        when(ghContent.getOwner()).thenReturn(mock(GHRepository.class));
        when(ghContent.getOwner().getName()).thenReturn("owner");
        String contentString = "{\"swagger\" : \"2.0\",\"host\" : \"petstore.swagger.io\"}";
        //        HashMap<String, >
        InputStream is = new ByteArrayInputStream(contentString.getBytes(StandardCharsets.UTF_8));
        when(ghContent.read()).thenReturn(is);
        //        ApiProject mockProject = mapper.convertValue(contentString, ApiProject.class);
        when(converter.convert(any())).thenReturn(new ApiProject());
        when(apiRepository.findById(anyString())).thenReturn(Optional.of(project));
        when(apiRepository.save(any())).thenReturn(project);

        ProjectCreateResponse response = service.pullFileContent("owner/repo", "test.txt", "master", "123");
        Assert.assertTrue(response.getSuccess());
        Assert.assertEquals(response.getMessage(), "Project from github successfully pulled!");
        Assert.assertEquals(response.getProjectId(), "123");
    }

    private void mockRepository() throws IOException {
        when(gitHub.get()).thenReturn(mock(GitHub.class));
        when(gitHub.get().getRepository(anyString())).thenReturn(mock(GHRepository.class));
    }
    private void mockContent(){

    }
}
