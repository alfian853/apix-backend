package com.future.apix.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.command.model.ExportRequest;
import com.future.apix.command.model.enumerate.FileFormat;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.ProjectOasSwagger2;
import com.future.apix.repository.ApiRepository;
import com.future.apix.repository.OasSwagger2Repository;
import com.future.apix.response.DownloadResponse;
import com.future.apix.command.impl.Swagger2ExportCommandImpl;
import com.future.apix.util.ApixUtil;
import com.future.apix.util.converter.ApiProjectConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Swagger2ExportCommandTest {

    private ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private Swagger2ExportCommandImpl command = new Swagger2ExportCommandImpl();

    @Mock
    ApiRepository apiRepository;

    @Mock
    OasSwagger2Repository swagger2Repository;

    @Spy
    ApiProjectConverter apiProjectConverter;

    private HashSet<String> ignoredField = new HashSet<>(Arrays.asList("definitions","_signature","$ref","required","security","externalDocs"));;

    @Before
    public void init() throws URISyntaxException {
        command.setObjectMapper(mapper);
        command.setEXPORT_DIR(Paths.get(getClass().getClassLoader().getResource("").toURI()).toString()+"/");
        command.setEXPORT_URL("");
    }

    @Test
    public void oasFileExistButNotUpToDateTest() throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        HashMap<String, Object> temp = mapper.readValue(Files.readAllBytes(Paths.get(uri)), HashMap.class);
        ApiProject project = mapper.convertValue(temp, ApiProject.class);
        Optional<ApiProject> optionalApiProject = Optional.of(project);
        ProjectOasSwagger2 oasSwagger2 = new ProjectOasSwagger2();
        oasSwagger2.setOasFileName("Petstore API_1.0.0_123");

        oasSwagger2.setOasFileProjectUpdateDate(new Date(new Date().getTime()-1));
        project.setUpdatedAt(new Date());

        when(apiRepository.findById(any())).thenReturn(optionalApiProject);
        when(swagger2Repository.findProjectOasSwagger2ByProjectId(anyString())).thenReturn(Optional.of(oasSwagger2));

        DownloadResponse downloadResponse = command.execute(new ExportRequest("123", FileFormat.JSON));
        URI uriResult = getClass().getClassLoader().getResource(downloadResponse.getFileUrl()).toURI();
        URI uriExpected = getClass().getClassLoader().getResource("swagger-oas.json").toURI();

        verify(apiProjectConverter,times(1)).convertToOasSwagger2(any(ApiProject.class));

        HashMap<String, Object> mapResult = mapper.readValue(Files.readAllBytes(Paths.get(uriResult)),HashMap.class);
        HashMap<String, Object> mapExpected = mapper.readValue(Files.readAllBytes(Paths.get(uriExpected)),HashMap.class);
        Assert.assertTrue(ApixUtil.isEqualObject(mapResult, mapExpected, this.ignoredField));
    }

    @Test
    public void oasFileNotExistTest() throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        ApiProject project = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
        Optional<ApiProject> optionalApiProject = Optional.of(project);
        when(apiRepository.findById(any())).thenReturn(optionalApiProject);

        DownloadResponse downloadResponse = command.execute(new ExportRequest("1234",FileFormat.JSON));
        URI uriResult = getClass().getClassLoader().getResource(downloadResponse.getFileUrl()).toURI();
        URI uriExpected = getClass().getClassLoader().getResource("swagger-oas.json").toURI();

        verify(apiProjectConverter,times(1)).convertToOasSwagger2(any(ApiProject.class));

        HashMap<String, Object> mapResult = mapper.readValue(Files.readAllBytes(Paths.get(uriResult)),HashMap.class);
        HashMap<String, Object> mapExpected = mapper.readValue(Files.readAllBytes(Paths.get(uriExpected)),HashMap.class);
        Assert.assertTrue(ApixUtil.isEqualObject(mapResult, mapExpected, this.ignoredField));
    }
    @Test
    public void oasFileNotExistButUpToDateTest() throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        ApiProject project = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);

        uri = getClass().getClassLoader().getResource("swagger-oas.json").toURI();
        HashMap<String, Object> swagger = mapper.readValue(Files.readAllBytes(Paths.get(uri)), HashMap.class);


        Optional<ApiProject> optionalApiProject = Optional.of(project);
        ProjectOasSwagger2 oasSwagger2 = new ProjectOasSwagger2();
        oasSwagger2.setOasFileName("not-exist-path");
        oasSwagger2.setOasSwagger2(swagger);

        Date date = new Date();
        oasSwagger2.setOasFileProjectUpdateDate(date);
        project.setUpdatedAt(date);

        when(apiRepository.findById(any())).thenReturn(optionalApiProject);
        when(swagger2Repository.findProjectOasSwagger2ByProjectId(anyString())).thenReturn(Optional.of(oasSwagger2));

        DownloadResponse downloadResponse = command.execute(new ExportRequest("12345",FileFormat.JSON));
        URI uriResult = getClass().getClassLoader().getResource(downloadResponse.getFileUrl()).toURI();

        verifyNoMoreInteractions(apiProjectConverter);
        HashMap<String, Object> mapResult = mapper.readValue(Files.readAllBytes(Paths.get(uriResult)),HashMap.class);
        Assert.assertTrue(ApixUtil.isEqualObject(mapResult, swagger, this.ignoredField));
    }

    @Test
    public void oasFileExistAndUpToDateTest() throws URISyntaxException, IOException {

        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        ApiProject project = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
        Optional<ApiProject> optionalApiProject = Optional.of(project);
        when(apiRepository.findById(any())).thenReturn(optionalApiProject);

        ProjectOasSwagger2 oasSwagger2 = new ProjectOasSwagger2();
        oasSwagger2.setOasFileName("swagger-oas");


        URI uriExpected = getClass().getClassLoader().getResource("swagger-oas.json").toURI();
        HashMap<String, Object> mapExpected = mapper.readValue(Files.readAllBytes(Paths.get(uriExpected)),HashMap.class);
        oasSwagger2.setOasSwagger2(mapExpected);

        Date date = new Date();
        oasSwagger2.setOasFileProjectUpdateDate(date);
        project.setUpdatedAt(date);

        when(swagger2Repository.findProjectOasSwagger2ByProjectId(anyString())).thenReturn(Optional.of(oasSwagger2));

        DownloadResponse downloadResponse = command.execute(new ExportRequest("123456",FileFormat.JSON));

        URI uriResult = getClass().getClassLoader().getResource(downloadResponse.getFileUrl()).toURI();
        HashMap<String, Object> mapResult = mapper.readValue(Files.readAllBytes(Paths.get(uriResult)),HashMap.class);

        verifyNoMoreInteractions(apiProjectConverter);

        Assert.assertTrue(ApixUtil.isEqualObject(mapResult, mapExpected, this.ignoredField));
    }

}
