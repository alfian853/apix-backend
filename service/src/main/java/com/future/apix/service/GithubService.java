package com.future.apix.service;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.RepositoryId;

import java.io.IOException;
import java.util.List;

public interface GithubService {

    List<Repository> getRepositories() throws IOException;
    Repository createRepository(Repository repository) throws IOException;

    List<RepositoryBranch> getBranches(String user, String repo) throws IOException;
    RepositoryContents getReadme(String user, String repo, String ref) throws IOException;

    String authorizeUser();
    String getGithubProperties();
}
