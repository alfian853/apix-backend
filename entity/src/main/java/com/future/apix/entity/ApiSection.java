package com.future.apix.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.future.apix.entity.apidetail.Path;
import com.future.apix.entity.apidetail.Tag;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;
import java.util.UUID;

@Data
public class ApiSection{
    HashMap<String, Path> paths = new HashMap<>();

    @Field("_signature")
    @JsonProperty("_signature")
    String signature = UUID.randomUUID().toString();

    // simpan sesuai dengan Tags di swagger OAS
    Tag info;
}
