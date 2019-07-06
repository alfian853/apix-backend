package com.future.apix.controller;

import com.future.apix.config.jwt.JwtTokenProvider;
import com.future.apix.entity.User;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidAuthenticationException;
import com.future.apix.repository.UserRepository;
import com.future.apix.request.AuthenticationRequest;
import com.future.apix.response.AuthLoginResponse;
import com.future.apix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserService userService;

    @PostMapping("/login")
    public AuthLoginResponse login(@RequestBody AuthenticationRequest data) {
        User user = userService.getUser(data.getUsername());

        try {
            String username = user.getUsername();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, data.getPassword()));

            String token = jwtTokenProvider.createToken(username, user.getRoles());

            AuthLoginResponse response = new AuthLoginResponse();
            response.setUsername(username);
            response.setToken(token);
            response.setSuccess(true);
            response.setMessage("User is authenticated!");

            return response;
        } catch (AuthenticationException e) {
            throw new BadCredentialsException ("Invalid username or password!");
        }
    }
}
