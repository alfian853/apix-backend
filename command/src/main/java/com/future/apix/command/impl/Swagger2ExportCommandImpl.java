package com.future.apix.command.impl;

import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.future.apix.command.Swagger2ExportCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.command.model.ExportRequest;
import com.future.apix.command.model.enumerate.FileFormat;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.ProjectOasSwagger2;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.DefaultRuntimeException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.repository.OasSwagger2Repository;
import com.future.apix.response.DownloadResponse;
import com.future.apix.util.QueueCommand;
import com.future.apix.util.converter.ApiProjectConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;

@Component
public class Swagger2ExportCommandImpl implements Swagger2ExportCommand {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private OasSwagger2Repository swagger2Repository;

    @Autowired
    private ObjectMapper mapper;

    private YAMLMapper yamlMapper = new YAMLMapper();

    public void setObjectMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Autowired
    private ApiProjectConverter converter;

    private String EXPORT_URL;
    private String EXPORT_DIR;

    private static HashMap<String, QueueCommand<DownloadResponse, ExportRequest>> pools = new HashMap<>();

    public Swagger2ExportCommandImpl() {
    }

    public void setEXPORT_URL(String EXPORT_URL) {
        this.EXPORT_URL = EXPORT_URL;
    }

    public void setEXPORT_DIR(String EXPORT_DIR) {
        this.EXPORT_DIR = EXPORT_DIR;
    }

    @Autowired
    public Swagger2ExportCommandImpl(Environment env) {
        this.EXPORT_URL = env.getProperty("apix.export_oas.relative_url");
        this.EXPORT_DIR = env.getProperty("apix.export_oas.directory");
    }

    @Override
    public DownloadResponse execute(ExportRequest exportRequest) {
        String projectId = exportRequest.getProjectId();
        if(!pools.containsKey(projectId)){
            pools.put(
                    projectId,
                    new QueueCommand<DownloadResponse, ExportRequest>() {
                        @Override
                        synchronized public DownloadResponse execute(ExportRequest request) {
                            return generateOasFile(request);
                        }
                    }
            );
        }
        return pools.get(projectId).execute(exportRequest);
    }

    private DownloadResponse generateOasFile(ExportRequest request){
        String projectId = request.getProjectId();
        ApiProject project = apiRepository.findById(projectId).orElseThrow(DataNotFoundException::new);

        ProjectOasSwagger2 swagger2 = swagger2Repository.findProjectOasSwagger2ByProjectId(projectId)
                .orElse(new ProjectOasSwagger2());

        String newNonFormatFileName = project.getInfo().getTitle()+"_"
                + project.getInfo().getVersion() +"_"+ projectId;
        String newFileNameWithFormat = newNonFormatFileName + "." + request.getFormat().toString().toLowerCase();

        DownloadResponse response = new DownloadResponse();
        System.out.println("FORMAT: " + request.getFormat().toString());

        try{
            boolean fileExist = false;
            boolean expired = false;
            String oasFileName = newNonFormatFileName + "." + request.getFormat().toString().toLowerCase();

            //if not generated yet
            if(swagger2.getOasFileName() != null){
                File testFile = new File(EXPORT_DIR + swagger2.getOasFileName());
                //if not the latest version
                if(swagger2.getOasFileProjectUpdateDate().before(project.getUpdatedAt())){
                    Files.deleteIfExists(testFile.toPath());
                    expired = true;
                } // but the file is deleted

                if(testFile.exists()){
                    fileExist = true;
                }
            }

            // not the latest update and file is available
            if( expired || swagger2.getOasSwagger2() == null ){
                System.out.println("NOT THE LATEST UPDATE!");
                LinkedHashMap<String, Object> oasHashMap = converter.convertToOasSwagger2(project);
                swagger2.setOasSwagger2(oasHashMap);

                writeFile(newFileNameWithFormat, request.getFormat(), swagger2.getOasSwagger2());

                swagger2.setProjectId(projectId);
                swagger2.setOasFileName(newNonFormatFileName);
                swagger2.setOasFileProjectUpdateDate(project.getUpdatedAt());

                swagger2Repository.save(swagger2);
            }
            // if file is deleted, but the project still exist
            else if(!fileExist){
                System.out.println("FILE NOT EXISTS! jadi buat baru");
                writeFile(newFileNameWithFormat, request.getFormat(), swagger2.getOasSwagger2());

                swagger2.setOasFileName(newNonFormatFileName);
            }
            else{
                System.out.println("FILE EXIST! sudah ada di " + EXPORT_DIR + oasFileName);
                response.setFileUrl(EXPORT_DIR + oasFileName);
                response.setStatusToSuccess();
                response.setMessage("File export already exists!");
                return response;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            throw new DefaultRuntimeException("internal server error!");
        }
        response.fileUrl(EXPORT_URL + swagger2.getOasFileName() + "." + request.getFormat().toString().toLowerCase());
        response.setStatusToSuccess();
        response.setMessage("File has been exported!");

        return response;
    }

    private void writeFile(String newFileNameWithFormat, FileFormat format, HashMap<String, Object> swagger)
        throws IOException {

        File file = new File(EXPORT_DIR + newFileNameWithFormat);
        if (format.toString().equals("JSON")) {
            System.out.println("WRITE FILE TO JSON");
            mapper.writerWithDefaultPrettyPrinter()
                .writeValue(file, swagger);
        }
        else if (format.toString().equals("YAML")) {
            System.out.println("WRITE FILE TO YAML");
            yamlMapper.writerWithDefaultPrettyPrinter()
                .writeValue(file, swagger);
        }
//        return file;
    }

}
