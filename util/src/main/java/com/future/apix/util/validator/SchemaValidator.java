package com.future.apix.util.validator;

import com.future.apix.entity.apidetail.DataType;
import com.future.apix.entity.apidetail.Schema;

import java.util.HashMap;
import java.util.List;
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

        if(type == null) return (!formatIsNull && schema.getFormat().equals("date-time"));
        
        if( type == DataType.INTEGER){
            boolean res;
            //optimasi pengecekan, jadi pake switch case
            switch (schema.getFormat()){
                case "double":
                case "float":
                    res = false;
                    break;
                default:
                    res = schema.getFormat() != null && NumberFormatValidator.isValid(schema.getFormat());
            }
            if(!res){
                System.out.println("invalid integer");
                System.out.println(schema);
            }
            return res;
        }
        else if( type == DataType.NUMBER){
            boolean res = formatIsNull || (!formatIsNull && NumberFormatValidator.isValid(schema.getFormat())
                    && propertiesIsEmpty && itemsIsEmpty && patternIsNull);
            if(!res){
                System.out.println("invalid number");
                System.out.println(schema);
            }
            return res;
        }
        else if(type == DataType.ARRAY){
            boolean res = !itemsIsEmpty && (schema.getItems().getType() != null || schema.getItems().getRef() != null)
                    && formatIsNull && propertiesIsEmpty;
//            System.out.println("itemIsEmpty:" + itemsIsEmpty);
//            System.out.println("getType: " + schema.getItems().getType());
//            System.out.println("getRef: " + schema.getItems().getRef());
//            System.out.println("formatIsNull: " + formatIsNull);
//            System.out.println("propertiesIsEmpty: " + propertiesIsEmpty);
//            System.out.println("\n");
            if(!res){
                System.out.println("invalid array");
                System.out.println(schema);
            }
            return res;
        }
        else if(type == DataType.OBJECT){
            boolean res = !propertiesIsEmpty && formatIsNull && itemsIsEmpty && isValid(schema.getProperties()) && defaultIsNull;
//            System.out.println("itemIsEmpty:" + itemsIsEmpty);
//            System.out.println("defaultIsNull: " + defaultIsNull);
//            System.out.println("isValid: " + isValid(schema.getProperties()));
//            System.out.println("formatIsNull: " + formatIsNull);
//            System.out.println("propertiesIsEmpty: " + propertiesIsEmpty);
//            System.out.println("\n");
            if(!res){
                System.out.println("invalid object");
                System.out.println(schema);
            }
            return res;
        }
        else if(type == DataType.STRING){
            boolean res = itemsIsEmpty && propertiesIsEmpty;
            if(!res){
                System.out.println("invalid string");
                System.out.println(schema);
            }
            return res;
        }

        return true;
    }

    public static boolean isValid(HashMap<String,Schema> schemas){
        if(schemas == null)return true;
        for (Map.Entry<String,Schema> pair : schemas.entrySet()) {
//            System.out.println(pair);
            if (!isValid(pair.getValue())) {
                return false;
            }
        }

        return true;
    }
}