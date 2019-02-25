package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Email;

@Data
public class Contact {
    String name, description;

    @Email
    String email;

    @URL
    String url;
}
