package com.future.apix.entity;

import com.future.apix.entity.apidetail.Contact;
import com.future.apix.entity.apidetail.Tag;
import lombok.Data;
import org.springframework.http.HttpMethod;

import java.util.HashMap;

@Data
public class ApiSection{
//    HashMap<String, HashMap<HttpMethod, ApiMethodData> > paths = new HashMap<>();
    HashMap<String, HashMap<String, ApiMethodData> > paths = new HashMap<>();

    //simpan data type

    // simpan sesuai dengan Tags di swagger OAS
    Tag tag;
}
