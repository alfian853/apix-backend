package com.future.apix.service;

import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitUser;

import java.io.IOException;

public interface GithubService {
    void setToken(String oauthToken) throws IOException;
    GHUser getMyself() throws IOException;
    GHRepository getRespository(String repoName) throws IOException;
}
