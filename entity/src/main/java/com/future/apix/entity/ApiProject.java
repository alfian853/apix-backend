package com.future.apix.entity;

import com.future.apix.entity.apidetail.Definition;
import com.future.apix.entity.apidetail.ProjectInfo;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.HashMap;

@Data
@Document("ApiProjects")
public class ApiProject implements Serializable {

    @Id
    String id;

    String basePath,host;
    ProjectInfo info;
    HashMap<String, ApiSection> sections = new HashMap<>();
    HashMap<String, Definition> definitions = new HashMap<>();

}
