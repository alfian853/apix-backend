package com.future.apix.response.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GithubContentUpdateResponse {
    private GithubCommitResponse commit;
    private GithubContentResponse content;
}
