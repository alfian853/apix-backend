package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.Team;
import com.future.apix.entity.User;
import com.future.apix.entity.teamdetail.Member;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.DuplicateEntryException;
import com.future.apix.exception.InvalidAuthenticationException;
import com.future.apix.repository.TeamRepository;
import com.future.apix.repository.UserRepository;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.TeamResponse;
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
        else throw new InvalidAuthenticationException("User is not authenticated!");
    }

    @Override
    public Team getTeamByName(String name) {
        return Optional.ofNullable(teamRepository.findByName(name))
            .orElseThrow(() -> new DataNotFoundException("Team is not found!"));
    }

    @Override
    public RequestResponse createTeam(Team team) {
        Team existTeam = teamRepository.findByName(team.getName());
        RequestResponse response = new RequestResponse();

        if(existTeam == null) { /* Add team to team creator*/
            User creator = userRepository.findByUsername(team.getCreator());
            if (!creator.getTeams().contains(team.getName())) creator.getTeams().add(team.getName());
            userRepository.save(creator);

            Team createTeam = teamRepository.save(team);
            response.setStatusToSuccess();
            response.setMessage("Team is created!");

            // Add team to each of User
            for (Member member: createTeam.getMembers()) {
                User user = userRepository.findByUsername(member.getUsername());
                if (!user.getTeams().contains(createTeam.getName())) user.getTeams().add(createTeam.getName());
                userRepository.save(user);
            }
            return response;
        }
        else throw new DuplicateEntryException("Team name is already exists!");
    }

    @Override
    public RequestResponse editTeam(String name, Team team) {
        Team existTeam = Optional.ofNullable(teamRepository.findByName(name))
            .orElseThrow(() -> new DataNotFoundException("Team does not exists!"));
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

    /*
    @Override
    public RequestResponse grantTeamAccess(String name, List<Member> members) {
        if (members.size() == 0) throw new DataNotFoundException("There is no member to be granted!");
        String failedName = "";
        Team team = teamRepository.findByName(name);
        if (team != null) {
            for (Member member : members) {
                String memberName = member.getUsername();
                User user = userRepository.findByUsername(memberName);
                if (user == null) failedName += memberName + ", ";
                else if (user!=null && !user.getTeams().contains(name)) { // update in User if not yet belong to team
                    if (!user.getTeams().contains(name)) user.getTeams().add(name);
                    userRepository.save(user);
                }
                int idx = team.getMembers().indexOf(member);
                team.getMembers().get(idx).setGrant(!member.getGrant()); // jadi di reverse -> if grant = false jadi TRUE
            }
            teamRepository.save(team);

            RequestResponse response = new RequestResponse();
            if (failedName != "") {
                response.setStatusToFailed();
                response.setMessage("Members: " + failedName + "is failed to updated!");
            } else {
                response.setStatusToSuccess();
                response.setMessage("Team members grant have been updated!");
            }
            return response;
        }
        else throw new DataNotFoundException("Team is not found!");
    }

     */
}
