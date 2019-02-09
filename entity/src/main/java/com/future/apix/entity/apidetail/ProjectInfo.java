package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Data
public class ProjectInfo {

    //for validation of edition
    @Field("_signature")
    @JsonProperty("_signature")
    String signature = UUID.randomUUID().toString();

    String description,
            version,
            title,
            termsOfService;

    Object contact;
    License license;

}

