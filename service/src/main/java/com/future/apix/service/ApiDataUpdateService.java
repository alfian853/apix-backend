package com.future.apix.service;

import com.future.apix.response.ProjectUpdateResponse;

import java.util.HashMap;

public interface ApiDataUpdateService {
    //@query sudah ada id
    ProjectUpdateResponse doQuery(String id, HashMap<String, Object> query);
}
