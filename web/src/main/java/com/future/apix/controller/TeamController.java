package com.future.apix.controller;

import com.future.apix.entity.Team;
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

    @GetMapping("/{name}")
    public TeamResponse getTeamByName(@PathVariable("name") String name) {
        return teamService.getTeamByName(name);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponse createTeam(@RequestBody @Valid Team team) {
        return teamService.createTeam(team);
    }
}
