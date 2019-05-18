package com.future.apix.service.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.OasSwagger2Repository;
import com.future.apix.request.GithubContentsRequest;
import com.future.apix.response.github.*;
import com.future.apix.service.CommandExecutorService;
import com.future.apix.service.GithubApiService;
import com.future.apix.service.command.Swagger2ExportCommand;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class GithubApiServiceImpl implements GithubApiService {

    @Value("${apix.github.token}")
    private String token;

    @Value("${apix.export_oas.directory}")
    private String EXPORT_DIR;

    @Autowired
    private ObjectMapper oMapper;

    @Autowired
    private CommandExecutorService commandExecutor;

    @Autowired
    private OasSwagger2Repository oasRepository;

//    private static GitHub gitHub;

    @Override
    public String authenticateUser() throws IOException {
        GitHub gitHub = authToken();
        return "Github is authenticated!";
    }

    @Override
    public Boolean isAuthenticated() throws IOException {
        GitHub gitHub = authToken();
        return gitHub.isCredentialValid();
    }

    @Override
    public GithubUserResponse getMyself() throws IOException {
        GitHub gitHub = authToken();
        GHMyself self = gitHub.getMyself();
        return convertUser(self);
    }

    @Override
    public GithubUserResponse getUser(String login) throws IOException {
        GitHub gitHub = authToken();
        GHUser user = gitHub.getUser(login);
        return convertUser(user);
    }

    @Override
    public List<GithubRepoResponse> getMyselfRepositories() throws IOException {
        GitHub gitHub = authToken();
        PagedIterable<GHRepository> repositories = gitHub.getMyself().listRepositories();
        List<GithubRepoResponse> repoList = new ArrayList<>();

        Iterator itr = repositories.iterator();
        while(itr.hasNext()){
            Object object = itr.next();
            GithubRepoResponse response = convertRepository((GHRepository) object);
            repoList.add(response);
        }

        return repoList;
    }

    @Override
    public GithubRepoResponse getRepository(String repoName) throws IOException {
        GitHub gitHub = authToken();
        GHRepository repository = gitHub.getRepository(repoName);
        return convertRepository(repository);
    }

    @Override
    public Map<String, GHBranch> getBranches(String repoName) throws IOException {
        GitHub gitHub = authToken();
        return gitHub.getRepository(repoName).getBranches();
    }

    @Override
    public GithubBranchResponse getBranch(String repoName, String branchName) throws IOException {
        GitHub gitHub = authToken();
        GHBranch branch = gitHub.getRepository(repoName).getBranch(branchName);
        return convertBranch(branch);
    }

    @Override
    public GithubContentResponse getReadme(String repoName) throws IOException {
        GitHub gitHub = authToken();
        GHContent content = gitHub.getRepository(repoName).getReadme();
        return convertContent(content);
    }

    @Override
    public GithubContentResponse getFileContent(String repoName, String contentPath, String ref) throws IOException {
        GitHub gitHub = authToken();
        if (ref == null || ref.length() <= 0) ref = "master";
        GHContent content = gitHub.getRepository(repoName).getFileContent(contentPath, ref);
        if (content.isFile()) {
            return convertContent(content);

        }
        else throw new DataNotFoundException("File is not available!");

    }

    @Override
    public GithubCommitResponse updateFile(String repoName, String contentPath, GithubContentsRequest request) throws IOException {
        GitHub gitHub = authToken();
        if (request.getBranch() == null || request.getBranch().length() <= 0) request.setBranch("master");
        GHContent content = gitHub.getRepository(repoName).getFileContent(contentPath, request.getBranch());
        if (content.isFile()) {
            String projectId = request.getProjectId();
            commandExecutor.execute(Swagger2ExportCommand.class, projectId);
            String oasPath = oasRepository.findProjectOasSwagger2ByProjectId(projectId).orElseThrow(DataNotFoundException::new).getOasFileName();
            Path path = Paths.get(EXPORT_DIR + oasPath);
            String readContent = readFromFile(path);
//            System.out.println("Content\n" + readContent);

            GHContentUpdateResponse ghResponse = content.update(readContent, request.getMessage(), request.getBranch());
//            return convertContentUpdate(ghResponse);
            return convertCommit(ghResponse.getCommit());
//            return null;
        }
        else
            throw new InvalidRequestException("Content is not a file!");
    }

//    ============ private Function ===============

    private GitHub authToken() throws IOException {
        GitHub gitHub = GitHub.connectUsingOAuth(token);
        return gitHub;
    }

    private GithubUserResponse convertUser(GHUser user) throws IOException {
        GithubUserResponse response = new GithubUserResponse();
        response.setId(user.getId());
        response.setLogin(user.getLogin());
        response.setName(user.getName());
        return response;
    }

    private GithubUserResponse convertCommitter(GitUser user) {
        GithubUserResponse response = new GithubUserResponse();
        response.setDate(user.getDate());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        return response;
    }

    private GithubRepoResponse convertRepository(GHRepository repository) {
        GithubRepoResponse response = new GithubRepoResponse();
        response.setId(repository.getId());
        response.setName(repository.getName());
        response.setFullName(repository.getFullName());
        response.setDescription(repository.getDescription());
        response.setOwnerName(repository.getOwnerName());
        return response;
    }

    private GithubBranchResponse convertBranch(GHBranch branch) {
        GithubBranchResponse response = new GithubBranchResponse();
        response.setName(branch.getName());
        response.setSha(branch.getSHA1());
        response.setRepoName(branch.getOwner().getName());
        return response;
    }

    private GithubContentResponse convertContent(GHContent content) throws IOException {
        GithubContentResponse response = new GithubContentResponse();
        response.setType(content.getType());
        response.setEncoding(content.getEncoding());
        response.setName(content.getName());
        response.setRepoName(content.getOwner().getName());
        response.setPath(content.getPath());
        response.setSha(content.getSha());
        response.setSize(content.getSize());
        response.setUrl(content.getUrl());
        response.setHtmlUrl(content.getHtmlUrl());
        InputStream i = content.read();
        String readContent = IOUtils.toString(i, StandardCharsets.UTF_8.name());
        response.setContent(readContent);
        return response;
    }

    private GithubContentUpdateResponse convertContentUpdate(GHContentUpdateResponse updateResponse) {
        GithubContentUpdateResponse response = new GithubContentUpdateResponse();
        response = oMapper.convertValue(updateResponse, GithubContentUpdateResponse.class);
        return response;
    }

    private GithubCommitResponse convertCommit(GHCommit commit) throws IOException {
        GithubCommitResponse response = new GithubCommitResponse();
        response.setSha(commit.getSHA1());
        response.setMessage(commit.getCommitShortInfo().getMessage());
        response.setOwner(convertRepository(commit.getOwner()));
        response.setCommitter(convertUser(commit.getCommitter()));
        response.setCommitDate(commit.getCommitDate());
        return response;
    }
    private String readFromFile(Path filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(filePath , StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

}
