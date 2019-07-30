package com.future.apix.service;

import com.future.apix.entity.Team;
import com.future.apix.request.TeamCreateRequest;
import com.future.apix.request.TeamGrantMemberRequest;
import com.future.apix.response.RequestResponse;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface TeamService {
    List<Team> getTeams();

    Team getTeamByName(String name);

    Team createTeam(TeamCreateRequest request);

    RequestResponse inviteMembers(String name, Team team);

    List<Team> getMyTeam(Authentication authentication);

    RequestResponse deleteTeam(String name);

    RequestResponse grantTeamAccess(String name, TeamGrantMemberRequest request);
}
