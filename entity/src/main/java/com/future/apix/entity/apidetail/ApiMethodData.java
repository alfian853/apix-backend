package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.future.apix.entity.Mappable;
import com.future.apix.entity.apidetail.OperationDetail;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.http.MediaType;

import javax.validation.Valid;
import java.util.*;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiMethodData implements Mappable {
    // In Swagger 2.0 called Operation Object

    //for validation of edition
    @Field("_signature")
    @JsonProperty("_signature")
    String signature = UUID.randomUUID().toString();
    String summary,description,operationId;
    Boolean deprecated;

    List<String> consumes;
    List<String> produces;//= Collections.singletonList(MediaType.APPLICATION_JSON_VALUE);

    @Valid
    OperationDetail request = new OperationDetail();

    //responseBody sama seperti request
    Map<String, OperationDetail> responses = new HashMap<>();

}
