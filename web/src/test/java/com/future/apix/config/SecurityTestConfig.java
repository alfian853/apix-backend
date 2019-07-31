package com.future.apix.config;

import com.future.apix.entity.User;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.Arrays;
import java.util.Collections;

//https://eliux.github.io/java/spring/testing/how-to-mock-authentication-in-spring/
//https://www.concretepage.com/spring-4/spring-4-security-junit-test-with-withmockuser-and-withuserdetails-annotation-example-using-webappconfiguration
@TestConfiguration
public class SecurityTestConfig {

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        User admin = new User("id", "admin", "password",
                Arrays.asList("ROLE_ADMIN"), Collections.emptyList(), Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN")));

        return new InMemoryUserDetailsManager(Arrays.asList(admin));
    }
}
