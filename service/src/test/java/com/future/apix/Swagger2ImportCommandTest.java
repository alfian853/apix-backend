package com.future.apix;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.repository.ApiRepository;
import com.future.apix.service.command.impl.Swagger2ImportCommandImpl;
import com.future.apix.util.ApixUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
    public void testSchemaNumber(){
        URL url = getClass().getClassLoader().getResource("testcase-oas.json");
        File testFile = new File(url.getPath());
        MockMultipartFile multipartFile = null;
        try {
            multipartFile = new MockMultipartFile("filename.json", "filename.json",
                    "application/json", Files.readAllBytes(Paths.get(url.getPath())
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ApiProject result = command.executeCommand(multipartFile);

        url = getClass().getClassLoader().getResource("import-result.json");
        File expectedFile = new File(url.getPath());
        ApiProject expectedResult = null;
        try {
            expectedResult = mapper.readValue(Files.readAllBytes(Paths.get(url.getPath())), ApiProject.class);
            expectedResult.setId(result.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }

        verify(repository, times(1)).save(any());
        HashMap<String, Object> obj1 = mapper.convertValue(result, HashMap.class);
        HashMap<String, Object> obj2 = mapper.convertValue(expectedResult, HashMap.class);
        System.out.println(obj1.get("info"));
        Assert.assertTrue(ApixUtil.isEqualObject(obj1, obj2,
                new HashSet<>(Arrays.asList("definitions","_signature","$ref"))));


    }
}
