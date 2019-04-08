package com.future.apix.auth;

import com.future.apix.exception.InvalidJwtAuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

//https://o7planning.org/en/11649/secure-spring-boot-restful-service-using-basic-authentication
// https://www.devglan.com/spring-security/spring-boot-security-rest-basic-authentication
@Component
    public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(RestAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException e) throws IOException, ServletException {
        /*
        response.addHeader("WWW-Authenticate", "Basic realm=" + getRealmName());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        PrintWriter writer = response.getWriter();
        writer.println("HTTP Status 401 - " + e.getMessage());
        */
//        http://kelvinleong.github.io/authentication/2018/06/06/JWT-Authentication.html
        logger.error("Responding with unauthorized error. Message - ", e.getMessage());
        System.out.println(request);
        System.out.println(response);
        if (e instanceof InvalidJwtAuthenticationException) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token - " + e.getMessage());
        }
        else {
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Http Status 401 Unauthorized - " + e.getMessage());
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
