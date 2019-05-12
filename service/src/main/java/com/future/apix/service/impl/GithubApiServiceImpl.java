package com.future.apix.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.response.github.GithubBranchResponse;
import com.future.apix.response.github.GithubContentResponse;
import com.future.apix.response.github.GithubRepoResponse;
import com.future.apix.response.github.GithubUserResponse;
import com.future.apix.service.GithubApiService;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class GithubApiServiceImpl implements GithubApiService {

    @Value("${apix.github.token}")
    private String token;

    @Autowired
    private ObjectMapper oMapper;

//    private static GitHub gitHub;

    @Override
    public String authenticateUser() throws IOException {
        GitHub gitHub = authToken();
        return "Github is authenticated!";
    }

    @Override
    public Boolean isAuthenticated() throws IOException {
        GitHub gitHub = authToken();
        return gitHub.isCredentialValid();
    }

    @Override
    public GithubUserResponse getMyself() throws IOException {
        GitHub gitHub = authToken();
        GHMyself self = gitHub.getMyself();
        return convertUser(self);
    }

    @Override
    public GithubUserResponse getUser(String login) throws IOException {
        GitHub gitHub = authToken();
        GHUser user = gitHub.getUser(login);
        return convertUser(user);
    }

    @Override
    public List<GithubRepoResponse> getMyselfRepositories() throws IOException {
        GitHub gitHub = authToken();
        PagedIterable<GHRepository> repositories = gitHub.getMyself().listRepositories();
        List<GithubRepoResponse> repoList = new ArrayList<>();

        Iterator itr = repositories.iterator();
        while(itr.hasNext()){
            Object object = itr.next();
            GithubRepoResponse response = convertRepository((GHRepository) object);
            repoList.add(response);
        }

        return repoList;
    }

    @Override
    public GithubRepoResponse getRepository(String repoName) throws IOException {
        GitHub gitHub = authToken();
        GHRepository repository = gitHub.getRepository(repoName);
        return convertRepository(repository);
    }

    @Override
    public Map<String, GHBranch> getBranches(String repoName) throws IOException {
        GitHub gitHub = authToken();
        return gitHub.getRepository(repoName).getBranches();
    }

    @Override
    public GithubBranchResponse getBranch(String repoName, String branchName) throws IOException {
        GitHub gitHub = authToken();
        GHBranch branch = gitHub.getRepository(repoName).getBranch(branchName);
        return convertBranch(branch);
    }

    @Override
    public GithubContentResponse getReadme(String repoName) throws IOException {
        GitHub gitHub = authToken();
        GHContent content = gitHub.getRepository(repoName).getReadme();
        return convertContent(content);
    }

    @Override
    public GithubContentResponse getFileContent(String repoName, String contentPath, String ref) throws IOException {
        GitHub gitHub = authToken();
        if (ref == null || ref.length() <= 0) ref = "master";
        GHContent content = gitHub.getRepository(repoName).getFileContent(contentPath, ref);
        return convertContent(content);
    }

//    ============ private Function ===============

    private GitHub authToken() throws IOException {
        GitHub gitHub = GitHub.connectUsingOAuth(token);
        return gitHub;
    }

    private GithubUserResponse convertUser(GHUser user) throws IOException {
        GithubUserResponse response = new GithubUserResponse();
        response.setId(user.getId());
        response.setLogin(user.getLogin());
        response.setName(user.getName());
        return response;
    }

    private GithubRepoResponse convertRepository(GHRepository repository) {
        GithubRepoResponse response = new GithubRepoResponse();
        response.setId(repository.getId());
        response.setName(repository.getName());
        response.setFullName(repository.getFullName());
        response.setDescription(repository.getDescription());
        response.setOwnerName(repository.getOwnerName());
        return response;
    }

    private GithubBranchResponse convertBranch(GHBranch branch) {
        GithubBranchResponse response = new GithubBranchResponse();
        response.setName(branch.getName());
        response.setSha(branch.getSHA1());
        response.setRepoName(branch.getOwner().getName());
        return response;
    }

    private GithubContentResponse convertContent(GHContent content) throws IOException {
        GithubContentResponse response = new GithubContentResponse();
        response.setType(content.getType());
        response.setEncoding(content.getEncoding());
        response.setName(content.getName());
        response.setRepoName(content.getOwner().getName());
        response.setPath(content.getPath());
        response.setSha(content.getSha());
        response.setSize(content.getSize());
        response.setContent(content.getContent());
        response.setEncondedContent(content.getEncodedContent());
        response.setUrl(content.getUrl());
        response.setHtmlUrl(content.getHtmlUrl());
        return response;
    }
}
