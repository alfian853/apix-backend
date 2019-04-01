package com.future.apix.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.future.apix.entity.apidetail.OperationDetail;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.http.MediaType;

import javax.validation.Valid;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMethodData implements Mappable {
    // In Swagger 2.0 called Operation Object

    //for validation of edition
    @Field("_signature")
    @JsonProperty("_signature")
    String signature;
    String summary,description,operationId;
    Boolean deprecated;

    List<String> consumes = Collections.singletonList(MediaType.APPLICATION_JSON_VALUE);
    List<String> produces = Collections.singletonList(MediaType.APPLICATION_JSON_VALUE);

    @Valid
    OperationDetail request = new OperationDetail();

    //responseBody sama seperti request
    HashMap<String, OperationDetail> responses = new HashMap<>();

}
