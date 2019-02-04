package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.HashMap;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Definition {
    String title,type;
    HashMap<String,Schema> properties;
}
