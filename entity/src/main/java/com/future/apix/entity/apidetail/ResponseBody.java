package com.future.apix.entity.apidetail;

import org.springframework.http.HttpStatus;

import java.io.Serializable;
import java.util.HashMap;

public class ResponseBody implements Serializable {

    HashMap<HttpStatus, RequestBody> responses;

}
