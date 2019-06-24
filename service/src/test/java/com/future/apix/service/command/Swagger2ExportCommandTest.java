package com.future.apix.service.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.ProjectOasSwagger2;
import com.future.apix.repository.ApiRepository;
import com.future.apix.repository.OasSwagger2Repository;
import com.future.apix.response.DownloadResponse;
import com.future.apix.service.command.impl.Swagger2ExportCommandImpl;
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

    @Before
    public void init() throws URISyntaxException {
        command.setObjectMapper(mapper);
        command.setEXPORT_DIR(Paths.get(getClass().getClassLoader().getResource("").toURI()).toString()+"/");
        command.setEXPORT_URL("");
    }

    @Test
    public void oasFileExistButNotUpToDateTest() throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();

        ApiProject project = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
        System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(project));
        Optional<ApiProject> optionalApiProject = Optional.of(project);
        ProjectOasSwagger2 oasSwagger2 = new ProjectOasSwagger2();
        oasSwagger2.setOasFileName("Petstore API_1.0.0_123.json");

        oasSwagger2.setOasFileProjectUpdateDate(new Date(new Date().getTime()-1));
        project.setUpdatedAt(new Date());

        when(apiRepository.findById(any())).thenReturn(optionalApiProject);
        when(swagger2Repository.findProjectOasSwagger2ByProjectId(anyString())).thenReturn(Optional.of(oasSwagger2));

        DownloadResponse downloadResponse = command.executeCommand("123");
        URI uriResult = getClass().getClassLoader().getResource(downloadResponse.getFileUrl()).toURI();
        URI uriExpected = getClass().getClassLoader().getResource("swagger-oas.json").toURI();

        verify(apiProjectConverter,times(1)).convertToOasSwagger2(any(ApiProject.class));

        HashMap<String, Object> mapResult = mapper.readValue(Files.readAllBytes(Paths.get(uriResult)),HashMap.class);
        HashMap<String, Object> mapExpected = mapper.readValue(Files.readAllBytes(Paths.get(uriExpected)),HashMap.class);
        Assert.assertTrue(ApixUtil.isEqualObject(mapResult, mapExpected,
                new HashSet<>(Arrays.asList("definitions","_signature","$ref","required"))));
    }

    @Test
    public void oasFileNotExistTest() throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        ApiProject project = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
        Optional<ApiProject> optionalApiProject = Optional.of(project);
        when(apiRepository.findById(any())).thenReturn(optionalApiProject);

        DownloadResponse downloadResponse = command.executeCommand("1234");
        URI uriResult = getClass().getClassLoader().getResource(downloadResponse.getFileUrl()).toURI();
        URI uriExpected = getClass().getClassLoader().getResource("swagger-oas.json").toURI();

        verify(apiProjectConverter,times(1)).convertToOasSwagger2(any(ApiProject.class));

        HashMap<String, Object> mapResult = mapper.readValue(Files.readAllBytes(Paths.get(uriResult)),HashMap.class);
        HashMap<String, Object> mapExpected = mapper.readValue(Files.readAllBytes(Paths.get(uriExpected)),HashMap.class);
        Assert.assertTrue(ApixUtil.isEqualObject(mapResult, mapExpected,
                new HashSet<>(Arrays.asList("definitions","_signature","$ref","required"))));
    }
    @Test
    public void oasFileNotExistButUpToDateTest() throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        ApiProject project = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
        Optional<ApiProject> optionalApiProject = Optional.of(project);
        ProjectOasSwagger2 oasSwagger2 = new ProjectOasSwagger2();
        oasSwagger2.setOasFileName("not-exist-path.json");

        Date date = new Date();
        oasSwagger2.setOasFileProjectUpdateDate(date);
        project.setUpdatedAt(date);

        when(apiRepository.findById(any())).thenReturn(optionalApiProject);
        when(swagger2Repository.findProjectOasSwagger2ByProjectId(anyString())).thenReturn(Optional.of(oasSwagger2));

        DownloadResponse downloadResponse = command.executeCommand("12345");
        URI uriResult = getClass().getClassLoader().getResource(downloadResponse.getFileUrl()).toURI();
        URI uriExpected = getClass().getClassLoader().getResource("swagger-oas.json").toURI();

        verify(apiProjectConverter,times(1)).convertToOasSwagger2(any(ApiProject.class));

        HashMap<String, Object> mapResult = mapper.readValue(Files.readAllBytes(Paths.get(uriResult)),HashMap.class);
        HashMap<String, Object> mapExpected = mapper.readValue(Files.readAllBytes(Paths.get(uriExpected)),HashMap.class);
        Assert.assertTrue(ApixUtil.isEqualObject(mapResult, mapExpected,
                new HashSet<>(Arrays.asList("definitions","_signature","$ref","required"))));
    }

    @Test
    public void oasFileExistAndUpToDateTest() throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        ApiProject project = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
        Optional<ApiProject> optionalApiProject = Optional.of(project);
        ProjectOasSwagger2 oasSwagger2 = new ProjectOasSwagger2();
        oasSwagger2.setOasFileName("Petstore API_1.0.0_123.json");

        Date date = new Date();
        oasSwagger2.setOasFileProjectUpdateDate(date);
        project.setUpdatedAt(date);

        when(apiRepository.findById(any())).thenReturn(optionalApiProject);
        when(swagger2Repository.findProjectOasSwagger2ByProjectId(anyString())).thenReturn(Optional.of(oasSwagger2));

        DownloadResponse downloadResponse = command.executeCommand("123456");
        URI uriResult = getClass().getClassLoader().getResource(downloadResponse.getFileUrl()).toURI();
        URI uriExpected = getClass().getClassLoader().getResource("swagger-oas.json").toURI();

        verify(apiProjectConverter,times(0)).convertToOasSwagger2(any(ApiProject.class));

        HashMap<String, Object> mapResult = mapper.readValue(Files.readAllBytes(Paths.get(uriResult)),HashMap.class);
        HashMap<String, Object> mapExpected = mapper.readValue(Files.readAllBytes(Paths.get(uriExpected)),HashMap.class);
        Assert.assertTrue(ApixUtil.isEqualObject(mapResult, mapExpected,
                new HashSet<>(Arrays.asList("definitions","_signature","$ref","required"))));
    }

}
