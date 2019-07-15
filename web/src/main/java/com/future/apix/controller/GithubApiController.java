package com.future.apix.controller;

import com.future.apix.request.GithubContentsRequest;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.github.*;
import com.future.apix.service.GithubApiService;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/github/api")
public class GithubApiController {
    @Autowired
    private GithubApiService githubService;

    /*
    @GetMapping("/isauth")
    public boolean isAuthenticated() throws IOException {
        return githubService.isAuthenticated();
    }

    @PostMapping("/auth")
    public String authorizeUser() throws IOException {
        return githubService.authenticateUser();
    }
    */

    @GetMapping("/user")
    public GithubUserResponse getMyself() throws IOException {
        return githubService.getMyself();
    }


//    @GetMapping("/user/{username}")
//    public GithubUserResponse getUser(@PathVariable("username") String username) throws IOException {
//        return githubService.getUser(username);
//    }


    @GetMapping("/user/repos")
    public List<GithubRepoResponse> getMyselfRepositories() throws IOException {
        return githubService.getMyselfRepositories();
    }

//    @GetMapping("/repos/{owner}/{repo}")
//    public GithubRepoResponse getRepository(
//            @PathVariable("owner") String owner,
//            @PathVariable("repo") String repo) throws IOException {
//        return githubService.getRepository(owner + "/" + repo);
//    }

    @GetMapping("/repos/{owner}/{repo}/branches")
    public List<String> getBranches(
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo) throws IOException {
        return githubService.getBranches(owner + "/" + repo);
    }

//    @GetMapping("/repos/{owner}/{repo}/branches/{branch}")
//    public GithubBranchResponse getBranch(
//            @PathVariable("owner") String owner,
//            @PathVariable("repo") String repo,
//            @PathVariable("branch") String branchName
//    ) throws IOException {
//        return githubService.getBranch(owner + "/" + repo, branchName);
//    }

//    @GetMapping("/repos/{owner}/{repo}/readme")
//    public GithubContentResponse getReadme(
//            @PathVariable("owner") String owner,
//            @PathVariable("repo") String repo) throws IOException {
//        return githubService.getReadme(owner + "/" + repo);
//    }

    @GetMapping("/repos/{owner}/{repo}/contents/{path:.+}")
    public GithubContentResponse getFileContent(
//            https://stackoverflow.com/questions/30548822/spring-mvc-4-application-json-content-type-is-not-being-set-correctly
            // GET ERROR 406 for content since the request uri ends with .[suffix] the requestedMediaTypes be "text/[suffix]"
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo,
            @PathVariable("path") String contentPath,
            @RequestParam(required = false) String ref
    ) throws IOException {
        return githubService.getFileContent(owner + "/" + repo, contentPath, ref);
//        return githubService.pullFileContent(owner + "/" + repo, contentPath, ref, projectId);
    }

    @PostMapping("/repos/{owner}/{repo}/contents/{path:.+}")
    public ProjectCreateResponse pullFileContent(
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo,
            @PathVariable("path") String contentPath,
            @RequestParam(required = false) String ref,
            @RequestParam("projectId") String projectId
    ) throws IOException {
        return githubService.pullFileContent(owner + "/" + repo, contentPath, ref, projectId);
    }

    @PutMapping("/repos/{owner}/{repo}/contents/{path:.+}")
    public GithubCommitResponse pushFileContent(
            @PathVariable("owner") String owner,
            @PathVariable("repo") String repo,
            @PathVariable("path") String contentPath,
            @Valid @RequestBody GithubContentsRequest request
            ) throws IOException {
        return githubService.updateFile(owner + "/" + repo, contentPath, request);
    }

}
