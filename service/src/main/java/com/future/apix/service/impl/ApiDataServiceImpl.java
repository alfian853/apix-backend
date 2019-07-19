package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.Team;
import com.future.apix.entity.User;
import com.future.apix.entity.apidetail.Github;
import com.future.apix.entity.apidetail.ProjectInfo;
import com.future.apix.entity.teamdetail.Member;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.repository.TeamRepository;
import com.future.apix.repository.UserRepository;
import com.future.apix.request.ProjectCreateRequest;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.UserProfileResponse;
import com.future.apix.service.ApiDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
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

    @Autowired
    private UserRepository userRepository;

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

        Team team = setTeam(request.getIsNewTeam(), request.getTeam());
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

    private Team setTeam(Boolean isNewTeam, String teamName){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(!isNewTeam) { // if team is exists
            Team team = Optional.ofNullable(teamRepository.findByName(teamName))
                .orElseThrow(() -> new DataNotFoundException("Team does not exists!"));
            return team;
        }
        else { // if team does not exists, and create new team with authenticated user as the owner
            Team newTeam = new Team();
            UserProfileResponse convertUser = oMapper.convertValue(auth.getPrincipal(), UserProfileResponse.class);
            User user = Optional.ofNullable(userRepository.findByUsername(convertUser.getUsername()))
                .orElseThrow(() -> new DataNotFoundException("User is not exists!"));
            newTeam.setName(teamName);
            newTeam.setCreator(user.getUsername());
            newTeam.getMembers().add(new Member(user.getUsername(), true));
            newTeam.setAccess("private");
            newTeam = teamRepository.save(newTeam);

            user.getTeams().add(newTeam.getName());
            userRepository.save(user);

            return newTeam;
        }
    }
}
