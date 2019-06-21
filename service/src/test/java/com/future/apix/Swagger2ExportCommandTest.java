package com.future.apix;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.ProjectOasSwagger2;
import com.future.apix.repository.ApiRepository;
import com.future.apix.repository.OasSwagger2Repository;
import com.future.apix.service.command.impl.Swagger2ExportCommandImpl;
import com.future.apix.util.ApixUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Swagger2ExportCommandTest {

    @InjectMocks
    private Swagger2ExportCommandImpl command;

    @Autowired
    ApiRepository apiRepository;

    @Autowired
    OasSwagger2Repository swagger2Repository;

    private ObjectMapper mapper = new ObjectMapper();

    @Before
    public void before(){
        this.command.setObjectMapper(mapper);
    }

    @Test
    public void test(){
        URL url = getClass().getClassLoader().getResource("apix-oas.json");
        ApiProject project = null;
        try {
            project = mapper.readValue(Files.readAllBytes(Paths.get(url.getPath())), ApiProject.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        when(apiRepository.findById(any())).thenReturn(new Optional<>(project));

        ProjectOasSwagger2 oasSwagger2 = new ProjectOasSwagger2();
        oasSwagger2.set


    }

}
