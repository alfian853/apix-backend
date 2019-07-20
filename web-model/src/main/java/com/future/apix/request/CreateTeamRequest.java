package com.future.apix.request;

import com.future.apix.entity.enumeration.TeamAccess;
import lombok.Data;

import java.util.List;

@Data
public class CreateTeamRequest {
    String creator;
    String teamName;
    List<String> members;
    TeamAccess access;
}
