package com.future.apix.service.impl;

import com.future.apix.entity.User;
import com.future.apix.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

//https://www.codementor.io/gtommee97/rest-authentication-with-spring-security-and-mongodb-j8wgh8kg7

@Component
public class MongoUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        https://confluence.gdn-app.com/display/GDNIT/%5BBackend%5D+Use+java.util.Optional
        return Optional.ofNullable(userRepository.findByUsername(username))
            .orElseThrow(() -> new UsernameNotFoundException("Username: " + username + " not found!"));
    }
}
