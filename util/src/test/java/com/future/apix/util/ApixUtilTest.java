package com.future.apix.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;

@RunWith(MockitoJUnitRunner.class)
public class ApixUtilTest {

    private JsonUtil jsonUtil = new JsonUtil(Comparator.comparingInt(o -> ((int) o.get("index"))));

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void renameMapTest() throws URISyntaxException, IOException {
        URI inputUri = getClass().getClassLoader().getResource("apix-util-test-input.json").toURI();
        URI outputUri = getClass().getClassLoader().getResource("apix-util-test-output.json").toURI();

        HashMap<String, Object> input = mapper.readValue(Files.readAllBytes(Paths.get(inputUri)), HashMap.class);

        HashMap<String, String> keyMap = new HashMap<>();
        keyMap.put("key1", "newKey1");
        keyMap.put("key2.2.1", "newKey2.2.1");

        HashMap<String, Object> output = mapper.readValue(Files.readAllBytes(Paths.get(outputUri)), HashMap.class);

        jsonUtil.remappingKeys(input, keyMap);

        Assert.assertTrue(jsonUtil.isEqualObject(input, output));
    }

}
