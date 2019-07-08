package com.future.apix.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.Mappable;
import org.springframework.util.StringUtils;

import java.util.*;

public class ApixUtil {

    /*
        Tujuannya digunakan dalam unit test untuk compare hasil awal dan hasil update
     */
    private static ObjectMapper mapper = new ObjectMapper();

    public static HashMap<String,Object> toStrObjMap(Object object){
        return (HashMap<String,Object>) object;
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
        if(arr1.size() != arr2.size())return false;
        int len = arr1.size();
        for(int i = 0; i < len; ++i){
            Object val1 = arr1.get(i);
            Object val2 = arr2.get(i);

            if(val1.getClass() != val2.getClass()){
                System.out.println("T");
                return false;
            }
            else if(val1 instanceof List){
                if(!isEqualArray(toList(val1), toList(val2), ignoredField)){
                    return false;
                }
            }
            else if(val1 instanceof Map){
                if(!isEqualObject(toStrObjMap(val1), toStrObjMap(val2), ignoredField)){
                    System.out.println("rrr");
                    System.out.println(arr1);
                    System.out.println(arr2);

                    return false;
                }
            }
            else if(val1 instanceof Mappable){
                HashMap<String, Object> tmp1 = mapper.convertValue(val1, HashMap.class);
                HashMap<String, Object> tmp2 = mapper.convertValue(val2, HashMap.class);
                if(!isEqualObject(tmp1, tmp2, ignoredField)){
                    System.out.println("xx");
                    return false;
                }
            }
            else if(!val1.equals(val2)){
                System.out.println("Y");
                System.out.println(val1);
                System.out.println(val2);
                return false;
            }
        }
        return true;
    }

    public static boolean isEqualObject(HashMap<String, Object> obj1, HashMap<String, Object> obj2, Set<String> ignoredField){
        Set<String> key1 = obj1.keySet();
        key1.removeAll(ignoredField);
        Set<String> key2 = obj2.keySet();
        key2.removeAll(ignoredField);
        if(key1.size() != key2.size()){
            System.out.println("O");
            System.out.println(key1);
            System.out.println(key2);
            System.out.println(obj1);
            System.out.println(obj2);
            return false;
        }

        for(String key : key1){
            Object val1 = obj1.get(key);
            Object val2 = obj2.get(key);

            if(val1 == null && val2 == null){
                continue;
            }
            else if(val1 == null ^ val2 == null){
                System.out.println(val1);
                System.out.println(val2);
                System.out.println(key);
                System.out.println("A");
                return false;
            }
            if(val1 instanceof Number && val2 instanceof Number){
                Number d1 = (Number) val1;
                Number d2 = (Number) val2;
                if(Math.abs(d1.doubleValue()-d2.doubleValue()) > 0.0000001){
                    return false;
                }
            }
            else if(val1.getClass() != val2.getClass()){
                System.out.println(val1.getClass());
                System.out.println(val2.getClass());
                System.out.println(key);
                System.out.println(val1);
                System.out.println(val2);
                System.out.println("B");
                return false;
            }
            else if(val1 instanceof List){
                if(!isEqualArray(toList(val1), toList(val2), ignoredField)){
                    return false;
                }
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
