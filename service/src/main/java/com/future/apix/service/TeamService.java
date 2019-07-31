package com.future.apix.service;

import com.future.apix.entity.Team;
import com.future.apix.request.TeamCreateRequest;
import com.future.apix.request.TeamGrantMemberRequest;
import com.future.apix.request.TeamInviteRequest;
import com.future.apix.response.RequestResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface TeamService {
    List<Team> getTeams();

    Team getTeamByName(String name);

    Team createTeam(TeamCreateRequest request);

    List<Team> getMyTeam(Authentication authentication);

    RequestResponse deleteTeam(String name);

    // User can confirm invitation from team
    RequestResponse grantTeamAccess(String name, TeamInviteRequest request);

    // Team creator invite members
    RequestResponse inviteMembersToTeam(String name, TeamInviteRequest request);

    // Team creator remove members
    RequestResponse removeMembersFromTeam(String name, TeamInviteRequest request);
}
