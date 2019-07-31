package com.future.apix.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.command.impl.Swagger2CodegenCommandImpl;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.ProjectOasSwagger2;
import com.future.apix.repository.ProjectRepository;
import com.future.apix.repository.OasSwagger2Repository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Swagger2CodegenCommandTest {

    private ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private Swagger2CodegenCommandImpl command = new Swagger2CodegenCommandImpl();

    @Mock
    ProjectRepository apiRepository;

    @Mock
    OasSwagger2Repository swagger2Repository;

    @Mock
    Swagger2ExportCommand swagger2ExportCommand;

    private ApiProject project;

    private String CODEGEN_JAR, CODEGEN_RESULT_DIR,OAS_DIR;

    private String OAS_FILE_NAME = "ProjectNameAndUuid";
    private String CODE_FILE_NAME = "ProjectNameAndUuid.zip";


    @Before
    public void init() throws URISyntaxException, IOException {

        this.CODEGEN_JAR = Paths.get(getClass().getClassLoader()
                .getResource("codegen/swagger-codegen-cli-2.4.4.jar").toURI()).toString();
        this.CODEGEN_RESULT_DIR =
                Paths.get(getClass().getClassLoader()
                        .getResource("codegen/generated-code").toURI()).toString()+"/";
        this.OAS_DIR = Paths.get(getClass().getClassLoader()
                .getResource("codegen/exported-oas").toURI()).toString()+"/";

        command.setCODEGEN_JAR(CODEGEN_JAR);
        command.setCODEGEN_RESULT_DIR(CODEGEN_RESULT_DIR);
        command.setOAS_DIR(OAS_DIR);
        command.setCODEGEN_URL("");

        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();

        project = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
        project.setId("123");
        when(apiRepository.findById(any())).thenReturn(Optional.of(project));
    }

    @Test
    public void codegenExistAndUptoDate() {
        Date date = new Date();
        project.setUpdatedAt(date);
        ProjectOasSwagger2 oasSwagger2 = new ProjectOasSwagger2();
        oasSwagger2.setOasFileName(OAS_FILE_NAME);
        oasSwagger2.setGeneratedCodesFileName(CODE_FILE_NAME);
        oasSwagger2.setOasFileProjectUpdateDate(date);
        oasSwagger2.setGeneratedCodesProjectUpdatedDate(date);

        when(swagger2Repository.findProjectOasSwagger2ByProjectId(anyString())).thenReturn(Optional.of(oasSwagger2));
        command.execute("123");
        verify(swagger2Repository, times(0)).save(any());
    }

    @Test
    public void codegenNotExistAndUptoDate() throws IOException {
        Date date = new Date();
        project.setUpdatedAt(date);
        ProjectOasSwagger2 oasSwagger2 = new ProjectOasSwagger2();
        oasSwagger2.setOasFileName(OAS_FILE_NAME);
        oasSwagger2.setOasFileProjectUpdateDate(date);
        oasSwagger2.setGeneratedCodesProjectUpdatedDate(new Date(date.getTime()-1));

        when(swagger2Repository.findProjectOasSwagger2ByProjectId(anyString())).thenReturn(Optional.of(oasSwagger2));
        Files.deleteIfExists(Paths.get(CODEGEN_RESULT_DIR+ CODE_FILE_NAME));
        command.execute("1234");
        verify(swagger2Repository, times(1)).save(any());
        Assert.assertTrue(Files.exists(Paths.get(CODEGEN_RESULT_DIR+ CODE_FILE_NAME)));
    }

    @Test
    public void codegenExistAndNotUptoDate() throws IOException {
        Date date = new Date();
        project.setUpdatedAt(date);
        ProjectOasSwagger2 oasSwagger2 = new ProjectOasSwagger2();
        oasSwagger2.setOasFileName(OAS_FILE_NAME);
        oasSwagger2.setOasFileProjectUpdateDate(date);
        oasSwagger2.setGeneratedCodesProjectUpdatedDate(new Date(date.getTime()-1));

        when(swagger2Repository.findProjectOasSwagger2ByProjectId(anyString())).thenReturn(Optional.of(oasSwagger2));
        Files.deleteIfExists(Paths.get(CODEGEN_RESULT_DIR+ CODE_FILE_NAME));
        command.execute("12345");
        verify(swagger2Repository, times(1)).save(any());
        Assert.assertTrue(Files.exists(Paths.get(CODEGEN_RESULT_DIR+ CODE_FILE_NAME)));
    }






}
