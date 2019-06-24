package com.future.apix.request;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class ProjectCreateRequest {
    String basePath, host;

    @Data
    @Builder
    public static class ProjectInfo {
        @NotNull
        String title, version;
        String description, termsOfService;
    }

    ProjectInfo info;
}
