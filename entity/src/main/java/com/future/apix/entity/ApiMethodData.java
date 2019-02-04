package com.future.apix.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.future.apix.entity.apidetail.Parameter;
import com.future.apix.entity.apidetail.RequestBody;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMethodData {

    String summary,description,operationId;
    Boolean deprecated;

    List<String> consumes = Collections.singletonList(MediaType.APPLICATION_JSON_VALUE);
    List<String> produces = Collections.singletonList(MediaType.APPLICATION_JSON_VALUE);

    /**
     * query param dan body param dipisah tapi di OAS swagger digabung
     * di OAS param : [], disini param : {} agar lebih mudah diakses
    **/


    //HashMap<nama param, isiparam>
    HashMap<String, Parameter> queryParams = new HashMap<>();

    HashMap<String, Parameter> pathVariables = new HashMap<>();

    //requestHeader
    HashMap<String, Parameter> headers = new HashMap<>();
    //requestBody
    RequestBody body = new RequestBody();

    //responseBody sama seperti requestBody
    HashMap<HttpStatus, RequestBody> responses = new HashMap<>();
}
