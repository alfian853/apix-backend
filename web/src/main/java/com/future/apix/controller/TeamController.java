package com.future.apix.controller;

import com.future.apix.entity.Team;
import com.future.apix.repository.TeamRepository;
import com.future.apix.request.TeamCreateRequest;
import com.future.apix.request.TeamGrantMemberRequest;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.TeamService;
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

    @Autowired
    TeamRepository teamRepository;

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

    @PutMapping("/{name}")
    public RequestResponse inviteMembers(@PathVariable("name") String name,
                                      @RequestBody @Valid Team team){
        return teamService.inviteMembers(name, team);
    }

    @GetMapping("/{name}")
    public Team getTeamByName(@PathVariable("name") String name) {
        return teamService.getTeamByName(name);
    }

    @DeleteMapping("/{name}")
    public RequestResponse deleteTeam(@PathVariable("name") String name) {
        return teamService.deleteTeam(name);
    }

    @PutMapping("/{name}/grant")
    public RequestResponse grantTeam(@PathVariable("name") String name, @RequestBody TeamGrantMemberRequest request) {
        return teamService.grantTeamAccess(name, request);
    }

    @PutMapping("/{name}/repo")
    public Team grantTeamRepo(@PathVariable("name") String name, @RequestParam String member) {
//        boolean res = teamRepository.removeMemberFromTeam(name, member);
//        if (res) return RequestResponse.success("success");
//        else return RequestResponse.failed("failed");
        return teamRepository.removeMemberFromTeam(name, member);
    }

}
