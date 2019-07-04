package com.future.apix.controller;

import com.future.apix.entity.Team;
import com.future.apix.entity.teamdetail.Member;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.TeamResponse;
import com.future.apix.response.UserProfileResponse;
import com.future.apix.service.TeamService;
import com.future.apix.service.UserService;
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
    public RequestResponse createTeam(@RequestBody @Valid Team team) {
        return teamService.createTeam(team);
    }

    @PutMapping
    public RequestResponse editTeam(@RequestBody @Valid Team team){
        return teamService.editTeam(team);
    }

    @GetMapping("/{name}")
    public Team getTeamByName(@PathVariable("name") String name) {
        return teamService.getTeamByName(name);
    }

    @PutMapping("/{name}")
    public RequestResponse grantTeam(@PathVariable("name") String name, @RequestBody List<Member> members) {
        return teamService.grantTeamAccess(name, members);
    }
}
