package com.future.apix.repository;

import com.future.apix.entity.ApiProject;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ApiRepository extends MongoRepository<ApiProject, String>{

    @Query(value = "{}", fields = "{_id: 1, basePath: 1, host: 1, info: 1, teams: 1, updatedAt: 1}")
    List<ApiProject> findAllProjects();

    // List of Projects with users
    List<ApiProject> findByTeamsIn(String teamName);

    /*
    @Query(value = "{'teams': {$all: [?0] }}")
    List<ApiProject> cariUser(String username);

    List<ApiProject> findByUsers(String username);
    */
}
