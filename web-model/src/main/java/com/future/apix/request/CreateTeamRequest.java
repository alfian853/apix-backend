package com.future.apix.request;

import com.future.apix.entity.enumeration.TeamAccess;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTeamRequest {
    String creator;
    String teamName;
    List<String> members;
    TeamAccess access;
}
