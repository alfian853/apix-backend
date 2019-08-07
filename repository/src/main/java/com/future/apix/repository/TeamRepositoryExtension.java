package com.future.apix.repository;

import com.future.apix.entity.Team;
import com.mongodb.bulk.UpdateRequest;
import com.mongodb.client.result.UpdateResult;

import java.util.List;

public interface TeamRepositoryExtension {
    UpdateResult removeMemberFromTeam(String teamName, String memberName);
    UpdateResult inviteMemberToTeam(String teamName, String memberName, boolean isInvite);

    // when delete team, should remove from projects and users too
    UpdateResult removeTeamFromMember(String teamName, String memberName);
    // only remove team from project where the team is not project owner
    UpdateResult removeTeamFromProject(String teamName, String projectId);
}
