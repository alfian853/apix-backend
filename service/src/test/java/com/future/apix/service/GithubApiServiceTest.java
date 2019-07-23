package com.future.apix.service;

import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.request.GithubContentsRequest;
import com.future.apix.response.github.GithubContentResponse;
import com.future.apix.service.impl.GithubApiServiceImpl;
import com.future.apix.util.LazyObjectWrapper;
import org.apache.commons.io.IOUtils;
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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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






}
