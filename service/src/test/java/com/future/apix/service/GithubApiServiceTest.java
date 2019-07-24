package com.future.apix.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.ProjectOasSwagger2;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.repository.OasSwagger2Repository;
import com.future.apix.request.GithubCommitterRequest;
import com.future.apix.request.GithubContentsRequest;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.github.GithubCommitResponse;
import com.future.apix.response.github.GithubContentResponse;
import com.future.apix.service.impl.GithubApiServiceImpl;
import com.future.apix.util.LazyObjectWrapper;
import com.future.apix.util.converter.SwaggerToApixOasConverter;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.egit.github.core.client.PageIterator;
import org.junit.*;
import org.junit.runner.RunWith;
import org.kohsuke.github.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.PageImpl;
import sun.nio.ch.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
    ApiRepository apiRepository;

    @Mock
    OasSwagger2Repository oasRepository;

    @Mock
    SwaggerToApixOasConverter converter;

    @Mock
    CommandExecutorService commandExecutor;

    @Spy
    ObjectMapper mapper;

    private ApiProject project;
    //    private ApiProject swagger;

    @Before
    public void setUp() throws Exception {
        //        File f = new File(System.getProperty("user.home"), ".github.kohsuke2");
        //        if (f.exists()) {
        //            Properties props = new Properties();
        //            FileInputStream in = null;
        //            try {
        //                in = new FileInputStream(f);
        //                props.load(in);
        //            } finally {
        //                IOUtils.closeQuietly(in);
        //            }
        //            // use the non-standard credential preferentially, so that developers of this library do not have
        //            // to clutter their event stream.
        //            gitHub = GitHubBuilder.fromProperties(props).withRateLimitHandler(RateLimitHandler.FAIL).build();
        //        } else {
        //            gitHub = GitHubBuilder.fromCredentials().withRateLimitHandler(RateLimitHandler.FAIL).build();
        //        }
        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        project = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);

        //        URI uri2 = getClass().getClassLoader().getResource("swagger-oas.json").toURI();
        //        swagger = mapper.readValue(Files.readAllBytes(Paths.get(uri2)), ApiProject.class);
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
        when(gitHub.get()).thenReturn(mock(GitHub.class));
        //        GHRepository repository = new GHRepository();
        when(gitHub.get().getRepository(anyString())).thenReturn(mock(GHRepository.class));
        Map<String, GHBranch> branches = new HashMap<>();
        branches.put("dev", new GHBranch());
        //        when(gitHub.get().getRepository(anyString()).getBranches()).thenReturn(mock(Map.class));
        when(gitHub.get().getRepository(anyString()).getBranches()).thenReturn(branches);
        //
        List<String> result = service.getBranches("owner/repo");
        //        verify(gitHub).get().getRepository(anyString());
        verify(gitHub.get().getRepository(anyString())).getBranches();
        Assert.assertEquals(result.get(0), "dev");
    }

    @Test
    public void getFileContent_Success() throws IOException {
        when(gitHub.get()).thenReturn(mock(GitHub.class));
        when(gitHub.get().getRepository(anyString())).thenReturn(mock(GHRepository.class));
        GHContent ghContent = mock(GHContent.class);
        //        when(ghContent.isFile()).thenReturn(true);
        when(gitHub.get().getRepository(anyString()).getFileContent(anyString(), anyString()))
            .thenReturn(ghContent);
        when(ghContent.isFile()).thenReturn(true);
        when(ghContent.getOwner()).thenReturn(mock(GHRepository.class));
        when(ghContent.getOwner().getName()).thenReturn("owner");
        String contentString = "{\"swagger\" : \"2.0\",\"host\" : \"petstore.swagger.io\"}";
        InputStream is = new ByteArrayInputStream(contentString.getBytes(StandardCharsets.UTF_8));
        when(ghContent.read()).thenReturn(is);
        //        when(ioUtils.toString(any(InputStream.class), anyString())).thenReturn(contentString);
        //        when(gitHub.get().getRepository(anyString()).getFileContent(anyString(), anyString()).isFile()).thenReturn(true);
        GithubContentResponse response = service.getFileContent("owner/repo", "test.md", "master");
        verify(gitHub.get().getRepository(anyString())).getFileContent(anyString(), anyString());
        Assert.assertEquals(response.getContent(), contentString);
    }

    @Test
    public void getFileContent_NotFound() throws IOException {
        when(gitHub.get()).thenReturn(mock(GitHub.class));
        when(gitHub.get().getRepository(anyString())).thenReturn(mock(GHRepository.class));
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
        when(gitHub.get()).thenReturn(mock(GitHub.class));
        when(gitHub.get().getRepository(anyString())).thenReturn(mock(GHRepository.class));
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
    //    @Ignore
    public void updateFile_contentIsAlreadyEqual() throws IOException {
        when(gitHub.get()).thenReturn(mock(GitHub.class));
        when(gitHub.get().getRepository(anyString())).thenReturn(mock(GHRepository.class));
        GHContent ghContent = mock(GHContent.class);
        when(gitHub.get().getRepository(anyString()).getFileContent(anyString(), anyString()))
            .thenReturn(ghContent);
        when(ghContent.isFile()).thenReturn(true);
        //        DigestUtils utils = mock(DigestUtils.class);
        String contentString = "{\"swagger\" : \"2.0\",\"host\" : \"petstore.swagger.io\"}";
        InputStream is = new ByteArrayInputStream(contentString.getBytes(StandardCharsets.UTF_8));
        when(ghContent.read()).thenReturn(is);
        //        when(utils.sha256Hex(any(InputStream.class))).thenReturn("sha");
        //        doReturn("sha").when(utils)
        //        when(utils.sha256Hex(anyByte())).thenReturn("sha");
        ProjectOasSwagger2 swagger2 = new ProjectOasSwagger2();
        swagger2.setOasFileName("oas-file-name");
        //        doReturn(any()).when(commandExecutor.executeCommand(any(), anyString()));
        when(oasRepository.findProjectOasSwagger2ByProjectId(anyString())).thenReturn(Optional.of(swagger2));
        //        Files files = mock(Files.class);
        //        doReturn(notNull()).when(files.lines(any(),any()));
        //        when(service.readFromFile(any())).thenReturn(contentString);
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
        when(gitHub.get()).thenReturn(mock(GitHub.class));
        when(gitHub.get().getRepository(anyString())).thenReturn(mock(GHRepository.class));
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
//        when(ghContent.update(anyString(), anyString(), anyString())).thenReturn(new GHContentUpdateResponse());
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
        when(gitHub.get()).thenReturn(mock(GitHub.class));
        when(gitHub.get().getRepository(anyString())).thenReturn(mock(GHRepository.class));
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
        when(gitHub.get()).thenReturn(mock(GitHub.class));
        when(gitHub.get().getRepository(anyString())).thenReturn(mock(GHRepository.class));
        GHContent ghContent = mock(GHContent.class);
        when(gitHub.get().getRepository(anyString()).getFileContent(anyString(), anyString()))
            .thenReturn(ghContent);
        when(ghContent.isFile()).thenReturn(true);
        when(ghContent.getOwner()).thenReturn(mock(GHRepository.class));
        when(ghContent.getOwner().getName()).thenReturn("owner");
        String contentString = "{\"swagger\" : \"2.0\",\"host\" : \"petstore.swagger.io\"}";
        InputStream is = new ByteArrayInputStream(contentString.getBytes(StandardCharsets.UTF_8));
        when(ghContent.read()).thenReturn(is);
        //        HashMap<String, Object> hashMap = mapper.convertValue(contentString, HashMap.class);
        when(converter.convert(any())).thenReturn(project);
        //        Gson gson = mock(Gson.class);
        //        when(gson.fromJson(contentString, HashMap.class)).thenReturn(null);

        try {
            service.pullFileContent("owner/repo", "test.txt", "master", "123");
        } catch (DataNotFoundException e) {
            Assert.assertEquals(e.getMessage(), "Project is not found!");
        }
    }

    @Test
    public void pullFileContent_Success() throws IOException {
        when(gitHub.get()).thenReturn(mock(GitHub.class));
        when(gitHub.get().getRepository(anyString())).thenReturn(mock(GHRepository.class));
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






}
