package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.Team;
import com.future.apix.entity.User;
import com.future.apix.entity.enumeration.TeamAccess;
import com.future.apix.entity.teamdetail.Member;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.DuplicateEntryException;
import com.future.apix.exception.InvalidAuthenticationException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.TeamRepository;
import com.future.apix.repository.UserRepository;
import com.future.apix.request.CreateTeamRequest;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.UserProfileResponse;
import com.future.apix.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeamServiceImpl implements TeamService {
    @Autowired
    TeamRepository teamRepository;

    @Autowired
    UserRepository userRepository;

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
        else throw new InvalidRequestException("User is not authenticated!");
    }

    @Override
    public Team getTeamByName(String name) {
        return Optional.ofNullable(teamRepository.findByName(name))
            .orElseThrow(() -> new DataNotFoundException("Team is not found!"));
    }

    @Override
    public Team createTeam(CreateTeamRequest request){
        Team existTeam = teamRepository.findByName(request.getTeamName());

        if(existTeam == null){
            User teamCreator = Optional.ofNullable(
                    userRepository.findByUsername(request.getCreator())
            ).orElseThrow(
                () -> new DataNotFoundException("user not found!")
            );
            if(!teamCreator.getTeams().contains(request.getTeamName())){
                teamCreator.getTeams().add(request.getTeamName());
                userRepository.save(teamCreator);
            }

            Team newTeam = new Team();
            newTeam.setCreator(request.getCreator());
            newTeam.setAccess(request.getAccess());
            newTeam.setDivision(request.getDivision());
            newTeam.setName(request.getTeamName());

            request.getMembers().forEach(name -> {
                User user = userRepository.findByUsername(name);
                if (!user.getTeams().contains(newTeam.getName())) user.getTeams().add(newTeam.getName());

                newTeam.getMembers().add(new Member(name, request.getAccess().equals(TeamAccess.PUBLIC)));
                userRepository.save(user);
            });

            teamRepository.save(newTeam);
            return newTeam;
        }
        throw new DuplicateEntryException("Team name is already exists!");
    }

    @Override
    public RequestResponse editTeam(String name, Team team) {
        Team existTeam = Optional.ofNullable(teamRepository.findByName(name))
            .orElseThrow(() -> new DataNotFoundException("Team does not exist!"));
        RequestResponse response = new RequestResponse();
        for(Member member : team.getMembers()){
            Boolean memberAvailable = false;
            for(Member existMember: existTeam.getMembers()){
                if (existMember.equals(member) || existMember.getUsername().equals(member.getUsername()))
                    memberAvailable = true;
            }
            if(!memberAvailable) {
                existTeam.getMembers().add(member);
            }
        }
        teamRepository.save(existTeam);
        response.setStatusToSuccess();
        response.setMessage("Members have been invited!");
        return response;
    }
}
