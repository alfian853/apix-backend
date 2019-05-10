package com.future.apix.controller;

import com.future.apix.response.RequestResponse;
import com.future.apix.service.GithubService;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.RepositoryId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/github")
public class GithubController {
    @Autowired
    GithubService githubService;

    @GetMapping("/repos")
    public List<Repository> getRepositories() throws IOException {
        return githubService.getRepositories();
    }

    @GetMapping("/repos/{user}/{repo}/branches")
    public List<RepositoryBranch> getBranches(@PathVariable("user")String user, @PathVariable("repo") String repo) throws IOException {
        return githubService.getBranches(user, repo);
    }

    @GetMapping("/repos/{user}/{repo}/readme")
    public RepositoryContents getReadme(
            @PathVariable("user")String user,
            @PathVariable("repo") String repo,
            @RequestParam(required = false) String ref) throws IOException {
        return githubService.getReadme(user, repo, ref);
    }

    @GetMapping("/properties")
    public ResponseEntity getProperties(){
        return ResponseEntity.ok(githubService.getGithubProperties());
    }

    @PostMapping("/auth")
    public RequestResponse setAuth(){
        RequestResponse response = new RequestResponse();
        response.setStatusToSuccess();
        response.setMessage(githubService.authorizeUser());
        return response;
    }



}
