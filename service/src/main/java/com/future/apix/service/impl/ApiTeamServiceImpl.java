package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.Team;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidAuthenticationException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
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
    private ApiRepository apiRepository;

    @Override
    public RequestResponse grantTeamAccess(String id, ProjectAssignTeamRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        RequestResponse response = new RequestResponse();
        String assignType = request.getAssignType();
        String teamName = request.getTeamName();
        Team team = Optional.ofNullable(teamRepository.findByName(teamName))
            .orElseThrow(() -> new DataNotFoundException("Team does not exists!"));
        UserProfileResponse profile = oMapper.convertValue(auth.getPrincipal(), UserProfileResponse.class);
        ApiProject apiProject = apiRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException("Api project does not exists!"));
        if (profile.getUsername().equals(apiProject.getProjectOwner().getCreator())) {
            if (assignType.equals("grant") && !apiProject.getTeams().contains(teamName)) {
                apiProject.getTeams().add(teamName);
                apiRepository.save(apiProject);
                response.setStatusToSuccess();
                response.setMessage("Team has been assigned to project!");
                return response;
            }
            else if(assignType.equals("ungrant") && apiProject.getTeams().contains(teamName)){
                apiProject.getTeams().remove(teamName);
                apiRepository.save(apiProject);
                response.setStatusToSuccess();
                response.setMessage("Team has been removed from project!");
                return response;
            }
            else throw new InvalidRequestException("Team is already in the project!");
        }
        else {
            throw new InvalidRequestException("You are unauthorized to grant team!");
        }
    }

}
