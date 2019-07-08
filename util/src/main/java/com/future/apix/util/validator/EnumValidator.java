package com.future.apix.util.validator;

import com.future.apix.entity.apidetail.DataType;
import com.future.apix.entity.apidetail.NumberFormat;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.Set;

public class EnumValidator {

    private static Set<String> numberFormats, httpMethods, dataTypes;

    static {
        for(NumberFormat format : NumberFormat.values()){
            numberFormats.add(format.toString());
        }

        for(HttpMethod method : HttpMethod.values()){
            httpMethods.add(method.toString());
        }

        for(DataType type : DataType.values()){
            dataTypes.add(type.toString());
        }

    }

    public static boolean isValidNumberFormat(String s){
        return numberFormats.contains(s.toUpperCase());
    }

    public static boolean isValidHttpMethod(String s){
        return httpMethods.contains(s.toUpperCase());
    }

    public static boolean isValidDataType(String s){
        return dataTypes.contains(s.toUpperCase());
    }

}
