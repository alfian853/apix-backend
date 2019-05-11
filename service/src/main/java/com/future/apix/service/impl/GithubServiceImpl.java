package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.request.GithubContentsRequest;
import com.future.apix.service.GithubService;
import org.apache.commons.codec.binary.Base64;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_REPOS;
import static org.eclipse.egit.github.core.client.IGitHubConstants.SEGMENT_CONTENTS;


import java.io.IOException;
import java.util.List;

//@PropertySource(value = "classpath:github.properties")
@Service
public class GithubServiceImpl implements GithubService {

    @Value("${apix.github.token}")
    private String token;

    @Value("${apix.github.oaspath}") // DO NOT FORGET TO CHANGE CRUD CONTENT PATH TO oas2.md
    private String oasPath;

    private GitHubClient client = new GitHubClient();

    private RepositoryService repositoryService = new RepositoryService(this.client);
    private ContentsService contentsService = new ContentsService(this.client);

    /* Get repositories of authenticated user */
    @Override
    public List<Repository> getRepositories() throws IOException {
        return repositoryService.getRepositories();
    }

    @Override
    public Repository createRepository(Repository repository) throws IOException {
        return repositoryService.createRepository(repository);
    }

    @Override
    public List<RepositoryBranch> getBranches(String user, String repo) throws IOException {
        RepositoryId repoId = new RepositoryId(user, repo);
        return repositoryService.getBranches(repoId);
    }

    @Override
    public RepositoryContents getReadme(String user, String repo, String ref) throws IOException {
        RepositoryId repoId = new RepositoryId(user, repo);
        RepositoryContents encodedReadme = contentsService.getReadme(repoId, ref);
        RepositoryContents decodedReadme = new RepositoryContents();
        decodedReadme.setSize(encodedReadme.getSize());
        decodedReadme.setEncoding(encodedReadme.getEncoding());
        decodedReadme.setName(encodedReadme.getName());
        decodedReadme.setType(encodedReadme.getType());
        decodedReadme.setPath(encodedReadme.getPath());
        decodedReadme.setSha(encodedReadme.getSha());
        String decodedContent = new String(Base64.decodeBase64(encodedReadme.getContent().getBytes()));
        decodedReadme.setContent(decodedContent);
        return decodedReadme;
    }

    @Override
    public String getGithubProperties() {
//        String propToken = env.getProperty("apix.github.token");
//        System.out.println(propToken);
        System.out.println("Token from @Value: " + token);
//        return new String(propToken + "Token Value: " + token);
        return token;
    }

    @Override
    public String authorizeUser(){
        this.client.setOAuth2Token(token);
        return "User has been authorized!";
    }

    @Override
    public List<RepositoryContents> getContents(String user, String repo, String path, String ref) throws IOException {
        RepositoryId repoId = new RepositoryId(user, repo);
        return contentsService.getContents(repoId, path, ref);
    }

    @Override
    public Object createContents(String user, String repo, String path, GithubContentsRequest contentsRequest) throws IOException {
        StringBuilder uri = new StringBuilder(SEGMENT_REPOS);
        uri.append('/').append(user).append('/').append(repo);
        uri.append(SEGMENT_CONTENTS).append('/').append(path);

        System.out.println(uri.toString());
        return this.client.post(uri.toString(), contentsRequest, GithubContentsRequest.class);

    }
}
