package com.future.apix.response.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.InputStream;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GithubContentResponse {
    private String type;
    private String encoding;
    private long size;
    private String name;
    private String repoName;
    private String sha;
    private String path;
    private String content;
    private String url;
    private String htmlUrl;

}
