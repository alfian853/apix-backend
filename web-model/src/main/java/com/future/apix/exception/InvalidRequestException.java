package com.future.apix.exception;

import lombok.Data;

@Data
public class InvalidRequestException extends DefaultRuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }

    public InvalidRequestException() {

    }
}
