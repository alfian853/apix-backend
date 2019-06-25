package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.future.apix.entity.Mappable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Tag implements Mappable {
    String name, description;
    Contact externalDocs;
}
