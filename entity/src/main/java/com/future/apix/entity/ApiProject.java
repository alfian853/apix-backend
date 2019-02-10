package com.future.apix.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.future.apix.entity.apidetail.*;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Data
@Document("ApiProjects")
public class ApiProject implements Serializable {

    @Id
    String id;

    //for validation of edition
    @Field("_signature")
    @JsonProperty("_signature")
    String signature = UUID.randomUUID().toString();

    String basePath,host, swagger;
    ProjectInfo info;
    HashMap<String, ApiSection> sections = new HashMap<>();
    HashMap<String, Definition> definitions = new HashMap<>();

    List<String> schemes;
    SecurityDefinition securityDefinitions;
    Contact externalDocs;

//    List<Tag> tags;
//    HashMap<String, Tag> tags = new HashMap<>();

}
