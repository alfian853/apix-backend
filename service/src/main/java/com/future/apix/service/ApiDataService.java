package com.future.apix.service;

import com.future.apix.response.RequestResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;

public interface ApiDataService {

    RequestResponse importFromFile(MultipartFile file);

    //object query must contain id
    RequestResponse doQuery(HashMap<String, Object> query);

}
