package com.future.apix.entity;

import com.future.apix.entity.apidetail.Parameter;
import com.future.apix.entity.apidetail.RequestBody;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Data
public class ApiData {

    String summary;
    String description;

    List<String> consumes = Collections.singletonList(MediaType.APPLICATION_JSON_VALUE);
    List<String> produces = Collections.singletonList(MediaType.APPLICATION_JSON_VALUE);

    /**
     * query param dan body param dipisah tapi di OAS swagger digabung
     * di OAS param : [], disini param : {} agar lebih mudah diakses
    **/
    //HashMap<nama param, isiparam>
    HashMap<String, Parameter> queryParams = new HashMap<>();

    //requestBody
    RequestBody body = new RequestBody();

    //responseBody sama seperti requestBody
    HashMap<HttpStatus, RequestBody> responses = new HashMap<>();
}
