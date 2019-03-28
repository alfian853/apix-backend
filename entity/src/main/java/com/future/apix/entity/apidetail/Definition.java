package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Definition {
    @Field("_signature")
    @JsonProperty("_signature")
    String signature = UUID.randomUUID().toString();
    String name;
    String description;
    Schema schema;
}
