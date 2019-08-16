package com.future.apix.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.User;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.DuplicateEntryException;
import com.future.apix.exception.InvalidAuthenticationException;
import com.future.apix.exception.InvalidRequestException;
import com.future.apix.repository.UserRepository;
import com.future.apix.request.UserCreateRequest;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.UserCreateResponse;
import com.future.apix.response.UserProfileResponse;
import com.future.apix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper oMapper;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserProfileResponse userProfile (Authentication authentication) {
        UserProfileResponse response;
        if (authentication != null) {
            response = oMapper.convertValue(authentication.getPrincipal(), UserProfileResponse.class);
            response.setStatusToSuccess();
            response.setMessage("User is authenticated!");
            return response;
        }
        else throw new InvalidAuthenticationException("User is not authenticated!");
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public UserCreateResponse createUser(UserCreateRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("Password does not match!");
        }
        if (request.getUsername().length() < 4) {
            throw new InvalidRequestException("Username is too short!");
        }
        User exist = userRepository.findByUsername(request.getUsername());
        if (exist == null) {
            User newUser = new User();
            newUser.setUsername(request.getUsername());
            newUser.setPassword(passwordEncoder.encode(request.getPassword()));
            newUser.setRoles(request.getRoles());
            newUser = userRepository.save(newUser);

            UserCreateResponse response = new UserCreateResponse();
            response.setStatusToSuccess();
            response.setMessage("User is created!");
            response.setUserId(newUser.getId());
            return response;
        } else {
            throw new DuplicateEntryException("Username is already exists!");
        }
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public RequestResponse deleteUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("User does not exists!"));
        userRepository.deleteById(id);
        return RequestResponse.success("User has been deleted!");
    }

    @Override
    public User getUser(String username) {
        User user = Optional.ofNullable(userRepository.findByUsername(username))
                .orElseThrow(() -> new DataNotFoundException("User is not registered!"));
        return user;
    }

}
