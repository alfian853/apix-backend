package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.future.apix.entity.Mappable;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class Github implements Mappable {
    String owner, repo, branch;
    String path;

    @Field("_signature")
    @JsonProperty("_signature")
    String signature;
}
