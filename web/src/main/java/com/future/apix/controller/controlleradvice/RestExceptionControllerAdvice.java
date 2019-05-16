package com.future.apix.controller.controlleradvice;

import com.future.apix.response.MethodArgumentInvalidResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
//https://www.mkyong.com/spring-boot/spring-rest-validation-example/
public class RestExceptionControllerAdvice extends ResponseEntityExceptionHandler {


    // to handle error in @Valid from entity validation (@NotBlank)
    @Override
    protected ResponseEntity handleMethodArgumentNotValid
    (MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream().map(x -> x.getDefaultMessage()).collect(Collectors.toList());

        MethodArgumentInvalidResponse response = new MethodArgumentInvalidResponse();
        response.setStatusToFailed();
        response.setMessage("Validation errors");
        response.buildErrors(errors);

        return new ResponseEntity(response, headers, HttpStatus.BAD_REQUEST);
    }
}
