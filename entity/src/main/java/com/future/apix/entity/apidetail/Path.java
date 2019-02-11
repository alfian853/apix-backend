package com.future.apix.entity.apidetail;

import com.future.apix.entity.ApiMethodData;
import lombok.Data;

import java.util.HashMap;

@Data
public class Path {
    HashMap<String, ApiMethodData> methods;
    String description="";
    HashMap<String, Schema> pathVariables;
}
