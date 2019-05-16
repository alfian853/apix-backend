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

    @PostMapping("/repos")
    public Repository createRepository(@RequestBody Repository repository) throws IOException {
        return eGithubService.createRepository(repository);
    }

    @GetMapping("/repos")
    public List<Repository> getRepositories2() throws IOException {
        return eGithubService.getRepositories2();
    }

    @GetMapping("/branches")
    public List<RepositoryBranch> getBranches(@RequestBody RepositoryId repoId) throws IOException {
        return eGithubService.getBranches(repoId);
    }

    @GetMapping("/contents")
    public List<RepositoryContents> getContents(@RequestBody RepositoryId repoId)
            throws IOException {
        return eGithubService.getContents(repoId);
    }

    @GetMapping("/readme")
    public RepositoryContents getReadme(RepositoryId repoId, String ref) throws IOException {
        return eGithubService.getReadme(repoId, ref);
    }
}
