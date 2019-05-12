package com.future.apix.response.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.InputStream;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubContentResponse {
    private String type;
    private String encoding;
    private long size;
    private String name;
    private String repoName;
    private String path;
    private String content;
    private String encondedContent;
    private String sha;
    private String url;
    private String htmlUrl;

}
