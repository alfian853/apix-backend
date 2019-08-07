package com.future.apix.repository;

import com.future.apix.entity.ApiProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProjectRepository extends MongoRepository<ApiProject, String>, ProjectRepositoryExtension {

    // List of Projects with users
    List<ApiProject> findByTeams(String teamName);
}
