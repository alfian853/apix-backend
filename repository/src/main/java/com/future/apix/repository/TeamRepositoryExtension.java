package com.future.apix.repository;

import com.future.apix.entity.Team;
import com.mongodb.bulk.UpdateRequest;
import com.mongodb.client.result.UpdateResult;

import java.util.List;

public interface TeamRepositoryExtension {
    UpdateResult removeMemberFromTeam(String teamName, String member);
}
