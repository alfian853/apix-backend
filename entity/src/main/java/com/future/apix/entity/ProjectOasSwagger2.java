package com.future.apix.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;

@Data
@Document("ApiProjectOasSwagger2")
public class ProjectOasSwagger2 {

    @Id
    String id;

    String projectId;

    //will be manually updated
    Date oasFileProjectUpdateDate;

    String oasFileName;

    Date generatedCodesProjectUpdatedDate;

    String generatedCodesFileName;

    HashMap<String, Object> oasSwagger2;
}
