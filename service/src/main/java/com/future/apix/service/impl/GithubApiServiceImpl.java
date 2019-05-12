package com.future.apix.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.response.github.GithubUserResponse;
import com.future.apix.service.GithubApiService;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
public class GithubApiServiceImpl implements GithubApiService {

    @Value("${apix.github.token}")
    private String token;

    @Autowired
    private ObjectMapper oMapper;

//    private static GitHub gitHub;

    private GitHub authToken() throws IOException {
        GitHub gitHub = GitHub.connectUsingOAuth(token);
        return gitHub;
    }

    @Override
    public Boolean isAuthenticated() throws IOException {
        GitHub gitHub = authToken();
        return gitHub.isCredentialValid();
    }

    @Override
    public GithubUserResponse getMyself() throws IOException {
        GitHub gitHub = authToken();
        oMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        GithubUserResponse response = oMapper.convertValue(gitHub.getMyself(), GithubUserResponse.class);
        return response;
    }

    @Override
    public GithubUserResponse getUser(String login) throws IOException {
        GitHub gitHub = authToken();
        oMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        GithubUserResponse response = oMapper.convertValue(gitHub.getUser(login), GithubUserResponse.class);
        return response;
    }

    @Override
    public PagedIterable<GHRepository> getMyselfRepositories() throws IOException {
        GitHub gitHub = authToken();
        return gitHub.getMyself().listRepositories();
    }

    @Override
    public String authenticateUser() throws IOException {
        GitHub gitHub = authToken();
        return "Github is authenticated!";
    }

    @Override
    public GHRepository getRepository(String repoName) throws IOException {
        GitHub gitHub = authToken();
        return gitHub.getRepository(repoName);
    }

    @Override
    public Map<String, GHBranch> getBranches(String repoName) throws IOException {
        GitHub gitHub = authToken();
        return gitHub.getRepository(repoName).getBranches();
    }

    @Override
    public GHBranch getBranch(String repoName, String branchName) throws IOException {
        GitHub gitHub = authToken();
        return gitHub.getRepository(repoName).getBranch(branchName);
    }

    @Override
    public GHContent getReadme(String repoName) throws IOException {
        GitHub gitHub = authToken();
        return gitHub.getRepository(repoName).getReadme();
    }

    @Override
    public GHContent getFileContent(String repoName, String contentPath, String ref) throws IOException {
        GitHub gitHub = authToken();
        if (ref == null || ref.length() <= 0) ref = "master";
        return  gitHub.getRepository(repoName).getFileContent(contentPath, ref);
    }
}
