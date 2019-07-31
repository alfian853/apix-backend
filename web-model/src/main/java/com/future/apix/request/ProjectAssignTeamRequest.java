package com.future.apix.request;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProjectAssignTeamRequest {
    private String assignType;
    private String teamName;
}
