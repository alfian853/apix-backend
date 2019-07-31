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

    // delete by id (sementara)
    RequestResponse deleteById(String id);

    // create new Project
    ProjectCreateResponse createProject(ProjectCreateRequest request);

    PagedResponse<ProjectDto> getByQuery(ProjectAdvancedQuery query);

    PagedResponse<ProjectDto> getByTeamAndQuery(ProjectAdvancedQuery query, String team);
}
