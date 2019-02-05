package com.future.apix.util.validator;

import com.future.apix.entity.apidetail.NumberFormat;

import java.util.HashSet;
import java.util.Set;

public class NumberFormatValidator {

    private static Set<String> numberFormats = new HashSet<>();

    static {
        for(NumberFormat format : NumberFormat.values()){
            numberFormats.add(format.toString());
        }
    }

    public static boolean isValid(String s){
        return (s != null) && numberFormats.contains(s.toUpperCase());
    }

}
