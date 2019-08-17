package com.future.apix.command.impl;

import com.future.apix.command.Swagger2CodegenCommand;
import com.future.apix.command.Swagger2ExportCommand;
import com.future.apix.command.model.ExportRequest;
import com.future.apix.enumerate.FileFormat;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.ProjectOasSwagger2;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.repository.OasSwagger2Repository;
import com.future.apix.repository.ProjectRepository;
import com.future.apix.response.DownloadResponse;
import com.future.apix.util.QueueCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class Swagger2CodegenCommandImpl implements Swagger2CodegenCommand {


    @Autowired
    ProjectRepository apiRepository;

    @Autowired
    OasSwagger2Repository swagger2Repository;

    @Autowired
    Swagger2ExportCommand swagger2ExportCommand;

    private static HashMap<String, QueueCommand<DownloadResponse, String>> pools = new HashMap<>();

    public void setCODEGEN_URL(String CODEGEN_URL) {
        this.CODEGEN_URL = CODEGEN_URL;
    }

    public void setCODEGEN_RESULT_DIR(String CODEGEN_RESULT_DIR) {
        this.CODEGEN_RESULT_DIR = CODEGEN_RESULT_DIR;
    }

    public void setCODEGEN_JAR(String CODEGEN_JAR) {
        this.CODEGEN_JAR = CODEGEN_JAR;
    }

    public void setOAS_DIR(String OAS_DIR) {
        this.OAS_DIR = OAS_DIR;
    }

    @Value("apix.codegen.relative_url")
    private String CODEGEN_URL;

    @Value("apix.codegen.directory")
    private String CODEGEN_RESULT_DIR;

    @Value("apix.codegen.swagger_cli_jar")
    private String CODEGEN_JAR;

    @Value("apix.export_oas.directory")
    private String OAS_DIR;

    private List<File> fileList = new ArrayList<>();

    @Override
    public DownloadResponse execute(String projectId) {
        if(!pools.containsKey(projectId)){
            pools.put(
                projectId,
                new QueueCommand<DownloadResponse, String>() {
                    @Override
                    synchronized public DownloadResponse execute(String id) {
                        return generateSourceCode(id);
                    }
                }
            );
        }
        return pools.get(projectId).execute(projectId);
    }

    private DownloadResponse generateSourceCode(String projectId){
        ApiProject project = apiRepository.findById(projectId).orElseThrow(DataNotFoundException::new);

        //make sure the oas file is the latest version
        swagger2ExportCommand.execute(new ExportRequest(projectId, FileFormat.JSON));

        ProjectOasSwagger2 swagger2 = swagger2Repository.findProjectOasSwagger2ByProjectId(projectId).get();

        DownloadResponse response = new DownloadResponse();

        boolean notExistOrExpired = swagger2.getGeneratedCodesFileName() == null ||
                swagger2.getGeneratedCodesProjectUpdatedDate().before(
                        project.getUpdatedAt()
                );

        File checkDirectory = new File(CODEGEN_RESULT_DIR);
        if (!checkDirectory.exists()) checkDirectory.mkdir();

        if(notExistOrExpired){
            String baseName = swagger2.getOasFileName();
            File resultDir = new File(
                    CODEGEN_RESULT_DIR + baseName
            );

            try {
                if(swagger2.getGeneratedCodesFileName() != null){
                    FileSystemUtils.deleteRecursively(new File(CODEGEN_RESULT_DIR + swagger2.getGeneratedCodesFileName()));
                }

                //hapus temp folder untuk codegen jika ada
                if(resultDir.exists()){
                    FileSystemUtils.deleteRecursively(new File(resultDir.getPath()));
                }
                resultDir.mkdir();
                ProcessBuilder pb = new ProcessBuilder(
                        "java",
                        "-jar",CODEGEN_JAR,"generate",
                        "-i",OAS_DIR + swagger2.getOasFileName() + ".json" ,
                        "-l","spring","-o", CODEGEN_RESULT_DIR + baseName
                );
                pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
                pb.redirectError(ProcessBuilder.Redirect.INHERIT);
                pb.start().waitFor();

                File directoryToZip = new File(CODEGEN_RESULT_DIR + baseName);
                getAllFiles(directoryToZip, fileList);
                writeZipFile(directoryToZip, fileList);

                FileSystemUtils.deleteRecursively(new File(CODEGEN_RESULT_DIR +baseName));

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

    public static void getAllFiles(File dir, List<File> fileList1) {
        File[] files = dir.listFiles();
        for (File file : files) {
            fileList1.add(file);
            if(file.isDirectory()){
                getAllFiles(file, fileList1);
            }
        }
    }

    public static void writeZipFile(File directoryToZip, List<File> fileList1){
        try {
            FileOutputStream fos = new FileOutputStream(directoryToZip.getCanonicalPath() + ".zip");
            ZipOutputStream zos = new ZipOutputStream(fos);
            for (File file : fileList1) {
                if(!file.isDirectory()) {
                    addToZip(directoryToZip, file, zos);
                }
            }
            zos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1);
        ZipEntry zipEntry = new ZipEntry(zipFilePath);
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }
        zos.closeEntry();
        fis.close();
    }

}
