package com.future.apix.entity.apidetail;

import lombok.Data;

@Data
public class ProjectInfo {
    String description,
            version,
            title,
            termsOfService;

    Object contact;
    License license;
}

