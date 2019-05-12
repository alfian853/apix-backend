package com.future.apix.service;

import com.future.apix.response.github.GithubUserResponse;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.Map;

public interface GithubApiService {
//    https://github-api.kohsuke.org

    String authenticateUser() throws IOException;
    Boolean isAuthenticated() throws IOException;
    GithubUserResponse getMyself() throws IOException;
    GithubUserResponse getUser(String login) throws IOException;
    PagedIterable<GHRepository> getMyselfRepositories() throws IOException;

    GHRepository getRepository(String repoName) throws IOException;
    Map<String, GHBranch> getBranches(String repoName) throws IOException;
    GHBranch getBranch(String repoName, String branchName) throws IOException;
    GHContent getReadme(String repoName) throws IOException;
    GHContent getFileContent(String repoName, String contentPath, String ref) throws IOException;
}
