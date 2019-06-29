package com.future.apix.service.bean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.util.converter.ApiProjectConverter;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class BeanInitiator {

    @Value("${apix.github.token}")
    private String token;

    @Bean
    ObjectMapper getObjectMapper(){
        return new ObjectMapper();
    }

    @Bean
    ApiProjectConverter getApiProjectConverter(){
        return new ApiProjectConverter();
    }


    @Bean
    public GitHub authToken() throws IOException {
        return GitHub.connectUsingOAuth(token);
    }
}
