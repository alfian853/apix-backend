package com.future.apix.service;

import com.future.apix.response.RequestResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ApiDataService {

    RequestResponse importFromFile(MultipartFile file);
}
