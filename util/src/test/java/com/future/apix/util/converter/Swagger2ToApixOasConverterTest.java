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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

@RunWith(MockitoJUnitRunner.class)
public class Swagger2ToApixOasConverterTest {
    private ObjectMapper mapper = new ObjectMapper();

    @InjectMocks
    private SwaggerToApixOasConverter converter;
    private JsonUtil jsonUtil = new JsonUtil((map1, map2) -> {
        String key = "name";
        if(!map1.containsKey(key)){
            key = "ref";
        }
        return map1.get(key).toString().compareTo(map2.get(key).toString());
    });
    @Test
    public void importTest() throws URISyntaxException, IOException {
        URI uri = getClass().getClassLoader().getResource("swagger-oas.json").toURI();

        HashMap<String, Object> input = mapper.readValue(Files.readAllBytes(Paths.get(uri)),HashMap.class);

        ApiProject result = converter.convert(input);

        uri = getClass().getClassLoader().getResource("apix-oas.json").toURI();
        ApiProject expectedResult = null;
        try {
            expectedResult = mapper.readValue(Files.readAllBytes(Paths.get(uri)), ApiProject.class);
            expectedResult.setId(result.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
        HashMap<String, Object> obj1 = mapper.convertValue(result, HashMap.class);
        HashMap<String, Object> obj2 = mapper.convertValue(expectedResult, HashMap.class);
        Assert.assertTrue(jsonUtil.isEqualObject(obj1, obj2,
                new HashSet<>(Arrays.asList("githubProject","definitions","_signature","$ref","required","createdAt","updatedAt"))));


    }
}
