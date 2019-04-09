package com.future.apix.auth;

import com.future.apix.exception.InvalidJwtAuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//https://o7planning.org/en/11649/secure-spring-boot-restful-service-using-basic-authentication
// https://www.devglan.com/spring-security/spring-boot-security-rest-basic-authentication
@Component
    public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException e) throws IOException {

        response.addHeader("Access-Control-Allow-Origin","*");
        response.addHeader("Access-Control-Allow-Methods"," GET, POST, PUT, DELETE, OPTIONS");
        response.addHeader(
                "Access-Control-Allow-Headers",
                "access-control-allow-headers,access-control-allow-methods,access-control-allow-origin,authorization"
        );
        if (e instanceof InvalidJwtAuthenticationException) {
            logger.error("Responding with unauthorized error. Message - ", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token - " + e.getMessage());
        }

    }

    /* hanya digunakan jika extends BasicAuthenticationEntryPoint
    @Override
    public void afterPropertiesSet() throws Exception {
        // Cek ini buat apa??
        setRealmName("APIX");
        super.afterPropertiesSet();
    }
    */
}
