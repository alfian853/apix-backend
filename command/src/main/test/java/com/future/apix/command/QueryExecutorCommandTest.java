package com.future.apix.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.command.impl.QueryExecutorCommandImpl;
import com.future.apix.command.model.QueryExecutorRequest;
import com.future.apix.entity.ApiProject;
import com.future.apix.exception.ConflictException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.ApiRepository;
import com.future.apix.response.ProjectUpdateResponse;
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
import java.util.HashMap;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class QueryExecutorCommandTest {

    @InjectMocks
    QueryExecutorCommandImpl dataUpdate;

    @Spy
    ObjectMapper mapper;

    @Mock
    ApiRepository apiRepository;


    @Before
    public void init() throws IOException, URISyntaxException {
        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        ApiProject project = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
        Optional<ApiProject> optionalApiProject = Optional.of(project);

        when(apiRepository.findById(anyString())).thenReturn(optionalApiProject);
    }

    @Test
    public void updateSuccess() throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource("update-testcase/success-query.json").toURI();
        HashMap<String,Object> query = mapper.readValue(Files.readAllBytes(Paths.get(uri)), HashMap.class);

        ProjectUpdateResponse response = dataUpdate.execute(new QueryExecutorRequest("123", query));
        Assert.assertTrue(response.isSuccess());
    }

    @Test(expected = InvalidRequestException.class)
    public void updateFailedSignatureNotFound() throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource("update-testcase/none-signature-query.json").toURI();
        HashMap<String,Object> query = mapper.readValue(Files.readAllBytes(Paths.get(uri)), HashMap.class);

        ProjectUpdateResponse response = dataUpdate.execute(new QueryExecutorRequest("123", query));
        Assert.assertTrue(response.isSuccess());
    }

    @Test(expected = ConflictException.class)
    public void updateFailedSignatureNotMatch() throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource("update-testcase/signature-not-match-query.json").toURI();
        HashMap<String,Object> query = mapper.readValue(Files.readAllBytes(Paths.get(uri)), HashMap.class);

        ProjectUpdateResponse response = dataUpdate.execute(new QueryExecutorRequest("123", query));
        Assert.assertTrue(response.isSuccess());
    }



}
