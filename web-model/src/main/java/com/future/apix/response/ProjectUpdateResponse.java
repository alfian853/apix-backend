package com.future.apix.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProjectUpdateResponse extends RequestResponse {

    @JsonProperty("new_signature")
    String newSignature;
}
