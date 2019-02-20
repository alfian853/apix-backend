package com.future.apix.repository;

import com.future.apix.entity.ApiProject;
import com.future.apix.entity.ApiSection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ApiRepository extends MongoRepository<ApiProject, String>{

    @Query(value = "{}", fields = "{_id: 1, basePath: 1, host: 1, info: 1}")
    List<ApiProject> findAllProjects();
}
