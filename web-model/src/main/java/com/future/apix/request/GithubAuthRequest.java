package com.future.apix.request;

import lombok.Data;

@Data
public class GithubAuthRequest {
    private String user;
    private String password;
    private String token;
}
