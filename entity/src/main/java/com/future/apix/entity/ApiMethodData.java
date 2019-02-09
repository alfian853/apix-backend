package com.future.apix.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.future.apix.entity.apidetail.Parameter;
import com.future.apix.entity.apidetail.RequestBody;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMethodData {

    String summary,description,operationId;
    Boolean deprecated;

    @Field("_signature")
    @JsonProperty("_signature")
    String signature;

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
    @Valid
    RequestBody body;

    //responseBody sama seperti requestBody
    HashMap<String, RequestBody> responses = new HashMap<>();
}
