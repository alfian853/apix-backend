package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.apidetail.Github;
import com.future.apix.entity.apidetail.ProjectInfo;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.repository.UserRepository;
import com.future.apix.request.ProjectCreateRequest;
import com.future.apix.response.ProjectCreateResponse;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.ApiDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Service
public class ApiDataServiceImpl implements ApiDataService {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ObjectMapper oMapper;

    @Override
    public ApiProject findById(String id) {
        return apiRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Project does not exists!"));
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
        ApiProject project = apiRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Project does not exists!"));
        apiRepository.deleteById(id);
        return RequestResponse.success("Project has been deleted!");
    }

    @Override
    public List<ApiProject> findByUser(String teamName) {
//        User user = userRepository.findByUsername(username);
//        return apiRepository.findByUsersIn(user);
        return apiRepository.findByTeamsIn(teamName);
    }

    @Override
    public ProjectCreateResponse createProject(ProjectCreateRequest request) {
        ApiProject project = new ApiProject();
        project.setBasePath(request.getBasePath());
        project.setHost(request.getHost());
        project.setInfo(oMapper.convertValue(request.getInfo(), ProjectInfo.class));
        project.setGithubProject(new Github());
        project.getInfo().setSignature(UUID.randomUUID().toString());
        project.getGithubProject().setSignature(UUID.randomUUID().toString());
        project.setSignature(UUID.randomUUID().toString());
        apiRepository.save(project);

        ProjectCreateResponse response = new ProjectCreateResponse();
        response.setStatusToSuccess();
        response.setMessage("Project has been created!");
        response.setApiProject(project);
        return response;
    }

//    private HashMap<String,Object> toStrObjMap(Object object){
//        return (HashMap<String,Object>) object;
//    }


}
