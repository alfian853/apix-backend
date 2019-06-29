package com.future.apix.service;

import com.future.apix.repository.ApiRepository;
import com.future.apix.service.impl.GithubApiServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.github.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.IOException;

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
    GitHub gitHub;

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
        when(gitHub.getMyself()).thenReturn(ghMyself);
        service.getMyself();
        verify(gitHub).getMyself();
        verifyNoMoreInteractions(gitHub);
    }

    @Test
    public void getMyselfRepositoriesTest()throws IOException{
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
    }






}
