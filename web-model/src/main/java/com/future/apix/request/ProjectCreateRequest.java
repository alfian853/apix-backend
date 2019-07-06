package com.future.apix.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProjectCreateRequest {
    String basePath, host;
    String team;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProjectInfo {
        @NotNull
        String title, version;
        String description, termsOfService;
    }

    ProjectInfo info;
}
