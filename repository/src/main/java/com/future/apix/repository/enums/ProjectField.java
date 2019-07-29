package com.future.apix.repository.enums;

import java.util.*;

public enum ProjectField implements MongoEntityField{
    TITLE("info.title"),
    HOST("host"),
    OWNER("projectOwner.creator"),
    REPO("githubProject.repo");

    ProjectField(String mongoField) {
        this.mongoFieldVal = mongoField;
    }

    String mongoFieldVal;

    private static Map<String, ProjectField> projectFieldMap = new HashMap<>();
    private static List<MongoEntityField> projectFieldList;
    static {
        Arrays.stream(ProjectField.values()).forEach(field -> {
            projectFieldMap.put(field.toString(), field);
        });
        projectFieldList = Arrays.asList(ProjectField.values());
    }

    @Override
    public ProjectField getMongoField(String field){
        field = field.toUpperCase();
        if(projectFieldMap.containsKey(field)){
            return projectFieldMap.get(field);
        }
        throw new RuntimeException("invalid field");
    }

    @Override
    public String getMongoFieldValue() {
        return mongoFieldVal;
    }

    @Override
    public List<MongoEntityField> getMongoFieldList() {
        return projectFieldList;
    }
}
