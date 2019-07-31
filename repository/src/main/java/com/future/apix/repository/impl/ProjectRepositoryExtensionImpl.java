package com.future.apix.repository.impl;

import com.future.apix.entity.ApiProject;
import com.future.apix.repository.ProjectRepositoryExtension;
import com.future.apix.repository.enums.MongoEntityField;
import com.future.apix.repository.enums.ProjectField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProjectRepositoryExtensionImpl implements ProjectRepositoryExtension {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public MongoTemplate getMongoTemplate() {
        return mongoTemplate;
    }

    @Override
    public Class<ApiProject> getEntityClass() {
        return ApiProject.class;
    }

    @Override
    public List<MongoEntityField> getFieldList() {
        return ProjectField.TITLE.getMongoFieldList();
    }

}
