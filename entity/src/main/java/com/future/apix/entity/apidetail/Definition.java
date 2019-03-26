package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Data
public class Definition {
    @Field("_signature")
    @JsonProperty("_signature")
    String signature = UUID.randomUUID().toString();
    String name;
    String description;
    Schema schema;
}
