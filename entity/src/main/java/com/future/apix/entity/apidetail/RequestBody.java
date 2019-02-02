package com.future.apix.entity.apidetail;

import org.springframework.http.MediaType;

import java.io.Serializable;

public class RequestBody implements Serializable {

    MediaType mediaType;

    String description;

    BodyData data;


}
