package com.future.apix.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.Mappable;

import java.util.*;

public class ApixUtil {

    /*
        Tujuannya digunakan dalam unit test untuk compare hasil awal dan hasil update
     */
    private static ObjectMapper mapper = new ObjectMapper();

    public static Map<String,Object> toStrObjMap(Object object){
        return (Map<String,Object>) object;
    }
    public static List<Object> toList(Object object){
        return (List<Object>) object;
    }

    public static boolean isEqualArray(List<Object> arr1,List<Object> arr2,Set<String> ignoredField){
        Comparator comparator = (o1, o2) -> {
            if(o1 instanceof String){
                return ((String) o1).compareTo((String) o2);
            }
            else if(o1 instanceof Integer || o1 instanceof Double){
                return ((Double) o1).compareTo((Double) o2);
            }
            else if(o1 instanceof Mappable || o1 instanceof Map){
                HashMap<String, Object> o1m = mapper.convertValue(o1, HashMap.class);
                HashMap<String, Object> o2m = mapper.convertValue(o2, HashMap.class);
                String key = "name";
                if(!o1m.containsKey(key)){
                    key = "ref";
                }
                return o1m.get(key).toString().compareTo(o2m.get(key).toString());
            }
            else if(o1 instanceof List){
                throw new RuntimeException("can't compare nested array");
            }
            throw new RuntimeException("can't compare array elements");
        };
        arr1.sort(comparator);
        arr2.sort(comparator);
        if(arr1.size() != arr2.size()){
            return false;
        }
        int len = arr1.size();
        for(int i = 0; i < len; ++i){

            if(!isEqualObject(arr1.get(i), arr2.get(i),ignoredField) ){
                return false;
            }
        }
        return true;
    }

    public static boolean isEqualObject(Object obj1, Object obj2, Set<String> ignoredField){

        if(obj1 == null && obj2 == null){
            return true;
        }
        else if(obj1 == null ^ obj2 == null){
            return false;
        }
        if(obj1 instanceof Number && obj2 instanceof Number){
            Number d1 = (Number) obj1;
            Number d2 = (Number) obj2;
            if(Math.abs(d1.doubleValue()-d2.doubleValue()) > 0.0000001){
                return false;
            }
        }
        else if(obj1 instanceof Map || obj1 instanceof Mappable){
            Map<String, Object> map1;
            Map<String, Object> map2;
            if(obj1 instanceof Mappable){
                map1 = (Map<String, Object>) obj1;
                map2 = (Map<String, Object>) obj2;
            }
            else{
                map1 = mapper.convertValue(obj1, HashMap.class);
                map2 = mapper.convertValue(obj2, HashMap.class);

            }
            Set<String> key1 = map1.keySet();
            key1.removeAll(ignoredField);
            Set<String> key2 = map2.keySet();
            key2.removeAll(ignoredField);
            if(key1.size() != key2.size()){
                return false;
            }

            for(String key : key1){
                Object val1 = map1.get(key);
                Object val2 = map2.get(key);
                if(!isEqualObject(val1, val2, ignoredField)){
                    return false;
                }
            }
        }
        else if(obj1.getClass() != obj2.getClass()){
            return false;
        }
        else if(obj1 instanceof List){
            if(!isEqualArray(toList(obj1), toList(obj2), ignoredField)){
                return false;
            }
        }
        else{
            return obj1.equals(obj2);
        }

        return true;
    }
}
