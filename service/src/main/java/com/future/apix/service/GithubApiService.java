package com.future.apix.service;

import com.future.apix.response.github.GithubBranchResponse;
import com.future.apix.response.github.GithubContentResponse;
import com.future.apix.response.github.GithubRepoResponse;
import com.future.apix.response.github.GithubUserResponse;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface GithubApiService {
//    https://github-api.kohsuke.org

    String authenticateUser() throws IOException;
    Boolean isAuthenticated() throws IOException;
    GithubUserResponse getMyself() throws IOException;
    GithubUserResponse getUser(String login) throws IOException;
    List<GithubRepoResponse> getMyselfRepositories() throws IOException;

    GithubRepoResponse getRepository(String repoName) throws IOException;
    Map<String, GHBranch> getBranches(String repoName) throws IOException;
    GithubBranchResponse getBranch(String repoName, String branchName) throws IOException;
    GithubContentResponse getReadme(String repoName) throws IOException;
    GithubContentResponse getFileContent(String repoName, String contentPath, String ref) throws IOException;
}
