package com.future.apix.service;

import com.future.apix.entity.ApiProject;
import com.future.apix.repository.request.AdvancedQuery;
import com.future.apix.request.ProjectCreateRequest;
import com.future.apix.response.ProjectDto;
import com.future.apix.response.PagedResponse;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.RequestResponse;

public interface  ApiDataService {

    ApiProject findById(String id);

    // delete by id (sementara)
    RequestResponse deleteById(String id);

    // create new Project
    ProjectCreateResponse createProject(ProjectCreateRequest request);

    PagedResponse<ProjectDto> getByQuery(AdvancedQuery query);

    PagedResponse<ProjectDto> getByTeamAndQuery(AdvancedQuery query, String team);
}
