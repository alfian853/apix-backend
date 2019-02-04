package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Parameter{
    String name,in,type,description;
    boolean required;
    String pattern;
    NumberFormat format;
}
