package com.future.apix.service;

import org.eclipse.egit.github.core.*;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.List;

public interface EGithubService {
    String setCredentials(String user, String password);
    String setToken(String token);

    List<Repository> getUserRepositories(String user) throws IOException;
    PageIterator<Repository> getRepositories(int start, int size);

    Repository createRepository(Repository repository) throws IOException;
    List<RepositoryBranch> getBranches(RepositoryId repoId) throws IOException;
    List<RepositoryContents> getContents(IRepositoryIdProvider repository, String path) throws IOException;
}
