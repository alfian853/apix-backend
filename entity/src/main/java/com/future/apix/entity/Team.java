package com.future.apix.entity;

import com.future.apix.entity.teamdetail.Member;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
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

    private String creator; // to give access
    private List<Member> members;

    @CreatedDate
    Date createdAt;

}
