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
import com.future.apix.util.JsonUtil;
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
import java.util.Map;

@Component
public class Swagger2ExportCommandImpl implements Swagger2ExportCommand {

    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private OasSwagger2Repository swagger2Repository;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private JsonUtil jsonUtil;

    @Autowired
    private YAMLMapper yamlMapper;

    private Map<String,String> map$refTorRef = new HashMap<String, String>(){{
        this.put("$ref","ref");
    }};

    private Map<String,String> maprefTor$ref = new HashMap<String, String>(){{
        this.put("ref","$ref");
    }};

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

        DownloadResponse response = new DownloadResponse();
        System.out.println("FORMAT: " + request.getFormat().toString());

        try{
            boolean fileExist = false;
            boolean expired = false;
            String targetFilePath = swagger2.getOasFileName()
                    + "." + request.getFormat().toString().toLowerCase();
            //if not generated yet
            if(swagger2.getOasFileName() != null){
                File testFile = new File(EXPORT_DIR + targetFilePath);
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

                String newNonFormatFileName = project.getInfo().getTitle()+"_"
                        + project.getInfo().getVersion() +"_"+ projectId;

                targetFilePath = newNonFormatFileName + "." + request.getFormat().toString().toLowerCase();

                writeFile(newNonFormatFileName, request.getFormat(), swagger2.getOasSwagger2());
                swagger2.setProjectId(projectId);
                swagger2.setOasFileName(newNonFormatFileName);
                swagger2.setOasFileProjectUpdateDate(project.getUpdatedAt());

                //replace '$ref' with 'ref'
                jsonUtil.remappingKeys(oasHashMap, this.map$refTorRef);

                swagger2Repository.save(swagger2);
            }
            // if file is deleted, but the project still exist
            else if(!fileExist){
                System.out.println("FILE NOT EXISTS! jadi buat baru");
                Map<String, Object> swaggerOas = swagger2.getOasSwagger2();
                jsonUtil.remappingKeys(swagger2.getOasSwagger2(),maprefTor$ref);
                writeFile(swagger2.getOasFileName(), request.getFormat(), swaggerOas);
            }
            response.setFileUrl(EXPORT_URL + targetFilePath);
            response.setStatusToSuccess();
        }
        catch (Exception e){
            e.printStackTrace();
            throw new DefaultRuntimeException("internal server error!");
        }

        return response;
    }

    private void writeFile(String newFileName, FileFormat format, Map<String, Object> swagger)
        throws IOException {

        if (format.toString().equals("JSON")) {
            File file = new File(EXPORT_DIR + newFileName + ".json");
            System.out.println("WRITE FILE TO JSON");
            mapper.writerWithDefaultPrettyPrinter()
                .writeValue(file, swagger);
        }
        else if (format.toString().equals("YAML")) {
            File file = new File(EXPORT_DIR + newFileName + ".yaml");
            System.out.println("WRITE FILE TO YAML");
            yamlMapper.writerWithDefaultPrettyPrinter()
                .writeValue(file, swagger);
        }
    }

}
