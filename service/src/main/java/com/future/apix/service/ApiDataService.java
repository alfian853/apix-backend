package com.future.apix.service;

import com.future.apix.entity.ApiProject;
import com.future.apix.response.RequestResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ApiDataService {

    RequestResponse importFromFile(MultipartFile file);

    ApiProject findById(String id);

    // digunakan untuk mendapatkan semua project dari mongo (sementara saja)
    List<ApiProject> findAll();

    // digunakan untuk mendapatkan field tertentu yang diletakkan pada front page
    List<ApiProject> findAllProjects();

    // delete by id (sementara)
    RequestResponse deleteById(String id);
}
