package com.future.apix.repository.impl;

import com.future.apix.entity.ApiProject;
import com.future.apix.repository.RepositoryExtension;
import com.future.apix.repository.enums.MongoEntityField;
import com.future.apix.repository.enums.ProjectField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

public class ProjectRepositoryExtensionImpl implements RepositoryExtension<ApiProject> {

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
        return ProjectField.HOST.getMongoFieldList();
    }

}
