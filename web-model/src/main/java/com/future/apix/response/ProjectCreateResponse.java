package com.future.apix.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.future.apix.entity.ApiProject;
import lombok.Data;

@Data
public class ProjectCreateResponse extends RequestResponse {

    @JsonProperty("newProject")
    ApiProject apiProject;
}
