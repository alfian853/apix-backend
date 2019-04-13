package com.future.apix.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;

@Data
@Document("api_project_oas_swagger2")
public class ProjectOasSwagger2 {

    @Id
    String id;

    String projectId;

    //will be manually updated
    Date updatedAt;

    HashMap<String, Object> oasSwagger2;
}
