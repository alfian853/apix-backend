package com.future.apix.service;

import com.future.apix.entity.Team;
import com.future.apix.entity.teamdetail.Member;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.TeamResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface TeamService {
    List<Team> getTeams();

    Team getTeamByName(String name);

    RequestResponse createTeam(Team team);

    List<Team> getMyTeam(Authentication authentication);

    RequestResponse grantTeamAccess(String name, List<Member> members);
}
