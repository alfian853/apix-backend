package com.future.apix.service;

import com.future.apix.entity.apidetail.Github;
import com.future.apix.repository.ApiRepository;
import com.future.apix.response.github.GithubUserResponse;
import com.future.apix.service.impl.GithubApiServiceImpl;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.RateLimitHandler;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

@RunWith(MockitoJUnitRunner.class)
public class GithubApiServiceTest {

//    https://github.com/kohsuke/github-api/blob/master/src/test/java/org/kohsuke/github/AbstractGitHubApiTestBase.java
//    https://github.com/kohsuke/github-api/blob/master/src/test/java/org/kohsuke/github/AppTest.java
//    https://github.com/kohsuke/github-api/blob/master/src/test/java/org/kohsuke/github/GitHubTest.java

    @Spy
    @InjectMocks
    GithubApiServiceImpl service;

    @Value("${apix.github.token}")
    private String token;

    @Mock
    GitHub mockGitHub;

    protected GitHub gitHub;

    @Mock
    ApiRepository apiRepository;

//    @Before
//    public void setUp() throws IOException {
//        mockGitHub = GitHub.connectUsingOAuth(token);
//        gitHub = new GitHubBuilder().withOAuthToken(token).withRateLimitHandler(RateLimitHandler.FAIL).build();
//    }

    @Before
    public void setUp() throws Exception {
        File f = new File(System.getProperty("user.home"), ".github.kohsuke2");
        if (f.exists()) {
            Properties props = new Properties();
            FileInputStream in = null;
            try {
                in = new FileInputStream(f);
                props.load(in);
            } finally {
                IOUtils.closeQuietly(in);
            }
            // use the non-standard credential preferentially, so that developers of this library do not have
            // to clutter their event stream.
            gitHub = GitHubBuilder.fromProperties(props).withRateLimitHandler(RateLimitHandler.FAIL).build();
        } else {
            gitHub = GitHubBuilder.fromCredentials().withRateLimitHandler(RateLimitHandler.FAIL).build();
        }
    }

    @Test
    public void getMyself(){
        try {
//            Mockito.when(mockGitHub.connectUsingOAuth(token)).thenReturn(GitHub.class);
            Mockito.when(service.authToken()).thenReturn(gitHub);
            GithubUserResponse response = service.getMyself();
            System.out.println("Response: " + response.getName());

        } catch (IOException e){
            System.out.println("IOException: " +e.getMessage());
        }
    }
}
