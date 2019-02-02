package com.future.apix.entity;

import com.future.apix.entity.apidetail.RequestBody;
import com.future.apix.entity.apidetail.ResponseBody;
import lombok.Data;
import org.springframework.http.HttpMethod;

import java.io.Serializable;
import java.util.HashMap;

@Data
public class ApiData implements Serializable {

    String name,
            description;

    HttpMethod method;

    HashMap<String, String> queryParams;
    RequestBody request;
    ResponseBody responses;
}
