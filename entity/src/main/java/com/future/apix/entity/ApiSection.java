package com.future.apix.entity;

import com.future.apix.entity.apidetail.Contact;
import com.future.apix.entity.apidetail.Path;
import com.future.apix.entity.apidetail.Tag;
import lombok.Data;
import org.springframework.http.HttpMethod;

import java.util.HashMap;

@Data
public class ApiSection{
//    HashMap<path, HashMap<httpMethod, ApiMethodData> > paths = new HashMap<>();
    HashMap<String, Path> paths = new HashMap<>();

    //simpan data type

    // simpan sesuai dengan Tags di swagger OAS
    Tag info;
}
