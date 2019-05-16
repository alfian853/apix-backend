package com.future.apix.exception;

import lombok.Data;

@Data
public class InvalidAuthenticationException extends DefaultRuntimeException {
    public InvalidAuthenticationException(String message) { super (message); }
    public InvalidAuthenticationException(){}
}
