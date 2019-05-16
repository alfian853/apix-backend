package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.future.apix.entity.Mappable;
import com.future.apix.entity.apidetail.Path;
import com.future.apix.entity.apidetail.Tag;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;

@Data
public class ApiSection implements Mappable {
    HashMap<String, Path> paths = new HashMap<>();

    @Field("_signature")
    @JsonProperty("_signature")
    String signature;
    Tag info;
}
