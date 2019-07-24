package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.command.model.ExportRequest;
import com.future.apix.command.model.enumerate.FileFormat;
import com.future.apix.entity.ApiProject;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.repository.OasSwagger2Repository;
import com.future.apix.request.GithubContentsRequest;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.github.GithubCommitResponse;
import com.future.apix.response.github.GithubContentResponse;
import com.future.apix.response.github.GithubRepoResponse;
import com.future.apix.response.github.GithubUserResponse;
import com.future.apix.service.CommandExecutorService;
import com.future.apix.service.GithubApiService;
import com.future.apix.command.Swagger2ExportCommand;
import com.future.apix.util.LazyObjectWrapper;
import com.future.apix.util.converter.SwaggerToApixOasConverter;
import com.google.gson.Gson;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

@Service
public class GithubApiServiceImpl implements GithubApiService {

    @Value("${apix.export_oas.directory}")
    private String EXPORT_DIR;

    @Autowired
    private ObjectMapper oMapper;

    @Autowired
    private CommandExecutorService commandExecutor;

    @Autowired
    private OasSwagger2Repository oasRepository;

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private SwaggerToApixOasConverter converter;

    @Autowired
    private LazyObjectWrapper<GitHub> gitHub;

    @Override
    public GithubUserResponse getMyself() throws IOException {
        GHMyself self = gitHub.get().getMyself();
        return convertUser(self);
    }

    @Override
    public List<GithubRepoResponse> getMyselfRepositories() throws IOException {
        PagedIterable<GHRepository> repositories = gitHub.get().getMyself().listRepositories();
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
    public List<String> getBranches(String repoName) throws IOException {
        List<String> branchName = new ArrayList<>();
        Map<String, GHBranch> branches = gitHub.get().getRepository(repoName).getBranches();
        for (Map.Entry<String, GHBranch> entry : branches.entrySet()){
            if (!Objects.equals(entry.getKey(), "master")) branchName.add(entry.getKey());
            // exclude master, since it is default branch
        }
        return branchName;
    }

    @Override
    public GithubContentResponse getFileContent(String repoName, String contentPath, String ref) throws IOException {
        if (ref == null || ref.length() <= 0) ref = "master";
        GHContent content = gitHub.get().getRepository(repoName).getFileContent(contentPath, ref);
        if (content.isFile()) {
            return convertContent(content);

        }
        else throw new DataNotFoundException("File is not available!");

    }

    @Override
    public GithubCommitResponse updateFile(String repoName, String contentPath, GithubContentsRequest request) throws IOException {
        if (request.getBranch() == null || request.getBranch().length() <= 0) request.setBranch("master");
        GHContent content = gitHub.get().getRepository(repoName).getFileContent(contentPath, request.getBranch());
        if (content.isFile()) {
            String existSha = DigestUtils.sha256Hex(content.read());

            String projectId = request.getProjectId();
            ExportRequest exportFormat = new ExportRequest(projectId, FileFormat.JSON);
            commandExecutor.executeCommand(Swagger2ExportCommand.class, exportFormat);
            String oasPath = oasRepository.findProjectOasSwagger2ByProjectId(projectId).orElseThrow(DataNotFoundException::new).getOasFileName();
            Path path = Paths.get(EXPORT_DIR + oasPath + "." + FileFormat.JSON.toString().toLowerCase());
            String readContent = readFromFile(path);
//            System.out.println("Content\n" + readContent);

            String exportSha = DigestUtils.sha256Hex(readContent);
            if (existSha.equals(exportSha)) throw new InvalidRequestException("Content of OAS in Github is already equal");
            GHContentUpdateResponse ghResponse = content.update(readContent, request.getMessage(), request.getBranch());
//            return convertContentUpdate(ghResponse);
            return convertCommit(ghResponse.getCommit());
        }
        else
            throw new InvalidRequestException("Content is not a file!");
    }

    @Override
    public ProjectCreateResponse pullFileContent(String repoName, String contentPath, String ref, String projectId) throws IOException {
        if (ref == null || ref.length() <= 0) ref = "master";
        GHContent content = gitHub.get().getRepository(repoName).getFileContent(contentPath, ref);
        if (content.isFile()) {
//            HashMap<String, Object> json = convertContentToHashMap(content.read());
//            ApiProject project = converter.convert(json);
            GithubContentResponse GithubResponse = convertContent(content);
            ApiProject project = converter.convert(GithubResponse.getJson());
            ApiProject existingProject = apiRepository.findById(projectId)
                    .orElseThrow(() -> new DataNotFoundException("Project is not found!"));
            project.setId(projectId);
            project.setProjectOwner(existingProject.getProjectOwner());
            project.setTeams(existingProject.getTeams());
            apiRepository.save(project);

            ProjectCreateResponse response = new ProjectCreateResponse();
            response.setStatusToSuccess();
            response.setMessage("Project from github successfully pulled!");
            response.setProjectId(project.getId());
            return response;
        }
        else {
            throw new InvalidRequestException("Content is not a file!");
        }
    }

//    ============ private Function ===============

    private GithubUserResponse convertUser(GHUser user) throws IOException {
        GithubUserResponse response = new GithubUserResponse();
        response.setId(user.getId());
        response.setLogin(user.getLogin());
        response.setName(user.getName());
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
        Gson gson = new Gson();
        response.setJson(gson.fromJson(readContent, HashMap.class));
        return response;
    }

    private GithubCommitResponse convertCommit(GHCommit commit) throws IOException {
        GithubCommitResponse response = new GithubCommitResponse();
        response.setSha(commit.getSHA1());
        response.setMessage(commit.getCommitShortInfo().getMessage());
//        response.setOwner(convertRepository(commit.getOwner()));
//        response.setCommitter(convertUser(commit.getCommitter()));
        response.setCommitDate(commit.getCommitDate());
        return response;
    }

    public String readFromFile(Path filePath) {
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
