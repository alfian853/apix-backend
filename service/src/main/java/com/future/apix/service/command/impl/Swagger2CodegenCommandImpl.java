package com.future.apix.service.command.impl;

import com.future.apix.entity.ApiProject;
import com.future.apix.entity.ProjectOasSwagger2;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.repository.OasSwagger2Repository;
import com.future.apix.response.DownloadResponse;
import com.future.apix.service.CommandExecutorService;
import com.future.apix.service.command.Swagger2CodegenCommand;
import com.future.apix.service.command.Swagger2ExportCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.util.Arrays;

@Component
public class Swagger2CodegenCommandImpl implements Swagger2CodegenCommand {


    @Autowired
    ApiRepository apiRepository;

    @Autowired
    OasSwagger2Repository swagger2Repository;

    @Autowired
    CommandExecutorService executorService;

    private String CODEGEN_URL,CODEGEN_DIR,CODEGEN_JAR,OAS_DIR;

    public Swagger2CodegenCommandImpl(Environment e) {
        this.CODEGEN_URL = e.getProperty("apix.codegen.relative_url");
        this.CODEGEN_DIR = e.getProperty("apix.codegen.directory");
        this.CODEGEN_JAR = e.getProperty("apix.codegen.swagger_cli_jar");
        this.OAS_DIR = e.getProperty("apix.export_oas.directory");
    }

    @Override
    public synchronized DownloadResponse executeCommand(String projectId) {

        ApiProject project = apiRepository.findById(projectId).orElseThrow(DataNotFoundException::new);

        //make sure the oas file is the latest version
        executorService.execute(Swagger2ExportCommand.class,projectId);

        ProjectOasSwagger2 swagger2 = swagger2Repository.findProjectOasSwagger2ByProjectId(projectId).get();

        DownloadResponse response = new DownloadResponse();

        boolean notExistOrExpired = swagger2.getGeneratedCodesFileName() == null ||
                !swagger2.getGeneratedCodesProjectUpdatedDate().equals(
                        project.getUpdatedAt()
                );

        if(notExistOrExpired){
            String baseName = swagger2.getOasFileName().substring(
                    0, swagger2.getOasFileName().length()-5
            );
            File resultDir = new File(
                    CODEGEN_DIR + baseName
            );


            try {
                if(swagger2.getGeneratedCodesFileName() != null){
//                    new ProcessBuilder(
//                            "rm",CODEGEN_DIR + swagger2.getGeneratedCodesFileName()
//                    ).start().waitFor();
                    FileSystemUtils.deleteRecursively(new File(CODEGEN_DIR + swagger2.getGeneratedCodesFileName()));
                }

                //hapus temp folder untuk codegen jika ada
                if(resultDir.exists()){
                    FileSystemUtils.deleteRecursively(new File(resultDir.getPath()));
//                    new ProcessBuilder(
//                            "rm",resultDir.getPath()
//                    ).start().waitFor();
                }
                resultDir.mkdir();

                ProcessBuilder pb = new ProcessBuilder(
                        "java",
                        "-jar",CODEGEN_JAR,"generate",
                        "-i",OAS_DIR + swagger2.getOasFileName(),
                        "-l","spring","-o",CODEGEN_DIR + baseName
                );
                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                pb.start().waitFor();


//                pb = new ProcessBuilder("cd",CODEGEN_DIR).d;
//                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//                pb.redirectError(ProcessBuilder.Redirect.INHERIT);
//                pb.start().waitFor();
//                pb.command("zip","-r"+baseName+"zip",baseName).start();
                pb = new ProcessBuilder(
                        "zip","-r",baseName+".zip",
                        baseName
                );
                pb.directory(new File(CODEGEN_DIR));
                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                pb.start().waitFor();


//                pb = new ProcessBuilder(
////                        "rm","-rf",CODEGEN_DIR+baseName
////                );
                FileSystemUtils.deleteRecursively(new File(CODEGEN_DIR+baseName));
//                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
//                pb.redirectError(ProcessBuilder.Redirect.INHERIT);
//                pb.start().waitFor();

                swagger2.setGeneratedCodesFileName(baseName+".zip");
                swagger2.setGeneratedCodesProjectUpdatedDate(project.getUpdatedAt());
                swagger2Repository.save(swagger2);

                response.setFileUrl(CODEGEN_URL+swagger2.getGeneratedCodesFileName());
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Internal Server Error! : "+e.getMessage());
            }
        }
        else{
            response.setFileUrl(CODEGEN_URL+swagger2.getGeneratedCodesFileName());
        }

        response.setStatusToSuccess();
        return response;
    }
}
