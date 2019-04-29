package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.Team;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.DuplicateEntryException;
import com.future.apix.exception.InvalidAuthenticationException;
import com.future.apix.repository.TeamRepository;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.TeamResponse;
import com.future.apix.response.UserProfileResponse;
import com.future.apix.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamServiceImpl implements TeamService {
    @Autowired
    TeamRepository teamRepository;

    @Autowired
    private ObjectMapper oMapper;

    @Override
    public List<Team> getTeams() {
       return teamRepository.findAll();
    }

    @Override
    public List<Team> getMyTeam(Authentication authentication) {
        if (authentication != null) {
            UserProfileResponse profile = oMapper.convertValue(authentication.getPrincipal(), UserProfileResponse.class);
            return teamRepository.findByMembersUsername(profile.getUsername());
        }
        else throw new InvalidAuthenticationException("User is not authenticated!");
    }

    @Override
    public TeamResponse getTeamByName(String name) {
        Team team = teamRepository.findByName(name);
        if (team != null) {
            TeamResponse response = new TeamResponse();
            response.setStatusToSuccess();
            response.setMessage("Team is found!");
            response.setTeam(team);
            return response;
        }
        else throw new DataNotFoundException("Team is not found!");
    }

    @Override
    public TeamResponse createTeam(Team team) {
        Team existTeam = teamRepository.findByName(team.getName());
        TeamResponse response = new TeamResponse();
        if(existTeam == null) {
            teamRepository.save(team);
            response.setTeam(team);
            response.setStatusToSuccess();
            response.setMessage("Team is created!");
            return response;
        }
        else throw new DuplicateEntryException("Team name is already exists!");
    }
}
