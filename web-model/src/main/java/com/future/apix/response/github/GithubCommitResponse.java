package com.future.apix.response.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GithubCommitResponse {
    private String sha;
    private String message;
    private GithubUserResponse committer;
    private Date commitDate;
    private GithubRepoResponse owner;

}
