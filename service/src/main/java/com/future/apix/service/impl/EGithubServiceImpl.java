package com.future.apix.service.impl;

import com.future.apix.service.EGithubService;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class EGithubServiceImpl implements EGithubService {

    private static GitHubClient client = new GitHubClient();

    @Override
    public String setToken(String token) {
        client.setOAuth2Token(token);
        System.out.println(client.getRequestLimit());
        return "Token set!";
    }

    @Override
    public String setCredentials(String user, String password) {
        client.setCredentials(user, password);
        System.out.println(client.getUser());
        return "User in github client!";
    }

    @Override
    public List<Repository> getUserRepositories(String user) throws IOException {
        RepositoryService service = new RepositoryService();
        List<Repository> repositories = new ArrayList<>();
        for (Repository repo : service.getRepositories(user)){
            System.out.println(repo.getName() + "Watchers: " + repo.getWatchers());
            repositories.add(repo);
        }
        return repositories;
    }
}
