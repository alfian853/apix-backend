package com.future.apix.repository;

import com.future.apix.entity.Team;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TeamRepository extends MongoRepository<Team, String> {
    List<Team> findByAccessEquals(String access);

    Team findByName(String name);

    List<Team> findByMembersUsername(String username);
}
