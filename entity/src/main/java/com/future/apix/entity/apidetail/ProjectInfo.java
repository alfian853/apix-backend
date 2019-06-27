package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.future.apix.entity.Mappable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInfo implements Mappable {

    //for validation of edition
    @Field("_signature")
    @JsonProperty("_signature")
    String signature = UUID.randomUUID().toString();

    String description,
            version,
            title,
            termsOfService;

    Contact contact;
    Contact license;

}

