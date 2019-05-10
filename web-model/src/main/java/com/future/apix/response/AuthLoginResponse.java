package com.future.apix.response;

import lombok.Data;

@Data
public class AuthLoginResponse extends RequestResponse {
    private String username;
    private String token;
}
