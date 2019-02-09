package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Items {
    String type;

    @JsonProperty("default")
    Object itemDefault;

    @JsonProperty("enum")
    List<String> itemEnum;
}
