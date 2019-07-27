package com.future.apix.request;

import com.future.apix.entity.enumeration.TeamAccess;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamRequest {

    @NotEmpty(message = "Team name must not be empty")
    private String teamName;

    private String division;

    @NotNull(message = "Access must not be null")
    private TeamAccess access;

    @NotEmpty(message = "Team creator name must not be empty")
    private String creator;

    private List<String> members;
}
