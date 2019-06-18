package com.future.apix.service;

import com.future.apix.request.GithubAuthRequest;
import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.List;

public interface EGithubService {
    String setCredentials(GithubAuthRequest request);
    String setToken(GithubAuthRequest request);

    List<Repository> getUserRepositories(String user) throws IOException;

    // List<Repository> getRepositories() throws IOException; // with initialize new Repo and new GithubClient

    List<Repository> getRepositories2() throws IOException;
    Repository createRepository(Repository repository) throws IOException;
    List<RepositoryBranch> getBranches(RepositoryId repoId) throws IOException;
    List<RepositoryContents> getContents(RepositoryId repoId) throws IOException;

    RepositoryContents getReadme(RepositoryId repoId, String ref) throws IOException;

    User getMyself() throws IOException;


}
