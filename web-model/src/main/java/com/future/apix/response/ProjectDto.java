package com.future.apix.response;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ProjectDto {
    String id;
    String host;
    String title;
    String owner;
    String githubUsername;
    String repository;
    Date updatedAt;

}
