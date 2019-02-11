package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Xml {
    // In Swagger 2.0 called XML Object

    String name, namespace, prefix;
    Boolean attribute, wrapped;
}
