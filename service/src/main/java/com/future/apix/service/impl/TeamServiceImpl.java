package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.Team;
import com.future.apix.entity.User;
import com.future.apix.entity.enumeration.TeamAccess;
import com.future.apix.entity.teamdetail.Member;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.DuplicateEntryException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ProjectRepository;
import com.future.apix.repository.TeamRepository;
import com.future.apix.repository.UserRepository;
import com.future.apix.request.TeamCreateRequest;
import com.future.apix.request.TeamGrantMemberRequest;
import com.future.apix.request.TeamInviteRequest;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.UserProfileResponse;
import com.future.apix.service.TeamService;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TeamServiceImpl implements TeamService {
    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

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
    public Team createTeam(TeamCreateRequest request){
        Team existTeam = teamRepository.findByName(request.getTeamName());
        if(existTeam == null){
            User teamCreator = Optional.ofNullable(
                userRepository.findByUsername(request.getCreator())
            ).orElseThrow(
                () -> new DataNotFoundException("User creator not found!")
            );
            if(!teamCreator.getTeams().contains(request.getTeamName())){
                teamCreator.getTeams().add(request.getTeamName());
                userRepository.save(teamCreator);
            }

            Team newTeam = new Team();
            newTeam.setCreator(request.getCreator());
            newTeam.setAccess(request.getAccess());
            newTeam.setName(request.getTeamName());
            newTeam.getMembers().add(new Member(teamCreator.getUsername(), true));
            request.getMembers().forEach(name -> {
                User user = userRepository.findByUsername(name);
                if (user != null && !user.getTeams().contains(newTeam.getName()))
                    user.getTeams().add(newTeam.getName());

                newTeam.getMembers().add(new Member(name, request.getAccess().equals(TeamAccess.PUBLIC)));
                userRepository.save(user);
            });

            teamRepository.save(newTeam);
            return newTeam;
        }
        throw new DuplicateEntryException("Team name is already exists!");
    }

    @Override
    public RequestResponse deleteTeam(String name) {
        Team existTeam = Optional.ofNullable(teamRepository.findByName(name))
            .orElseThrow(() -> new DataNotFoundException("Team does not exist!"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserProfileResponse profile = oMapper.convertValue(auth.getPrincipal(), UserProfileResponse.class);
        if (existTeam.getCreator().equals(profile.getUsername())){
            // delete team from each user
            List<User> users = userRepository.findByTeams(name);
            for (User user : users) {
                teamRepository.removeTeamFromMember(name, user.getUsername());
            }
            // delete team from projects
            List<ApiProject> projects = projectRepository.findByTeams(name);
            for (ApiProject project : projects) {
                teamRepository.removeTeamFromProject(name, project.getId());
            }
            // only delete where team as member, cannot delete where team as project owner
            if (!projectRepository.findByTeams(name).isEmpty())
                throw new InvalidRequestException("There are projects under your team as owner!");

            teamRepository.deleteById(existTeam.getId());
            return RequestResponse.success("Team has been deleted!");
        }
        else throw new InvalidRequestException("You are not allowed to delete this team!");
    }

    @Override
    public RequestResponse inviteMembersToTeam(String name, TeamInviteRequest request){
        Team existTeam = Optional.ofNullable(teamRepository.findByName(name))
            .orElseThrow(() -> new DataNotFoundException("Team does not exist!"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserProfileResponse profile = oMapper.convertValue(auth.getPrincipal(), UserProfileResponse.class);

        // only team creator has privilege to invite members
        if (profile.getUsername().equals(existTeam.getCreator())) {
            int totalSuccessMember = 0;
            for (String memberName : request.getMembers()) {
                User user = userRepository.findByUsername(memberName);
                if (user == null) continue;
                UpdateResult result = teamRepository.inviteMemberToTeam(name, memberName, request.getInvite());
                totalSuccessMember += result.getMatchedCount();
            }
            if (totalSuccessMember == request.getMembers().size())
                return RequestResponse.success("Members have been invited!");
            else
                return RequestResponse.failed("Failed in adding members!");
        }
        else throw new InvalidRequestException("You do not have privilege to add members!");
    }


    @Override
    public RequestResponse grantTeamAccess(String name, TeamInviteRequest request) {
        List<String> members = request.getMembers();
        if (members.size() == 0) throw new DataNotFoundException("There is no member to be granted!");
        String failedName = "";
        Team team = Optional.ofNullable(teamRepository.findByName(name))
            .orElseThrow(() -> new DataNotFoundException("Team does not exist!"));

        for (String memberName : members) {
            User user = userRepository.findByUsername(memberName);
            if (user == null) {
                failedName += memberName + ", ";
                continue;
            }
            // update in User if not yet belong to team
            if (!user.getTeams().contains(name)) user.getTeams().add(name);
            userRepository.save(user);
            // update grant member to team to true
            teamRepository.inviteMemberToTeam(name, memberName, request.getInvite());
        }
        if (!failedName.equals("")) {
            return RequestResponse.failed("Members: " + failedName + "is failed to updated!");
        } else {
            return RequestResponse.success("Members have joined team " + name + "!");
        }
    }

    @Override
    public RequestResponse removeMembersFromTeam(String name, TeamInviteRequest request){
        Team existTeam = Optional.ofNullable(teamRepository.findByName(name))
            .orElseThrow(() -> new DataNotFoundException("Team does not exist!"));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserProfileResponse profile = oMapper.convertValue(auth.getPrincipal(), UserProfileResponse.class);

        // only team creator has privilege to remove members
        if (profile.getUsername().equals(existTeam.getCreator())) {
            int totalRemovedMember = 0;
            String failedRemovedName = "";
            for (String memberName : request.getMembers()) {
                User user = userRepository.findByUsername(memberName);
                if (user == null) {
                    failedRemovedName += memberName + ", ";
                    continue;
                }
                if (user.getTeams().contains(name)) user.getTeams().remove(name);
                userRepository.save(user);
                // remove member from team
                UpdateResult result = teamRepository.removeMemberFromTeam(name, memberName);
                totalRemovedMember += result.getModifiedCount();
            }
            if (totalRemovedMember == request.getMembers().size())
                return RequestResponse.success("Members have been removed from team " + name + "!");
            else
                return RequestResponse.failed("Failed in removing members!");
        }

        else throw new InvalidRequestException("You do not have privilege to remove members!");
    }

}
