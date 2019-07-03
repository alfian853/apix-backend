package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.Team;
import com.future.apix.entity.apidetail.Github;
import com.future.apix.entity.apidetail.ProjectInfo;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.repository.TeamRepository;
import com.future.apix.request.ProjectCreateRequest;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.UserProfileResponse;
import com.future.apix.service.ApiDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ApiDataServiceImpl implements ApiDataService {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ObjectMapper oMapper;

    @Override
    public ApiProject findById(String id) {
        return apiRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Project does not exists!"));
    }

    @Override
    public List<ApiProject> findAll() {
        return apiRepository.findAll();
    }

    @Override
    public List<ApiProject> findAllProjects() {
        return apiRepository.findAllProjects();
    }

    @Override
    public RequestResponse deleteById(String id){
        ApiProject project = apiRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Project does not exists!"));
        apiRepository.deleteById(id);
        return RequestResponse.success("Project has been deleted!");
    }

    @Override
    public List<ApiProject> findByUser(String teamName) {
//        User user = userRepository.findByUsername(username);
//        return apiRepository.findByUsersIn(user);
        return apiRepository.findByTeamsIn(teamName);
    }

    @Override
    public ProjectCreateResponse createProject(ProjectCreateRequest request) {
        ApiProject project = new ApiProject();
        project.setBasePath(request.getBasePath());
        project.setHost(request.getHost());
        project.setInfo(oMapper.convertValue(request.getInfo(), ProjectInfo.class));
        project.setGithubProject(new Github());
        project.getInfo().setSignature(UUID.randomUUID().toString());
        project.getGithubProject().setSignature(UUID.randomUUID().toString());
        project.setSignature(UUID.randomUUID().toString());
        apiRepository.save(project);

        ProjectCreateResponse response = new ProjectCreateResponse();
        response.setStatusToSuccess();
        response.setMessage("Project has been created!");
//        response.setApiProject(project);
        response.setProjectId(project.getId());
        return response;
    }

    @Override
    public RequestResponse grantTeamAccess(String id, String teamName) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        RequestResponse response = new RequestResponse();
        Team team = Optional.ofNullable(teamRepository.findByName(teamName))
                .orElseThrow(() -> new DataNotFoundException("Team does not exists!"));
        UserProfileResponse profile = oMapper.convertValue(auth.getPrincipal(), UserProfileResponse.class);
        System.out.println("Username: " + profile.getUsername());
        if (profile.getUsername().equals(team.getCreator())) {
            ApiProject apiProject = apiRepository.findById(id)
                    .orElseThrow(() -> new DataNotFoundException("Api project does not exists!"));
            if (!apiProject.getTeams().contains(teamName)) apiProject.getTeams().add(teamName);
            apiRepository.save(apiProject);
            response.setStatusToSuccess();
            response.setMessage("Team has been assigned to project!");
            return response;
        }
        else {
            throw new InvalidRequestException("You are not this team creator!");
        }
    }

}
