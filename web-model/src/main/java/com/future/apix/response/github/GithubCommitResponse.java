package com.future.apix.response.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.HashMap;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubCommitResponse {
    private String sha;
    private String message;
    private Date commitDate;
}
