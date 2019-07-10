package com.future.apix.service.bean;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.util.converter.ApiProjectConverter;
import com.future.apix.util.converter.SwaggerToApixOasConverter;
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
        return new ObjectMapper().configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    @Bean
    ApiProjectConverter getApiProjectConverter(){
        return new ApiProjectConverter();
    }

    @Bean
    SwaggerToApixOasConverter getSwaggerToApixOasConverter() {return new SwaggerToApixOasConverter(); }

    @Bean
    public GitHub authToken() throws IOException {
        GitHub git = null;
        try{
            git = GitHub.connectUsingOAuth(token);
        }
        finally {
            return git;
        }
    }
}
