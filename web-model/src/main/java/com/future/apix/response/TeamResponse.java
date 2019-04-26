package com.future.apix.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.future.apix.entity.Team;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)

public class TeamResponse extends RequestResponse {
    Team team;
}
