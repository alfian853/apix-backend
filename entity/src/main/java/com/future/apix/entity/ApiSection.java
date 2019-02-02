package com.future.apix.entity;

import lombok.Data;
import org.springframework.http.HttpMethod;

import java.io.Serializable;
import java.util.HashMap;

@Data
public class ApiSection implements Serializable {

    String name;

    HashMap<String, HashMap<HttpMethod,ApiData> > paths;

}
