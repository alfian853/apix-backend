package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Parameter{
    String name,in,type,description,collectionFormat;
    boolean required;
    String pattern;
    String format;

    @Field("enum")
    @JsonProperty("enum")
    List<String> enums;

//    Items items;
    Schema items;
    Integer maximum, minimum;
}
