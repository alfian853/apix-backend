package com.future.apix.controller;

import com.future.apix.entity.Team;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.TeamRepository;
import com.future.apix.request.TeamCreateRequest;
import com.future.apix.request.TeamGrantMemberRequest;
import com.future.apix.request.TeamInviteRequest;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.TeamService;
import com.mongodb.client.result.UpdateResult;
import org.omg.CORBA.DynAnyPackage.Invalid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/teams")
public class TeamController {

    @Autowired
    TeamService teamService;

    @GetMapping
    public List<Team> getTeams() {
        return teamService.getTeams();
    }

    @GetMapping("/my-team")
    public List<Team> getMyTeams(Authentication auth){
        return teamService.getMyTeam(auth);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RequestResponse createTeam(@RequestBody @Valid TeamCreateRequest teamCreateRequest) {
        Team newTeam = teamService.createTeam(teamCreateRequest);
        return RequestResponse.success("Team is created!");
    }

    @GetMapping("/{name}")
    public Team getTeamByName(@PathVariable("name") String name) {
        return teamService.getTeamByName(name);
    }

    @DeleteMapping("/{name}")
    public RequestResponse deleteTeam(@PathVariable("name") String name) {
        return teamService.deleteTeam(name);
    }

//  ===============================================================================================
    @PutMapping("/{name}/invite")
    public RequestResponse inviteMembersToTeam(@PathVariable("name") String name, @RequestBody
        TeamInviteRequest request) {
        return teamService.inviteMembersToTeam(name, request);
    }

    @PutMapping("/{name}/grant")
    public RequestResponse grantTeam(@PathVariable("name") String name, @RequestBody TeamInviteRequest request) {
        if(request.getInvite()) return teamService.grantTeamAccess(name, request);
        else throw new InvalidRequestException("Invalid Request!");
    }

    @PutMapping("/{name}/remove")
    public RequestResponse removeMembersFromTeam(@PathVariable("name") String name, @RequestBody TeamInviteRequest request) {
        if (!request.getInvite()) return teamService.removeMembersFromTeam(name, request);
        else throw new InvalidRequestException("Invalid Request!");
    }

}
