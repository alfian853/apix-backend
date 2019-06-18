package com.future.apix.service;

import com.future.apix.entity.Team;
import com.future.apix.response.TeamResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface TeamService {
    List<Team> getTeams();

    TeamResponse getTeamByName(String name);

    TeamResponse createTeam(Team team);

    List<Team> getMyTeam(Authentication authentication);
}