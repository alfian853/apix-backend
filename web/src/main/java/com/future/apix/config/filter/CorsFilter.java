package com.future.apix.config.filter;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CorsFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin","*");
        response.setHeader("Access-Control-Allow-Methods","GET,POST,PUT,DELETE,HEAD,PATCH,OPTIONS");
        response.setHeader(
                "Access-Control-Allow-Headers",
                "access-control-allow-headers," +
                        "Access-Control-Allow-Methods," +
                        "Access-Control-Allow-Origin," +
                        "content-type," +
                        "authorization,"+
                        "accept"
        );
        if("OPTIONS".equals(request.getMethod())){
            response.setStatus(HttpServletResponse.SC_OK);
        }
        else{
            filterChain.doFilter(request, response);
        }

    }
}
