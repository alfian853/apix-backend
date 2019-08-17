package com.future.apix.util.jsonquery;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.ApiProject;
import com.future.apix.util.JsonUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class JsonQueryExecutorTest {

    @InjectMocks
    JsonQueryExecutor queryExecutor;

    private ObjectMapper mapper = new ObjectMapper();

    private HashSet<String> ignoredField = new HashSet<>(
            Arrays.asList("definitions","_signature","$ref","required","security","externalDocs")
    );

    private JsonUtil jsonUtil = new JsonUtil((map1, map2) -> {
        String key = "name";
        if(!map1.containsKey(key)){
            key = "ref";
        }
        return map1.get(key).toString().compareTo(map2.get(key).toString());
    });

    @Test
    public void executeQueryToMapSuccess() throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        Map project = mapper.readValue(Files.readAllBytes(Paths.get(uri)), HashMap.class);

        URI uriQuery = getClass().getClassLoader().getResource("update-testcase/success-query.json").toURI();
        Map<String,Object> query = mapper.readValue(Files.readAllBytes(Paths.get(uriQuery)), HashMap.class);

        URI uriResult = getClass().getClassLoader().getResource("apix-oas-update-result.json").toURI();
        Map expected = mapper.readValue(Files.readAllBytes(Paths.get(uriResult)), HashMap.class);

        queryExecutor.executeQuery(project, query);

        Assert.assertTrue(jsonUtil.isEqualObject(project, expected, this.ignoredField));
    }

    @Test
    public void executeQueryToObjectSuccess() throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        HashMap project = mapper.readValue(Files.readAllBytes(Paths.get(uri)), HashMap.class);

        URI uriQuery = getClass().getClassLoader().getResource("update-testcase/success-query.json").toURI();
        Map<String,Object> query = mapper.readValue(Files.readAllBytes(Paths.get(uriQuery)), HashMap.class);

        URI uriResult = getClass().getClassLoader().getResource("apix-oas-update-result.json").toURI();
        Map expected = mapper.readValue(Files.readAllBytes(Paths.get(uriResult)), HashMap.class);

        Object updatedProject = queryExecutor.executeQuery((Object)project, query);

        Assert.assertTrue(jsonUtil.isEqualObject(updatedProject, expected, this.ignoredField));
    }
}
