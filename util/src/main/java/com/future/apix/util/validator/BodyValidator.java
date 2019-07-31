package com.future.apix.util.validator;

import com.future.apix.entity.apidetail.DataType;
import com.future.apix.entity.apidetail.OperationDetail;

public class BodyValidator {

    private DataType getType(String s){
        try{
            return DataType.valueOf(s.toUpperCase());
        }
        catch (Exception e){
            return null;
        }
    }

    public static boolean isValid(OperationDetail requestBody) {
        if(requestBody.getIn() == null){
            return true;
        }
        return requestBody.getIn().equals("formData") ||
                (requestBody.getSchemaLazily() != null && SchemaValidator.isValid(requestBody.getSchemaLazily()));
    }
}
