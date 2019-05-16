package com.future.apix.response;

import lombok.Data;

import java.util.List;

@Data
public class MethodArgumentInvalidResponse extends RequestResponse {
    List<String> errors;

    public MethodArgumentInvalidResponse buildErrors(List<String> errors) {
        this.errors = errors;
        return this;
    }
}
