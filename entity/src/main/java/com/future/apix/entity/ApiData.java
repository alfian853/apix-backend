package com.future.apix.entity;

import com.future.apix.entity.apidetail.RequestBody;
import com.future.apix.entity.apidetail.ResponseBody;
import lombok.Data;
import org.springframework.http.HttpMethod;

import java.io.Serializable;

@Data
public class ApiData implements Serializable {

    String name,
            description;

    HttpMethod method;

    RequestBody request;
    ResponseBody responses;
}
