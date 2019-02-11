package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Parameter{
    // In Swagger 2.0 called Parameter Object

    // Fixed Fields
    String name,in,type,description;
    boolean required;


    String pattern;
    String format, collectionFormat;

    @Field("default")
    @JsonProperty("default")
    Object defaults;

    @Field("enum")
    @JsonProperty("enum")
    List<Object> enums;

    Schema items;
    Integer maximum, minimum, maxLength, minLength, maxItems, minItems, multipleOf;
//    boolean exclusiveMaximum, exclusiveMinimum, uniqueItems;
}
