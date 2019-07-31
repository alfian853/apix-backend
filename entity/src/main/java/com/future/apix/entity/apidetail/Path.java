package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.future.apix.entity.Mappable;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;
import java.util.UUID;

@Data
public class Path implements Mappable {
    HashMap<String, ApiMethodData> methods = new HashMap<>();
    String description="";

    HashMap<String, Schema> pathVariables;

    @Field("_signature")
    @JsonProperty("_signature")
    String signature = UUID.randomUUID().toString();

    public HashMap<String, Schema> getPathVariables(){
        return (pathVariables == null)?pathVariables = new HashMap<>():this.pathVariables;
    }
}
