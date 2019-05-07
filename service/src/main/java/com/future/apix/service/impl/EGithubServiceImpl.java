package com.future.apix.service.impl;

import com.future.apix.request.GithubAuthRequest;
import com.future.apix.service.EGithubService;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class EGithubServiceImpl implements EGithubService {

    private static GithubAuthRequest auth = new GithubAuthRequest();

    private GitHubClient client = new GitHubClient();

    private RepositoryService repositoryService = new RepositoryService(this.client);

//    private ContentsService CONTENT_SERVICE = new ContentsService(this.client);

    private UserService userService = new UserService(this.client);

    private RepositoryService initRepositoryService(){
        RepositoryService service;
        if (this.auth.getToken()!= null || this.auth.getToken().length() > 0) {
            service = new RepositoryService(new GitHubClient().setOAuth2Token(this.auth.getToken()));
        }
        else if (this.auth.getUser() != null && this.auth.getPassword() != null) {
            service = new RepositoryService(new GitHubClient().setCredentials(this.auth.getUser(), this.auth.getPassword()));
        }
        else {
            service = new RepositoryService(new GitHubClient());
        }
        return service;
    }


    @Override
    public String setToken(GithubAuthRequest request) {
        this.client.setOAuth2Token(request.getToken());
        this.auth.setToken(request.getToken());
        System.out.println(this.client.getRemainingRequests());
        return "Token set!";
    }

    @Override
    public String setCredentials(GithubAuthRequest request) {
        this.client.setCredentials(request.getUser(), request.getPassword());
        this.auth.setUser(request.getUser());
        this.auth.setPassword(request.getPassword());

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
    public List<Repository> getRepositories() throws IOException {
        // create new RepositoryService and new GithubClient
        RepositoryService service = initRepositoryService();
        System.out.println("CLIENT: " + service.getClient().toString());
        return service.getRepositories();
    }

    @Override
    public Repository createRepository(Repository repository) throws IOException {
        return repositoryService.createRepository(repository);
//        return null;
    }
    @Override
    public List<RepositoryBranch> getBranches(RepositoryId repoId) throws IOException {
//        return REPOSITORY_SERVICE.getBranches(repoId);
        return null;
    }

    @Override
    public List<RepositoryContents> getContents(IRepositoryIdProvider repository, String path) throws IOException {
//        return CONTENT_SERVICE.getContents(repository, path);
        return null;
    }

    @Override
    public User getMyself() throws IOException {
        // call reference UserService
//        UserService userService = new UserService(this.client);
        return userService.getUser();
    }
}
