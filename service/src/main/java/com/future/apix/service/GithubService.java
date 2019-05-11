package com.future.apix.service;

import com.future.apix.request.GithubContentsRequest;
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
    List<RepositoryContents> getContents(String user, String repo, String path, String ref) throws IOException;

    String authorizeUser();
    String getGithubProperties();

    Object createContents(String user, String repo, String path, GithubContentsRequest contentsRequest) throws IOException;
}
