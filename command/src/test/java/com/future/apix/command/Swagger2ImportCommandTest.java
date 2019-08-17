package com.future.apix.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.command.impl.Swagger2ImportCommandImpl;
import com.future.apix.entity.ApiProject;
import com.future.apix.entity.Team;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ProjectRepository;
import com.future.apix.request.ProjectImportRequest;
import com.future.apix.util.converter.SwaggerToApixOasConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Swagger2ImportCommandTest {

    @InjectMocks
    private Swagger2ImportCommandImpl command;

    @Mock
    private ProjectRepository apiRepository;

    @Mock
    private SwaggerToApixOasConverter converter;

    @Mock
    private ObjectMapper mapper;

    @Test
    public void importTest() {
        MockMultipartFile multipartFile = new MockMultipartFile("filename.json", "filename.json",
                "application/json", "".getBytes()
        );

        ProjectImportRequest request = new ProjectImportRequest(new Team(), multipartFile, FileFormat.JSON);

        when(converter.convert(any())).thenReturn(new ApiProject());
        when(apiRepository.save(any())).thenReturn(new ApiProject());

        ApiProject result = command.execute(request);


        verify(apiRepository, times(1)).save(any());
    }

    @Test(expected = InvalidRequestException.class)
    public void importWithInvalidFileFormatTest() throws IOException {
        MockMultipartFile multipartFile = new MockMultipartFile("filename.json", "filename.json",
                "application/json", "".getBytes()
        );

        ProjectImportRequest request = new ProjectImportRequest(new Team(), multipartFile);

        when(mapper.readValue(any(InputStream.class),eq(HashMap.class)))
                .thenThrow(new IOException());
        ApiProject result = command.execute(request);


    }


}
