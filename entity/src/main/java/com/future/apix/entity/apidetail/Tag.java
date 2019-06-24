package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.future.apix.entity.Mappable;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tag implements Mappable {
    String name, description;
    Contact externalDocs;

    @Field("_signature")
    @JsonProperty("_signature")
    String signature;
}
