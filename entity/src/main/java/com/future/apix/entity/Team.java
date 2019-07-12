package com.future.apix.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.future.apix.entity.teamdetail.Member;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document("Teams")
public class Team {
    @Id
    String id;

    @NotEmpty(message = "Team name must not be empty")
    private String name;

    private String division;

    @NotNull(message = "Access must not be null")
    private String access = "public";
    // 'private' require team creator for grant; 'public' for anyone to enter

    @NotEmpty(message = "Team creator name must not be empty")
    private String creator; // person who create the team and give access to member

    private List<Member> members = new ArrayList<>();

    @CreatedDate
    Date createdAt;

    @LastModifiedDate
    Date updatedAt;
}
