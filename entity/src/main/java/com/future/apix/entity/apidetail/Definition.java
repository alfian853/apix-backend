package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Definition {
    String title,type;
    HashMap<String,Schema> properties;

    List<String> required;
    Xml xml;
}
