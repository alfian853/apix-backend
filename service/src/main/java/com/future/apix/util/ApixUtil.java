package com.future.apix.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.Mappable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ApixUtil {

    private static ObjectMapper mapper = new ObjectMapper();

    public static HashMap<String,Object> toStrObjMap(Object object){
        return (HashMap<String,Object>) object;
    }

    public static boolean isEqualObject(HashMap<String, Object> obj1, HashMap<String, Object> obj2, Set<String> ignoredField){
        Set<String> key1 = obj1.keySet();
        key1.removeAll(ignoredField);
        Set<String> key2 = obj2.keySet();
        key2.removeAll(ignoredField);
        if(key1.size() != key2.size() || !key1.equals(key2)){
            return false;
        }

        for(String key : key1){
            Object val1 = obj1.get(key);
            Object val2 = obj2.get(key);

            if(val1 == null && val2 == null){
                continue;
            }
            else if(val1 == null ^ val2 == null){
                return false;
            }
            else if(val1.getClass() != val2.getClass()){
                return false;
            }
            else if(val1 instanceof Map){
                if(!isEqualObject(toStrObjMap(val1), toStrObjMap(val2), ignoredField)){
                    return false;
                }
            }
            else if(val1 instanceof Mappable){
                HashMap<String, Object> tmp1 = mapper.convertValue(val1, HashMap.class);
                HashMap<String, Object> tmp2 = mapper.convertValue(val2, HashMap.class);
                if(!isEqualObject(tmp1, tmp2, ignoredField)){
                    return false;
                }
            }
            else{
                if(!val1.equals(val2)){
                    System.out.println("not equal!, key: "+key);
                    System.out.println(val1);
                    System.out.println(val2);
                    return false;
                }
            }
        }

        return true;
    }
}
