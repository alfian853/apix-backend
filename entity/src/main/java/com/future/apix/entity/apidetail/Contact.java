package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Contact {
    String name, description, url, email;
}
