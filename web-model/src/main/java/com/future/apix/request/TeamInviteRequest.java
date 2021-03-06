package com.future.apix.request;

import com.future.apix.entity.teamdetail.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamInviteRequest {
    private String teamName;

    private List<String> members;

    private Boolean invite;
}
