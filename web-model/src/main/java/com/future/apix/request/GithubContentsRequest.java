package com.future.apix.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GithubContentsRequest {
    @NotEmpty(message = "Message cannot be empty")
    @NotNull(message = "Message cannot be null")
    private String message; // commit message
    private String projectId;
    private String branch;
}
