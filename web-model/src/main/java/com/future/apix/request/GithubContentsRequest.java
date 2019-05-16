package com.future.apix.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GithubContentsRequest {
//    https://developer.github.com/v3/repos/contents

    /**
     * Create a file required message, content
     * Update a file required message, content, sha
     * Delete a file required message, sha
     */

    @NotEmpty(message = "Message cannot be empty")
    @NotNull(message = "Message cannot be null")
    private String message;

    @NotEmpty(message = "Content cannot be empty")
    @NotNull(message = "Content cannot be null")
    private String content;

    private String sha;
    private String branch;

    private GithubCommitterRequest committer;
    private GithubCommitterRequest author;
}
