package com.future.apix.service;

import org.kohsuke.github.*;

import java.io.IOException;

public interface GithubService {
    /*
    void setToken(String oauthToken) throws IOException;
    GHUser getMyself() throws IOException;
    GHRepository getRespository(String repoName) throws IOException;
    */

    GHUser getMyself(String user, String password) throws IOException;
    GitHubBuilder getPropertyFile() throws IOException;
    GitHubBuilder getPropertyFileName(String propertyName) throws IOException;
}
