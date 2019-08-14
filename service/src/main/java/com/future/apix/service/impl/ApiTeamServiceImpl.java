package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.Team;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ProjectRepository;
import com.future.apix.repository.TeamRepository;
import com.future.apix.request.ProjectAssignTeamRequest;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.UserProfileResponse;
import com.future.apix.service.ApiTeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ApiTeamServiceImpl implements ApiTeamService {
    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private ObjectMapper oMapper;

    @Autowired
    private ProjectRepository apiRepository;

    @Override
    public RequestResponse grantTeamAccess(String id, ProjectAssignTeamRequest request) {
        if (this.checkProjectOwner(id)) {
            String assignType = request.getAssignType();
            String teamName = request.getTeamName();
            ApiProject apiProject = apiRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Api project does not exists!"));
            if (assignType.equals("grant") && !apiProject.getTeams().contains(teamName)) {
                apiProject.getTeams().add(teamName);
                apiRepository.save(apiProject);
                return RequestResponse.success("Team has been assigned to project!");
            }
            else if(assignType.equals("ungrant") && apiProject.getTeams().contains(teamName)){
                apiProject.getTeams().remove(teamName);
                apiRepository.save(apiProject);
                return RequestResponse.success("Team has been removed to project!");
            }
            else throw new InvalidRequestException("Team is already in the project!");
        }
        else {
            throw new InvalidRequestException("You are unauthorized to grant team!");
        }
    }

    @Override
    public boolean checkProjectOwner(String id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserProfileResponse profile = oMapper.convertValue(auth.getPrincipal(), UserProfileResponse.class);
        ApiProject apiProject = apiRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException("Api project does not exists!"));
        if (profile.getUsername().equals(apiProject.getProjectOwner().getCreator())) {
            return true;
        }
        else return false;
    }

}
