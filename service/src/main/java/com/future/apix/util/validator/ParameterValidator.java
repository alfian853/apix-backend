package com.future.apix.util.validator;

import com.future.apix.entity.apidetail.DataType;
import com.future.apix.entity.apidetail.Parameter;

import javax.xml.crypto.Data;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ParameterValidator {

    private static DataType getType(String s){
        try{
            return DataType.valueOf(s.toUpperCase());
        }
        catch (Exception e){
            return null;
        }
    }

    private static boolean isValid(Parameter parameter){
        DataType type = getType(parameter.getType());

        if(type == null)return false;

        if(type == DataType.STRING){
            return (parameter.getFormat() == null || parameter.getFormat().equals("date-time"));
        }
        else if(type == DataType.INTEGER){
            return parameter.getFormat() != null && !parameter.getFormat().equals("double") &&
                    NumberFormatValidator.isValid(parameter.getFormat());
        }
        else if(type == DataType.NUMBER){
            return NumberFormatValidator.isValid(parameter.getFormat()) && (parameter.getPattern() == null);
        }
        else{
            return parameter.getFormat() == null && parameter.getPattern() == null;
        }

    }

    public static boolean isValid(HashMap<String,Parameter> parameters) {
        Iterator iterator = parameters.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String,Parameter> pair = (Map.Entry<String, Parameter>) iterator.next();
            if(!isValid(pair.getValue())){
                System.out.println("ori : "+pair.getValue());
                return false;
            }
        }
        return true;

    }
}
