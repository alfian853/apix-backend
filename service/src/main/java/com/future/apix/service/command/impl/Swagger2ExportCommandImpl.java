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
import com.future.apix.util.QueueCommand;
import com.future.apix.util.converter.ApiProjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
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

    private String EXPORT_URL;
    private String EXPORT_DIR;

    private static HashMap<String, QueueCommand<DownloadResponse>> pools = new HashMap<>();

    @Autowired
    public Swagger2ExportCommandImpl(Environment env) {
        this.EXPORT_URL = env.getProperty("apix.export_oas.relative_url");
        this.EXPORT_DIR = env.getProperty("apix.export_oas.directory");
    }

    @Override
    public DownloadResponse executeCommand(String projectId) {
        if(!pools.containsKey(projectId)){
            pools.put(
                    projectId,
                    new QueueCommand<DownloadResponse>() {
                        @Override
                        synchronized public DownloadResponse execute() {
                            return generateOasFile(projectId);
                        }
                    }
            );
        }
        return pools.get(projectId).execute();
    }

    private DownloadResponse generateOasFile(String projectId){
        ApiProject project = apiRepository.findById(projectId).orElseThrow(DataNotFoundException::new);

        ProjectOasSwagger2 swagger2 = swagger2Repository.findProjectOasSwagger2ByProjectId(projectId)
                .orElse(new ProjectOasSwagger2());


        String newFileName = project.getInfo().getTitle()+"_"
                + project.getInfo().getVersion() +"_"+ projectId +".json";

        DownloadResponse response = new DownloadResponse();


        try{
            if(swagger2.getOasSwagger2() == null){
                LinkedHashMap<String, Object> oasHashMap = converter.convertToOasSwagger2(project);
                swagger2.setOasSwagger2(oasHashMap);
            }

            boolean notExistOrExpired = false;

            //if not exist
            if(swagger2.getOasFileName() == null){
                notExistOrExpired = true;
            }
            else{
                File testFile = new File(EXPORT_DIR + swagger2.getOasFileName());
                //if not the latest version
                if(! swagger2.getOasFileProjectUpdateDate().equals(project.getUpdatedAt())){
                    Files.deleteIfExists(testFile.toPath());
                    notExistOrExpired = true;
                }
                else if(!testFile.exists()){
                    notExistOrExpired = true;
                }
            }

            if(notExistOrExpired){
                mapper.writerWithDefaultPrettyPrinter().writeValue(
                        new File(EXPORT_DIR + newFileName), swagger2.getOasSwagger2()
                );
                swagger2.setProjectId(projectId);
                swagger2.setOasFileName(newFileName);
                swagger2.setOasFileProjectUpdateDate(project.getUpdatedAt());

                swagger2Repository.save(swagger2);
            }
            else{
                response.fileUrl(EXPORT_URL + swagger2.getOasFileName());
                response.setStatusToSuccess();
            }
        }
        catch (Exception e){
            e.printStackTrace();
            throw new DefaultRuntimeException("internal server error!");
        }
        response.fileUrl(EXPORT_URL + swagger2.getOasFileName());
        response.setStatusToSuccess();

        return response;
    }

}
