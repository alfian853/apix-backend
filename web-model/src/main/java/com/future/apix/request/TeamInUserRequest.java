package com.future.apix.request;

import lombok.Data;

import java.util.List;

@Data
public class TeamInUserRequest {
    String username;
    List<String> teams;
}
