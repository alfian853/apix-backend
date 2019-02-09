package com.future.apix.exception;

import lombok.Data;

@Data
public class ConflictException extends DefaultRuntimeException {
    public ConflictException(String message) {
        super(message);
    }

    public ConflictException() {
    }
}
