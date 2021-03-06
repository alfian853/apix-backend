package com.future.apix.util.converter;

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
import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class ApiProjectConverterTest {

    @InjectMocks
    ApiProjectConverter converter;

    JsonUtil jsonUtil = new JsonUtil((map1, map2) -> {
        String key = "name";
        if(!map1.containsKey(key)){
            key = "ref";
        }
        return map1.get(key).toString().compareTo(map2.get(key).toString());
    });

    private ObjectMapper mapper = new ObjectMapper();
    private HashSet<String> ignoredField = new HashSet<>(
            Arrays.asList("definitions","_signature","$ref","required","security","externalDocs")
    );

    @Test
    public void converterTest() throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        HashMap<String, Object> temp = mapper.readValue(Files.readAllBytes(Paths.get(uri)), HashMap.class);
        ApiProject project = mapper.convertValue(temp, ApiProject.class);

        Map<String, Object> mapResult = converter.convertToOasSwagger2(project);


        URI uriExpected = getClass().getClassLoader().getResource("swagger-oas.json").toURI();
        HashMap<String, Object> mapExpected = mapper.readValue(Files.readAllBytes(Paths.get(uriExpected)),HashMap.class);
        Assert.assertTrue(jsonUtil.isEqualObject(mapResult, mapExpected, this.ignoredField));
    }
}
