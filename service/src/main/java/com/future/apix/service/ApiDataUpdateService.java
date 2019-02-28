package com.future.apix.service;

import com.future.apix.response.RequestResponse;

import java.util.HashMap;

public interface ApiDataUpdateService {
    //@query sudah ada id
    RequestResponse doQuery(String id, HashMap<String, Object> query);
}
