package com.future.apix.entity.apidetail;

import lombok.Data;

import java.util.HashMap;

@Data
public class SecurityScheme {
    // In Swagger 2.0 called Security Scheme Object

    String type, description, name, in, flow, authorizationUrl, tokenUrl;
    HashMap<String, String> scopes = new HashMap<>();
}
