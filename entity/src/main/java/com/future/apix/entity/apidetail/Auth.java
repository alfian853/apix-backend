package com.future.apix.entity.apidetail;

import lombok.Data;

import java.util.HashMap;

@Data
public class Auth {
    String type, authorizationUrl, flow;
    HashMap<String, String> scopes = new HashMap<>();
}
