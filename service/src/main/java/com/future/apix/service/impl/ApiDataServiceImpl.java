package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.Team;
import com.future.apix.entity.User;
import com.future.apix.entity.apidetail.Github;
import com.future.apix.entity.apidetail.ProjectInfo;
import com.future.apix.entity.enumeration.TeamAccess;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.request.CreateTeamRequest;
import com.future.apix.request.ProjectCreateRequest;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.ApiDataService;
import com.future.apix.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class ApiDataServiceImpl implements ApiDataService {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ObjectMapper oMapper;

    @Autowired
    private TeamService teamService;

    @Override
    public ApiProject findById(String id) {
        return apiRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Project does not exists!"));
    }

    @Override
    public Page<ApiProject> findAll(Pageable pageable) {
        return apiRepository.findAll(pageable);
    }

    @Override
    public Page<ApiProject> findSearch(String search, Pageable pageable){
        return apiRepository.findBySearch(search, pageable);
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
        return apiRepository.findByTeamsIn(teamName);
    }

    @Override
    public ProjectCreateResponse createProject(ProjectCreateRequest request) {
        ApiProject project = new ApiProject();
        project.setBasePath(request.getBasePath());
        project.setHost(request.getHost());
        project.setInfo(oMapper.convertValue(request.getInfo(), ProjectInfo.class));

        Team team;

        if(request.getIsNewTeam()){
            CreateTeamRequest createTeamRequest = new CreateTeamRequest();
            User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            createTeamRequest.setCreator(user.getUsername());

            createTeamRequest.setMembers(Collections.singletonList(user.getUsername()));
            createTeamRequest.setTeamName(request.getTeam());
            createTeamRequest.setAccess(TeamAccess.PUBLIC);
            team = teamService.createTeam(createTeamRequest);
        }
        else{
            team = teamService.getTeamByName(request.getTeam());
        }
        project.setProjectOwner(team);
        project.getTeams().add(team.getName());

        project.setGithubProject(new Github());
        project.getInfo().setSignature(UUID.randomUUID().toString());
        project.getGithubProject().setSignature(UUID.randomUUID().toString());
        project.setSignature(UUID.randomUUID().toString());
        apiRepository.save(project);

        ProjectCreateResponse response = new ProjectCreateResponse();
        response.setStatusToSuccess();
        response.setMessage("Project has been created!");
        response.setProjectId(project.getId());
        return response;
    }
}
