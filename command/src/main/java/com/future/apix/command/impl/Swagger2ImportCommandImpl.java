package com.future.apix.command.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.command.Swagger2ImportCommand;
import com.future.apix.entity.ApiProject;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.request.ProjectImportRequest;
import com.future.apix.util.converter.SwaggerToApixOasConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;

@Component
public class Swagger2ImportCommandImpl implements Swagger2ImportCommand {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private ObjectMapper oMapper;

    @Autowired
    private SwaggerToApixOasConverter converter;

    @Override
    public ApiProject execute(ProjectImportRequest request) {

        HashMap<String,Object> json = null;
        MultipartFile file = request.getFile();
        try {
            json = oMapper.readValue(file.getInputStream(), HashMap.class);
            ApiProject project = converter.convert(json);
            project.setProjectOwner(request.getTeam());
            project.getTeams().add(request.getTeam().getName());
            apiRepository.save(project);
            return project;

        } catch (IOException e) {
            e.printStackTrace();
            throw new InvalidRequestException("Failed to import data : "+e.getMessage());
        }

    }

}
