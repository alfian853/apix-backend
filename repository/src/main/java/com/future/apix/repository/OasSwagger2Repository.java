package com.future.apix.repository;

import com.future.apix.entity.ProjectOasSwagger2;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OasSwagger2Repository extends MongoRepository<ProjectOasSwagger2, String> {

    Optional<ProjectOasSwagger2> findProjectOasSwagger2ByProjectId(String projectId);

}
