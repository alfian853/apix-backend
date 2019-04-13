package com.future.apix.service.command.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.ProjectOasSwagger2;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.DefaultRuntimeException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.repository.OasSwagger2Repository;
import com.future.apix.response.DownloadResponse;
import com.future.apix.service.command.Swagger2ExportCommand;
import com.future.apix.util.converter.ApiProjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.LinkedHashMap;

@Component
public class Swagger2ExportCommandImpl implements Swagger2ExportCommand {

    @Autowired
    ApiRepository apiRepository;

    @Autowired
    OasSwagger2Repository swagger2Repository;

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
    public DownloadResponse executeCommand(String projectId) {

        ApiProject project = apiRepository.findById(projectId).orElseThrow(DataNotFoundException::new);

        ProjectOasSwagger2 swagger2 = swagger2Repository.findProjectOasSwagger2ByProjectId(projectId)
                .orElse(new ProjectOasSwagger2());

        boolean notExistOrExpired = swagger2.getOasSwagger2() == null ||
                ! swagger2.getUpdatedAt().equals(project.getUpdatedAt());
        DownloadResponse response = new DownloadResponse();

        String fileName = project.getInfo().getTitle()+"_"+project.getInfo().getVersion()+".json";
        File file = new File(TARGET_DIRECTORY + fileName);

        try{
            if(notExistOrExpired){
                LinkedHashMap<String, Object> oasHashMap = converter.convertToOasSwagger2(project);
                mapper.writerWithDefaultPrettyPrinter().writeValue(
                        file,
                        oasHashMap
                );
                swagger2.setProjectId(projectId);
                swagger2.setOasSwagger2(oasHashMap);
                swagger2.setUpdatedAt(project.getUpdatedAt());
                swagger2Repository.save(swagger2);
                response.fileUrl(EXPORT_URL+fileName);
                response.setStatusToSuccess();
            }
            else{
                if(file.exists()){
                    response.fileUrl(EXPORT_URL+fileName);
                    response.setStatusToSuccess();
                }
                else{
                    mapper.writerWithDefaultPrettyPrinter().writeValue(
                            file,swagger2.getOasSwagger2()
                    );
                    response.fileUrl(EXPORT_URL+fileName);
                    response.setStatusToSuccess();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            throw new DefaultRuntimeException("internal server error!");
        }

        return response;
    }

}
