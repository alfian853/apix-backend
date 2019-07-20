package com.future.apix.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.util.LazyObjectWrapper;
import com.future.apix.util.ObjectInitiator;
import com.future.apix.util.converter.ApiProjectConverter;
import com.future.apix.util.converter.SwaggerToApixOasConverter;
import com.future.apix.util.jsonquery.JsonQueryExecutor;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class BeanConfig {

    @Value("${apix.github.token}")
    private String token;

    @Bean
    ObjectMapper objectMapper(){
        return new ObjectMapper().configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
    }

    @Bean
    ApiProjectConverter apiProjectConverter(){
        return new ApiProjectConverter();
    }

    @Bean
    SwaggerToApixOasConverter swaggerToApixOasConverter() {return new SwaggerToApixOasConverter(); }

    @Bean
    JsonQueryExecutor jsonQueryExecutor(){return new JsonQueryExecutor();}

    @Bean
    public LazyObjectWrapper<GitHub> LazyGitHubObjectWrapper() throws IOException {

        return new LazyObjectWrapper<>(new ObjectInitiator<GitHub>() {
            @Override
            public GitHub initObject() {
                GitHub git = null;
                try {
                    git = GitHub.connectUsingOAuth(token);
                } finally {
                    System.out.println(git);
                    return git;
                }
            }

            @Override
            public void onInitFailed() {
                throw new RuntimeException("Apix github service unavaliable");
            }
        });
    }
}
