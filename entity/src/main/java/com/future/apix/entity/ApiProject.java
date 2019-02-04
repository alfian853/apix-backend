package com.future.apix.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.HashMap;

@Data
@Document("ApiProjects")
public class ApiProject implements Serializable {

    String id;
    String basePath;
    HashMap<String, ApiSection> sections;

}
