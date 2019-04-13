package com.future.apix.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class UserProfileResponse extends RequestResponse {
    // hide id and password to profile

    String username;
    List<String> roles, teams;
}
