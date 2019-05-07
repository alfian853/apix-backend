package com.future.apix.controller;

import com.future.apix.request.GithubAuthRequest;
import com.future.apix.service.EGithubService;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/egithub")
public class EGithubController {
    @Autowired
    EGithubService eGithubService;

    @PostMapping("/login")
    public String setCredentials(@RequestBody GithubAuthRequest authRequest) {
        return eGithubService.setCredentials(authRequest);
    }

    @PostMapping("/token")
    public String setOAuthToken(@RequestBody GithubAuthRequest authRequest) {
        return eGithubService.setToken(authRequest);
    }

    @GetMapping("/myself")
    public User getMyself() throws IOException {
        return eGithubService.getMyself();
    }

    @GetMapping("/repository")
    public List<Repository> getRepositories2(@RequestParam("user") String user) throws IOException {
        GitHubClient client = new GitHubClient().setCredentials("natashaval", "Jengsusy69");
        RepositoryService service = new RepositoryService();
        for (Repository repo : service.getRepositories(user))
            System.out.println(repo.getName() + " Watchers: " + repo.getWatchers());
        return service.getRepositories(user);
    }

    @GetMapping("/repo")
    public List<Repository> getRepositories() throws IOException {
        return eGithubService.getRepositories();
    }


    @PostMapping("/repo")
    public Repository createRepository(@RequestBody Repository repository) throws IOException {
        return eGithubService.createRepository(repository);
    }

    @GetMapping("/branch")
    public List<RepositoryBranch> getBranches(RepositoryId repoId) throws IOException {
        return eGithubService.getBranches(repoId);
    }

    @GetMapping("/contents")
    List<RepositoryContents> getContents(IRepositoryIdProvider repository, String path)
            throws IOException {
        return eGithubService.getContents(repository, path);
    }
}
