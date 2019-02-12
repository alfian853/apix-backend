package com.future.apix.entity.apidetail;

import com.future.apix.entity.ApiMethodData;
import lombok.Data;
import lombok.Getter;

import java.util.HashMap;

@Data
public class Path {
    HashMap<String, ApiMethodData> methods = new HashMap<>();
    String description="";

    HashMap<String, Schema> pathVariables;

    public HashMap<String, Schema> getPathVariables(){
        return (pathVariables == null)?new HashMap<>():this.pathVariables;
    }
}
