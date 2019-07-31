package com.future.apix.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.future.apix.entity.ApiProject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ProjectCreateResponse extends RequestResponse {

//    @JsonProperty("newProject")
//    ApiProject apiProject;

    @JsonProperty("new_project")
    String projectId;
}
