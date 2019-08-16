package com.future.apix.service;

import com.future.apix.request.GithubContentsRequest;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.github.*;
import org.kohsuke.github.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface GithubApiService {
//    https://github-api.kohsuke.org

    GithubUserResponse getMyself() throws IOException;

    List<GithubRepoResponse> getMyselfRepositories() throws IOException;

    List<String> getBranches(String repoName) throws IOException;

    List<String> getFiles(String repoName, String branchName) throws IOException;

    GithubContentResponse getFileContent(String repoName, String contentPath, String ref) throws IOException;

    GithubCommitResponse updateFile(String repoName, String contentPath, GithubContentsRequest request) throws IOException;

    ProjectCreateResponse pullFileContent(String repoName, String contentPath, String ref, String projectId) throws IOException;
}
