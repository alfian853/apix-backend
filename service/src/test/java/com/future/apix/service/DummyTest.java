package com.future.apix.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.apidetail.OperationDetail;
import com.future.apix.entity.apidetail.Schema;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.HashMap;

@RunWith(MockitoJUnitRunner.class)
public class DummyTest {

    private ObjectMapper oMapper = new ObjectMapper();

    private static Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    @Test
    public void testSchemaNumber(){

        Schema schema = new Schema();
        schema.setType("object");
//        schema.setFormat("int32");

        HashMap<String,Schema> properties = new HashMap<>();
        Schema s = new Schema();
        s.setType("string");
        properties.put("sume",s);

        schema.setItems(s);
        schema.setProperties(properties);
        OperationDetail requestBody = new OperationDetail();
        requestBody.setSchema(schema);

        validator.validate(requestBody).forEach(x -> System.out.println(x.getMessage()));


    }
}
