package com.future.apix.config;

import com.future.apix.auth.JwtConfigurer;
import com.future.apix.auth.JwtTokenProvider;
import com.future.apix.auth.RestAuthenticationEntryPoint;
import com.future.apix.service.impl.MongoUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

//https://www.codementor.io/gtommee97/rest-authentication-with-spring-security-and-mongodb-j8wgh8kg7
//https://www.baeldung.com/securing-a-restful-web-service-with-spring-security

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    MongoUserDetailsService userDetailsService;

    @Autowired
    private RestAuthenticationEntryPoint authEntryPoint;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("admin").password(passwordEncoder().encode("admin")).roles("ADMIN")
                .and()
                .withUser("user").password(passwordEncoder().encode("user")).roles("USER");

        auth.userDetailsService(userDetailsService);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        https://www.codementor.io/gtommee97/rest-authentication-with-spring-security-and-mongodb-j8wgh8kg7
        http
                .csrf().disable()
                .httpBasic().disable()
//                .and().httpBasic().authenticationEntryPoint(authEntryPoint)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                    .antMatchers("/auth/login").permitAll()
                    .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                    .authenticationEntryPoint(authEntryPoint)
                .and()
                    .apply(new JwtConfigurer(jwtTokenProvider))
                ;


    }

}
