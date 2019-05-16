package com.future.apix.response.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubBranchResponse {
    private String name;
    private String repoName;
    private String sha;
}
