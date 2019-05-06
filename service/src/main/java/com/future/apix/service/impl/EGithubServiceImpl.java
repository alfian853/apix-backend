package com.future.apix.service.impl;

import com.future.apix.service.EGithubService;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class EGithubServiceImpl implements EGithubService {

    private GitHubClient client = new GitHubClient();

    private RepositoryService REPOSITORY_SERVICE = new RepositoryService(this.client);

    private ContentsService CONTENT_SERVICE = new ContentsService(this.client);

    @Override
    public String setToken(String token) {
        this.client.setOAuth2Token(token);
        System.out.println(this.client.getRemainingRequests());
        return "Token set!";
    }

    @Override
    public String setCredentials(String user, String password) {
        this.client.setCredentials(user, password);
        System.out.println(this.client.getUser());
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

    @Override
    public PageIterator<Repository> getRepositories(int start, int size) {
        return REPOSITORY_SERVICE.pageRepositories(start, size);
    }

    @Override
    public Repository createRepository(Repository repository) throws IOException {
        return REPOSITORY_SERVICE.createRepository(repository);
    }
    @Override
    public List<RepositoryBranch> getBranches(RepositoryId repoId) throws IOException {
        return REPOSITORY_SERVICE.getBranches(repoId);
    }

    @Override
    public List<RepositoryContents> getContents(IRepositoryIdProvider repository, String path) throws IOException {
        return CONTENT_SERVICE.getContents(repository, path);
    }
}
