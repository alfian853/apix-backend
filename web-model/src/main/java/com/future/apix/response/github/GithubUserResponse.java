package com.future.apix.response.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubUserResponse {
    private String login;
    private long id;
//    private String avatarUrl;
//    private String gravatarId;
//    private String url;
//    private String htmlUrl;
    private String name;
//    private String company;
//    private String blog;
//    private String location;
    private String email;
//    private int publicRepoCount;
//    private int publicGistCount;
//    private int followersCount;
//    private int followingCount;
//    private String createdAt;
//    private String updatedAt;

//    private Map<String, List<String>> responseHeaderFields;

    private Date date; // for GitUser (author or committer)


}
