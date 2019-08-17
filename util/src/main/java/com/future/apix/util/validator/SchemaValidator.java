package com.future.apix.util.validator;

import com.future.apix.entity.apidetail.DataType;
import com.future.apix.entity.apidetail.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SchemaValidator {

    private static Logger logger = LoggerFactory.getLogger(SchemaValidator.class);
    
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
                logger.info("invalid integer");
                logger.info(schema.toString());
            }
            return res;
        }
        else if( type == DataType.NUMBER){
            boolean res = formatIsNull || (!formatIsNull && NumberFormatValidator.isValid(schema.getFormat())
                    && propertiesIsEmpty && itemsIsEmpty && patternIsNull);
            if(!res){
                logger.info("invalid number");
                logger.info(schema.toString());
            }
            return res;
        }
        else if(type == DataType.ARRAY){
            boolean res = !itemsIsEmpty && (schema.getItems().getType() != null || schema.getItems().getRef() != null)
                    && formatIsNull && propertiesIsEmpty;
            if(!res){
                logger.info("invalid array");
                logger.info(schema.toString());
            }
            return res;
        }
        else if(type == DataType.OBJECT){
            boolean res = formatIsNull && itemsIsEmpty && isValid(schema.getProperties()) && defaultIsNull;
            if(!res){
                logger.info("invalid object");
                logger.info(schema.toString());
            }
            return res;
        }
        else if(type == DataType.STRING){
            boolean res = itemsIsEmpty && propertiesIsEmpty;
            if(!res){
                logger.info("invalid string");
                logger.info(schema.toString());
            }
            return res;
        }

        return true;
    }

    public static boolean isValid(HashMap<String,Schema> schemas){
        if(schemas == null)return true;
        for (Map.Entry<String,Schema> pair : schemas.entrySet()) {
            if (!isValid(pair.getValue())) {
                return false;
            }
        }

        return true;
    }
}
