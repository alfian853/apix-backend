package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Parameter{
    String name,in,type,description;
    boolean required;
    String pattern;
    String format;
    @JsonProperty("enum")
    List<String> enums;
}
