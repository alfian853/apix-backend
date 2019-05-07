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

    List<Repository> getRepositories() throws IOException; // with initialize new Repo and new GithubClient

    Repository createRepository(Repository repository) throws IOException;
    List<RepositoryBranch> getBranches(RepositoryId repoId) throws IOException;
    List<RepositoryContents> getContents(IRepositoryIdProvider repository, String path) throws IOException;

    User getMyself() throws IOException;


}
