package com.future.apix.entity;

import lombok.Data;
import org.springframework.http.HttpMethod;

import java.util.HashMap;

@Data
public class ApiSection{
//    HashMap<String, HashMap<HttpMethod, ApiMethodData> > paths = new HashMap<>();
    HashMap<String, HashMap<String, ApiMethodData> > paths = new HashMap<>();

    //simpan data type
}
