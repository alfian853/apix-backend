package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.HashMap;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestBody {

    String name,in,description,type;
    boolean required;

    //ga perlu?
    HashMap<String,String> headers;

    Schema schema;

}
