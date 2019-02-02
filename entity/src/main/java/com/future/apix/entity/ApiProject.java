package com.future.apix.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

@Data
@Document("ApiProjects")
public class ApiProject implements Serializable {

    String id;
    List<ApiSection> sections;
}
