package com.future.apix.request;

import lombok.Data;

@Data
public class ProjectAssignTeamRequest {
    private String assignType;
    private String teamName;
}
