package com.future.apix.entity.apidetail;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.future.apix.entity.Mappable;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.Email;

@Data
public class Contact implements Mappable {
    String name, description;

    @Email
    String email;

    @URL
    String url;
}
