package com.future.apix.request;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class ProjectCreateRequest {
    String basePath, host;

    @Data
    public static class ProjectInfo {
        @NotNull
        String title, version;
        String description, termsOfService;
    }

    ProjectInfo info;
}
