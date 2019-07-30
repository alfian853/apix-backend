package com.future.apix.repository;

import com.future.apix.entity.Team;

import java.util.List;

public interface TeamRepositoryExtension {
    Team removeMemberFromTeam(String teamName, String member);
}
