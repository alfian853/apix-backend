package com.future.apix.controller;

import com.future.apix.response.github.GithubUserResponse;
import com.future.apix.service.GithubApiService;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/github/api")
public class GithubApiController {
    @Autowired
    private GithubApiService githubService;

    @GetMapping("/isauth")
    public boolean isAuthenticated() throws IOException {
        return githubService.isAuthenticated();
    }

    @PostMapping("/auth")
    public String authorizeUser() throws IOException {
        return githubService.authenticateUser();
    }

    @GetMapping("/user")
    public GithubUserResponse getMyself() throws IOException {
        return githubService.getMyself();
    }

    @GetMapping("/user/{username}")
    public GithubUserResponse getUser(@PathVariable("username") String username) throws IOException {
        return githubService.getUser(username);
    }

    @GetMapping("/user/repos")
    public PagedIterable<GHRepository> getMyselfRepositories() throws IOException {
        return githubService.getMyselfRepositories();
    }

    @GetMapping("/repos/{repo}")
    public GHRepository getRepository(
            @PathVariable("repo") String repoName) throws IOException {
        return githubService.getRepository(repoName);
    }

    @GetMapping("/repos/{repo}/branches")
    public Map<String, GHBranch> getBranches(
            @PathVariable("repo") String repoName) throws IOException {
        return githubService.getBranches(repoName);
    }

    @GetMapping("/repos/{repo}/branches/{branch}")
    public GHBranch getBranch(
            @PathVariable("repo") String repoName,
            @PathVariable("branch") String branchName
    ) throws IOException {
        return githubService.getBranch(repoName, branchName);
    }

    @GetMapping("/repos/{repo}/readme")
    public GHContent getReadme(
            @PathVariable("repo") String repoName) throws IOException {
        return githubService.getReadme(repoName);
    }

    @GetMapping("/repos/{repo}/contents/{path}")
    public GHContent getFileContent(
            @PathVariable("repo") String repoName,
            @PathVariable("path") String contentPath,
            @RequestParam(required = false) String ref
    ) throws IOException {
        return githubService.getFileContent(repoName, contentPath, ref);
    }

}
