package com.future.apix.service;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.List;

public interface EGithubService {
    String setCredentials(String user, String password);
    String setToken(String token);

    List<Repository> getUserRepositories(String user) throws IOException;
}
