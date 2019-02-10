package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
public class Items {
    String type;

    @Field("default")
    @JsonProperty("default")
    Object itemDefault;

    @Field("enum")
    @JsonProperty("enum")
    List<String> itemEnum;
}
