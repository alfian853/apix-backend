package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.HashMap;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestBody {

    String name,in = "",description,type;
    boolean required;

    // Isi dari Header Object hampir sama dengan Parameter Object

    //HashMap<nama param, isiparam>
    HashMap<String, Schema> queryParams;

    //requestHeader
    HashMap<String, Schema> headers;

    Schema schema;

    public HashMap<String, Schema> getQueryParams(){
        return (this.queryParams == null)?this.queryParams = new HashMap<>() : this.queryParams;
    }

    public HashMap<String, Schema> getHeaders(){
        return (this.headers == null)?this.headers = new HashMap<>() : this.headers;
    }

    public Schema getSchema(){
        return (this.schema == null)?this.schema = new Schema() : this.schema;
    }
}
