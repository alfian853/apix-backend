package com.future.apix.util.validator;

import com.future.apix.entity.apidetail.DataType;
import com.future.apix.entity.apidetail.Schema;

import java.util.HashMap;
import java.util.Map;

public class SchemaValidator {

    private static DataType getType(String s){
        try{
            return DataType.valueOf(s.toUpperCase());
        }
        catch (Exception e){
            return null;
        }
    }

    public static boolean isValid(Schema schema) {
        if(schema == null || schema.getType() == null){return true;}

        boolean itemsIsEmpty = schema.getItems() == null;
        boolean propertiesIsEmpty = schema.getProperties() == null || schema.getProperties().isEmpty();
        boolean formatIsNull = schema.getFormat() == null;
        boolean defaultIsNull = schema.getDefaults() == null;
        boolean patternIsNull = schema.getPattern() == null;
        DataType type = getType(schema.getType());

        if(type == null) return false || (!formatIsNull && schema.getFormat().equals("date-time"));
        System.out.println(schema);
        if( type == DataType.INTEGER){
            return schema.getFormat() != null && !schema.getFormat().equals("double") &&
                    NumberFormatValidator.isValid(schema.getFormat());
        }
        else if( type == DataType.NUMBER){
            return !formatIsNull && NumberFormatValidator.isValid(schema.getFormat())
                    && propertiesIsEmpty && itemsIsEmpty && patternIsNull;
        }
        else if(type == DataType.ARRAY){
            return !itemsIsEmpty && (schema.getItems().getType() != null || schema.getItems().getRef() != null)
            && formatIsNull && propertiesIsEmpty;
        }
        else if(type == DataType.OBJECT){
            return !propertiesIsEmpty && formatIsNull && itemsIsEmpty && isValid(schema.getProperties()) && defaultIsNull;
        }
        else if(type == DataType.STRING){
            return itemsIsEmpty && propertiesIsEmpty;
        }

        return true;
    }

    public static boolean isValid(HashMap<String,Schema> schemas){

        for (Map.Entry<String,Schema> pair : schemas.entrySet()) {
            if (!isValid(pair.getValue())) {
                return false;
            }
        }

        return true;
    }
}
