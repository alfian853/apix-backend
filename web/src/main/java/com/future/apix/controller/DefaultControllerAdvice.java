package com.future.apix.controller;

import com.future.apix.exception.*;
import com.future.apix.response.RequestResponse;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Objects;


@ControllerAdvice
public class DefaultControllerAdvice {

    @ExceptionHandler(value={Exception.class, DefaultRuntimeException.class})
    @RequestMapping(produces = "application/vnd.error+json")
    public ResponseEntity<RequestResponse> DefaultExceptionHandler(Exception exception){

        RequestResponse response = new RequestResponse();
        if(exception instanceof DataIntegrityViolationException){
            DataIntegrityViolationException e = (DataIntegrityViolationException) exception;
                response.setMessage(Objects.requireNonNull(e.getRootCause()).getMessage());
        }
        else{
            response.setMessage(exception.getMessage());
        }
        exception.printStackTrace();
        response.setStatusToFailed();
        if(response.getMessage() == null){
            response.setMessage("Internal Server Error!");
        }
        HttpHeaders headers = new HttpHeaders();
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return new ResponseEntity<>(response, headers,status);
    }

    @ExceptionHandler(DataNotFoundException.class)
    @RequestMapping(produces = "application/vnd.error+json")
    public ResponseEntity<RequestResponse> DataNotFoundExceptionHandler(DataNotFoundException exception){

        exception.printStackTrace();

        RequestResponse response = new RequestResponse();
        response.setStatusToFailed();
        response.setMessage(exception.getMessage());
        HttpHeaders headers = new HttpHeaders();
        HttpStatus status = HttpStatus.NOT_FOUND;
        return new ResponseEntity<>(response, headers,status);
    }

    @ExceptionHandler(value = {DuplicateEntryException.class, InvalidRequestException.class})
    @RequestMapping(produces = "application/vnd.error+json")
    public ResponseEntity<RequestResponse> duplicateEntryExceptionHandler(DefaultRuntimeException exception){

        exception.printStackTrace();

        RequestResponse response = new RequestResponse();
        response.setStatusToFailed();
        response.setMessage(exception.getMessage());
        HttpHeaders headers = new HttpHeaders();
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(response, headers,status);
    }

    @ExceptionHandler(value = {ConflictException.class})
    @RequestMapping(produces = "application/vnd.error+json")
    public ResponseEntity<RequestResponse> conflictExceptionHandler(ConflictException exception){
        exception.printStackTrace();

        return new ResponseEntity<>(
                RequestResponse.failed(exception.getMessage()),
                new HttpHeaders(),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(value = {InvalidJwtAuthenticationException.class})
    @RequestMapping(produces = "application/vnd.error+json")
    public ResponseEntity<RequestResponse> invalidJwtToken(InvalidJwtAuthenticationException exception) {
        exception.printStackTrace();

        return new ResponseEntity<>(RequestResponse.failed(exception.getMessage()),
                new HttpHeaders(),
                HttpStatus.UNAUTHORIZED);

    }

    @ExceptionHandler(value = {InvalidAuthenticationException.class})
    @RequestMapping(produces = "application/vnd.error+json")
    public ResponseEntity<RequestResponse> invalidAuthenticationExceptionHandler(InvalidAuthenticationException exception){
        exception.printStackTrace();

        return new ResponseEntity<>(
                RequestResponse.failed(exception.getMessage()),
                new HttpHeaders(),
                HttpStatus.UNAUTHORIZED
        );
    }


}
