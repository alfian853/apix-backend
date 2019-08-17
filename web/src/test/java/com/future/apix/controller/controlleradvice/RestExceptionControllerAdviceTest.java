package com.future.apix.controller.controlleradvice;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RestExceptionControllerAdviceTest {
  @Mock
  private MethodArgumentNotValidException mockedException;

  @InjectMocks
  private RestExceptionControllerAdvice advice;

  @Mock
  private BindingResult result;

//  https://github.com/ogstation/member/blob/master/src/test/java/com/github/ogstation/member/controller/RestExceptionHandlerAdviceControllerTest.java
//  https://github.com/rprobinson/MediPi/blob/master/Application/Common-Components/devops-commons/src/test/java/com/dev/ops/common/service/exception/ServiceExceptionControllerAdviceTest.java
  @Test
  public void handle_MethodArgumentNotValidException() throws Exception {
    when(result.getFieldErrors()).thenReturn(Arrays.asList(new FieldError("team", "name", "team name must not be empty")));
    when(mockedException.getBindingResult()).thenReturn(result);

    ResponseEntity result = advice.handleMethodArgumentNotValid(mockedException,
        new HttpHeaders(),
        HttpStatus.BAD_REQUEST,
        null);
    Assert.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    Assert.assertEquals(400, result.getStatusCodeValue());
    Assert.assertNotNull(result);
  }
}
