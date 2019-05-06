package com.future.apix.service.impl;

import com.future.apix.service.GithubService;
import org.kohsuke.github.*;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GithubServiceImpl implements GithubService {

    /*
    private static GitHub gitHub = initGithub();

    private static GitHub initGithub() {
        gitHub = null;
        try {
            gitHub = new GitHub();
        } catch (IOException e) {
            System.out.println(e);
        }
        return gitHub;
    }

    private static String token;



    @Override
    public void setToken(String oauthToken) throws IOException {
        token = oauthToken;
    }

    @Override
    public GHUser getMyself() throws IOException {
        GitHub gitHub = GitHub.connectUsingOAuth(token);
        GHUser myself = gitHub.getMyself();
        return myself;
    }

    @Override
    public GHRepository getRespository(String repoName) throws IOException {
        GitHub gitHub = GitHub.connectUsingOAuth(token);
        GHRepository repository = gitHub.getRepository(repoName);
        return repository;
    }
    */

    @Override
    public GHUser getMyself(String user, String password) throws IOException {
        GitHub gitHub = GitHubBuilder.fromCredentials().withPassword(user, password).build();
        GHUser ghUser = gitHub.getMyself();
        return ghUser;
    }

    @Override
    public GitHubBuilder getPropertyFile() throws IOException {
        GitHubBuilder gitHubBuilder = GitHubBuilder.fromPropertyFile();
        System.out.println(gitHubBuilder);
        return gitHubBuilder;
    }

    @Override
    public GitHubBuilder getPropertyFileName(String propertyName) throws IOException {
        GitHubBuilder gitHubBuilder = GitHubBuilder.fromPropertyFile(propertyName);
        System.out.println(gitHubBuilder);
        return gitHubBuilder;
    }

}
