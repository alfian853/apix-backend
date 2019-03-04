package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiMethodData;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.ApiSection;
import com.future.apix.entity.User;
import com.future.apix.entity.apidetail.*;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.repository.UserRepository;
import com.future.apix.request.ProjectCreateRequest;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.ApiDataService;
import com.future.apix.util.validator.BodyValidator;
import com.future.apix.util.validator.SchemaValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class ApiDataServiceImpl implements ApiDataService {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ObjectMapper oMapper;

    @Override
    public ApiProject findById(String id) {
        return apiRepository.findById(id).orElseThrow(DataNotFoundException::new);
    }

    @Override
    public List<ApiProject> findAll() {
        return apiRepository.findAll();
    }

    @Override
    public List<ApiProject> findAllProjects() {
        return apiRepository.findAllProjects();
    }

    @Override
    public RequestResponse deleteById(String id){
        ApiProject project = apiRepository.findById(id).orElseThrow(DataNotFoundException::new);
        apiRepository.deleteById(id);
        return RequestResponse.success("Project has been deleted!");
    }

    @Override
    public List<ApiProject> findByUser(String username) {
//        User user = userRepository.findByUsername(username);
//        return apiRepository.findByUsersIn(user);
        return apiRepository.findByUsersIn(username);
    }

    @Override
    public ProjectCreateResponse createProject(ProjectCreateRequest request) {
        ApiProject project = new ApiProject();
        project.setBasePath(request.getBasePath());
        project.setHost(request.getHost());
        project.setInfo(oMapper.convertValue(request.getInfo(), ProjectInfo.class));
        apiRepository.save(project);

        ProjectCreateResponse response = new ProjectCreateResponse();
        response.setStatusToSuccess();
        response.setMessage("Project has been created!");
        response.setApiProject(project);
        return response;
    }


}
