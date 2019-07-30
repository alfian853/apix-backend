package com.future.apix.repository;

import com.future.apix.entity.ApiProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProjectRepository extends MongoRepository<ApiProject, String>, ProjectRepositoryExtension {

    @Query(value = "{}", fields = "{_id:1, basePath:1, host:1, info:1, teams:1, projectOwner:1, githubProject:1, updatedAt:1}")
    List<ApiProject> findAllProjects();

    // List of Projects with users
    List<ApiProject> findByTeamsIn(String teamName);

    @Query(value = "{}", fields = "{_id:1, basePath:1, host:1, info:1, teams:1, projectOwner:1, githubProject:1, updatedAt:1}")
    Page<ApiProject> findAll(Pageable pageable);

    @Query("{ $or: [ {'host': {$regex: ?0}}, {'info.title': {$regex: ?0}}, " +
            "{'projectOwner.creator': {$regex: ?0}}, " +
            "{'githubProject.owner': {$regex: ?0}}, {'githubProject.repo': {$regex: ?0}} ] }")
    Page<ApiProject> findBySearch(String search, Pageable pageable);
}
