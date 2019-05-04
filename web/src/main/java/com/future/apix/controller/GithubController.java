package com.future.apix.controller;

import com.future.apix.service.GithubService;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/github")
public class GithubController {
    @Autowired
    GithubService githubService;

    @GetMapping("/myself")
    public GHUser getMyself() throws IOException {
        return githubService.getMyself();
    }

    @PostMapping("/token")
    public ResponseEntity setToken(@RequestParam("token") String token) throws IOException{
        githubService.setToken(token);
        return ResponseEntity.ok("Token has been set!");
    }

    @GetMapping("/repo/{name}")
    public GHRepository getRepository(@PathVariable("name") String repoName) throws IOException {
        return githubService.getRespository(repoName);
    }

}
