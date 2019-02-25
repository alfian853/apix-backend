package com.future.apix.service.command.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.response.ExportResponse;
import com.future.apix.service.command.Swagger2ExportCommand;
import com.future.apix.util.converter.ApiProjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

@Component
public class Swagger2ExportCommandImpl implements Swagger2ExportCommand {

    @Autowired
    ApiRepository apiRepository;

    @Autowired
    private ObjectMapper mapper;

    private ApiProjectConverter converter = ApiProjectConverter.getInstance();

    final String TARGET_DIRECTORY = "web/src/main/resources/exported-oas/";

    private String EXPORT_URL;

    @Autowired
    public Swagger2ExportCommandImpl(Environment env) {
        this.EXPORT_URL = env.getProperty("apix.export_relative_url");
    }


    @Override
    public ExportResponse executeCommand(String projectId) {

        ApiProject project = apiRepository.findById(projectId).orElseThrow(DataNotFoundException::new);

        String fileName = project.getInfo().getTitle()+"_"+project.getInfo().getVersion()+".json";

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(
                    new File(TARGET_DIRECTORY + fileName),
                    converter.convertToOasSwagger2(project)
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        ExportResponse response = new ExportResponse();
        response.fileUrl(EXPORT_URL+fileName);
        response.setStatusToSuccess();
        return response;
    }

}
