package com.future.apix.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.future.apix.entity.apidetail.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.*;

@Data
@Document("ApiProjects")
public class ApiProject implements Serializable, Mappable {

//    https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md
    @Id
    String id;

    @Field("_signature")
    @JsonProperty("_signature")
    String signature = UUID.randomUUID().toString();

    String basePath,host;
    // String swagger, openapi; // swagger for version 2.0 and openapi for version 3.0
    ProjectInfo info;
    HashMap<String, ApiSection> sections = new HashMap<>();
    HashMap<String, Definition> definitions = new HashMap<>();

    List<String> schema;
    HashMap<String, SecurityScheme> securityDefinitions = new HashMap<>();
    Contact externalDocs;

    List<String> teams = new ArrayList<>(); // list of invited team member
    // String teams; // jika 1 project hanya 1 repo -> kalau ada yang gabungan, berarti harus buat team baru

    Team projectOwner; // only 1 team own the project and only team owner can change list of teams

    Github githubProject;

    @CreatedDate
    Date createdAt;

    @LastModifiedDate
    Date updatedAt;

}
