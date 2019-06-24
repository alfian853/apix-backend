package com.future.apix.service.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.repository.ApiRepository;
import com.future.apix.service.command.impl.Swagger2ImportCommandImpl;
import com.future.apix.util.ApixUtil;
import com.google.gson.Gson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class Swagger2ImportCommandTest {
    private ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private Swagger2ImportCommandImpl command;

    @Mock
    private ApiRepository repository;

    @Before
    public void init(){
        command.setMapper(mapper);
    }

    @Test
    public void importTest() throws URISyntaxException {
        URI uri = null;
        uri = getClass().getClassLoader().getResource("swagger-oas.json").toURI();
        File testFile = new File(uri.getPath());
        MockMultipartFile multipartFile = null;
        System.out.println(testFile.getPath());
        try {
            multipartFile = new MockMultipartFile("filename.json", "filename.json",
                    "application/json", Files.readAllBytes(Paths.get(uri))
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        ApiProject result = command.executeCommand(multipartFile);

        uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        ApiProject expectedResult = null;
        try {
            expectedResult = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
            expectedResult.setId(result.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }

        verify(repository, times(1)).save(any());
        HashMap<String, Object> obj1 = mapper.convertValue(result, HashMap.class);
        HashMap<String, Object> obj2 = mapper.convertValue(expectedResult, HashMap.class);
        Assert.assertTrue(ApixUtil.isEqualObject(obj1, obj2,
                new HashSet<>(Arrays.asList("definitions","_signature","$ref","required","createdAt","updatedAt"))));


    }
}