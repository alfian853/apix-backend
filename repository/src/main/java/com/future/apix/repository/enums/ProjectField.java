package com.future.apix.repository.enums;

import java.util.*;

public enum ProjectField implements MongoEntityField{
    TITLE("title","info.title"),
    HOST("host","host"),
    OWNER("owner","projectOwner.creator"),
    REPO("repository","githubProject.repo"),
    UPDATED_AT("updated_at","updatedAt");

    private String apiField, mongoFieldVal;

    ProjectField(String apiField, String mongoField) {
        this.mongoFieldVal = mongoField;
        this.apiField = apiField;
    }


    private static Map<String, ProjectField> projectFieldMap = new HashMap<>();
    private static List<MongoEntityField> projectFieldList;
    static {
        Arrays.stream(ProjectField.values()).forEach(field -> {
            projectFieldMap.put(field.apiField, field);
        });
        projectFieldList = Arrays.asList(ProjectField.values());
    }

    public static ProjectField getProjectField(String field){
        field = field.toLowerCase();
        if(projectFieldMap.containsKey(field)){
            return projectFieldMap.get(field);
        }
        throw new RuntimeException("invalid field");
    }

    @Override
    public String getMongoFieldValue() {
        return mongoFieldVal;
    }

//    @Override
//    public boolean isSearchable() {
//        return null;
//    }

    @Override
    public List<MongoEntityField> getMongoFieldList() {
        return projectFieldList;
    }
}
