package com.future.apix.controller;

import com.future.apix.config.jwt.JwtTokenProvider;
import com.future.apix.entity.User;
import com.future.apix.repository.UserRepository;
import com.future.apix.request.AuthenticationRequest;
import com.future.apix.response.AuthLoginResponse;
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

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserRepository userRepository;

    @PostMapping("/login")
    public AuthLoginResponse login(@RequestBody AuthenticationRequest data) {
        try {
            String username = data.getUsername();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, data.getPassword()));

            User user = userRepository.findByUsername(username);
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
