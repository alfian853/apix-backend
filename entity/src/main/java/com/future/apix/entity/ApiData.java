package com.future.apix.entity;

import com.future.apix.entity.apidetail.RequestBody;
import com.future.apix.entity.apidetail.ResponseBody;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Data
@Document("ApiDatas")
public class ApiData implements Serializable {

    String name,
            method,
            path,
            description;

    RequestBody request;
    ResponseBody responses;
}
