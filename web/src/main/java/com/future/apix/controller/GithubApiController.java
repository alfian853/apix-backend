package com.future.apix.controller;

import com.future.apix.response.github.GithubBranchResponse;
import com.future.apix.response.github.GithubContentResponse;
import com.future.apix.response.github.GithubRepoResponse;
import com.future.apix.response.github.GithubUserResponse;
import com.future.apix.service.GithubApiService;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
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
    public List<GithubRepoResponse> getMyselfRepositories() throws IOException {
        return githubService.getMyselfRepositories();
    }

    @GetMapping("/repos/{owner}/{repo}")
    public GithubRepoResponse getRepository(
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo) throws IOException {
        return githubService.getRepository(owner + "/" + repo);
    }

    @GetMapping("/repos/{repo}/branches")
    public Map<String, GHBranch> getBranches(
            @PathVariable("repo") String repoName) throws IOException {
        return githubService.getBranches(repoName);
    }

    @GetMapping("/repos/{owner}/{repo}/branches/{branch}")
    public GithubBranchResponse getBranch(
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo,
            @PathVariable("branch") String branchName
    ) throws IOException {
        return githubService.getBranch(owner + "/" + repo, branchName);
    }

    @GetMapping("/repos/{owner}/{repo}/readme")
    public GithubContentResponse getReadme(
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo) throws IOException {
        return githubService.getReadme(owner + "/" + repo);
    }

    @GetMapping("/repos/{repo}/contents/{path}")
    public GithubContentResponse getFileContent(
            @PathVariable("repo") String repoName,
            @PathVariable("path") String contentPath,
            @RequestParam(required = false) String ref
    ) throws IOException {
        return githubService.getFileContent(repoName, contentPath, ref);
    }

}
