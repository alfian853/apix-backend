package com.future.apix.command.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;

@Data
@Builder
public class QueryExecutorRequest {
    private String id;
    private HashMap<String,Object> query;

    public QueryExecutorRequest(String id, HashMap<String, Object> query) {
        this.id = id;
        this.query = query;
    }
}
