package com.future.apix.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class JsonUtil {

    public static ObjectMapper mapper = new ObjectMapper();

    public static void remappingKeys(Map<String, Object> target, Map<String, String> keyMapping){
        for(Object obj : target.entrySet()){
            Map.Entry<String, Object> pair = (Map.Entry<String, Object>) obj;
            if(pair.getKey() == null)continue;
            else if(keyMapping.containsKey(pair.getKey())){
                System.out.println("replaced "+pair.getValue().toString());
                target.put(keyMapping.get(pair.getKey()),pair.getValue());
                target.remove(pair.getKey());
            }

            if(pair.getValue() instanceof Map){
                remappingKeys((Map<String, Object>) pair.getValue(),keyMapping);
            }
            else if(pair.getValue() instanceof List){
                ((List)pair.getValue()).forEach(val -> {
                    if(val instanceof Map){
                        remappingKeys((Map<String, Object>) val, keyMapping);
                    }
                });
            }
        }
    }
}
