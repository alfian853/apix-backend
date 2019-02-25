package com.future.apix.util.validator;

import com.future.apix.entity.apidetail.DataType;
import com.future.apix.entity.apidetail.RequestBody;

public class BodyValidator {

    private DataType getType(String s){
        try{
            return DataType.valueOf(s.toUpperCase());
        }
        catch (Exception e){
            return null;
        }
    }

    public static boolean isValid(RequestBody requestBody) {
        if(requestBody.getIn() == null){
            System.out.println(requestBody);
        }
        return requestBody.getIn().equals("formData") ||
                (requestBody.getSchemaLazily() != null && SchemaValidator.isValid(requestBody.getSchemaLazily()));
    }
}
