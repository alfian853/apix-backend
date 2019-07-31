package com.future.apix.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserCreateResponse extends RequestResponse{
    @JsonProperty("new_user")
    private String userId;

}
