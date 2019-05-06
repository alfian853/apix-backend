package com.future.apix.controller;

import com.future.apix.service.EGithubService;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
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

    @PostMapping("/user")
    public String setCredentials(@RequestBody String user, @RequestBody String password) {
        return eGithubService.setCredentials(user, password);
    }

    @PostMapping("/token")
    public String setOAuthToken(@RequestBody String token) {
        return eGithubService.setToken(token);
    }

    @GetMapping("/repo")
    public List<Repository> getRepositories(@RequestParam("user") String user) throws IOException {
        return eGithubService.getUserRepositories(user);
    }

    @GetMapping("/repository")
    public List<Repository> getRepositories2(@RequestParam("user") String user) throws IOException {
        GitHubClient client = new GitHubClient().setCredentials("natashaval", "Jengsusy69");
        RepositoryService service = new RepositoryService();
        for (Repository repo : service.getRepositories(user))
            System.out.println(repo.getName() + " Watchers: " + repo.getWatchers());
        return service.getRepositories(user);
    }
}
