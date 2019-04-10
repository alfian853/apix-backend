package com.future.apix.entity;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotEmpty;

@Data
@Document("Teams")
public class Team {
    @NotEmpty
    private String name;

    private String division;
}
