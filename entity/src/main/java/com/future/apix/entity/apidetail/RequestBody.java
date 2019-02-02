package com.future.apix.entity.apidetail;

import org.springframework.http.MediaType;
import java.io.Serializable;
import java.util.HashMap;

public class RequestBody implements Serializable {

    MediaType mediaType;

    String description;

    HashMap<String,String> header;

    BodyData data;

}
