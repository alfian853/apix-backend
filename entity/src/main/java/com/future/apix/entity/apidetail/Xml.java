package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.future.apix.entity.Mappable;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Xml implements Mappable {
    // In Swagger 2.0 called XML Object

    String name, namespace, prefix;
    Boolean attribute, wrapped;
}
