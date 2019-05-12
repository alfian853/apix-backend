package com.future.apix.response.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubRepoResponse {
    private long id;
    private String name;
    private String fullName;
    private String description;

    private String ownerName;
}
