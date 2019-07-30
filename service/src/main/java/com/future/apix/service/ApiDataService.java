package com.future.apix.service;

import com.future.apix.entity.ApiProject;
import com.future.apix.repository.request.ProjectAdvancedQuery;
import com.future.apix.request.ProjectCreateRequest;
import com.future.apix.response.ProjectDto;
import com.future.apix.response.PagedResponse;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.RequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface  ApiDataService {

    ApiProject findById(String id);
    Page<ApiProject> findAll(Pageable pageable);

    Page<ApiProject> findSearch(String search, Pageable pageable);

    // digunakan untuk mendapatkan field tertentu yang diletakkan pada front page
    List<ApiProject> findAllProjects();


    // delete by id (sementara)
    RequestResponse deleteById(String id);

    List<ApiProject> findByUser(String username);

    // create new Project
    ProjectCreateResponse createProject(ProjectCreateRequest request);

    PagedResponse<ProjectDto> getByQuery(ProjectAdvancedQuery query);
}
