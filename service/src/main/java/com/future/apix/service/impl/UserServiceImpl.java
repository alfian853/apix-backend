package com.future.apix.service.impl;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.future.apix.entity.User;
import com.future.apix.exception.DataNotFoundException;
import com.future.apix.exception.InvalidAuthenticationException;
import com.future.apix.repository.UserRepository;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.UserProfileResponse;
import com.future.apix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper oMapper;

    @Override
    public UserProfileResponse userProfile (Authentication authentication) {
        UserProfileResponse response = new UserProfileResponse();
        if (authentication != null) {

            response = oMapper.convertValue(authentication.getPrincipal(), UserProfileResponse.class);
            response.setStatusToSuccess();
            response.setMessage("User is authenticated");
            return response;
        }
        else throw new InvalidAuthenticationException("User is not authenticated!");

    }

    @Override
    public RequestResponse checkUserTeams(String username, List<String> teams) {
        User user = userRepository.findByUsernameAndTeamsIn(username, teams);
        RequestResponse response = new RequestResponse();
        if (user != null) return response.success("User is belonged to team");
        return response.failed("User is not belonged to team!");
    }

    @Override
    public RequestResponse createUsers(List<User> users) {
        userRepository.saveAll(users);

        RequestResponse response = new RequestResponse();
        return response.success("User(s) are created!");
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public RequestResponse deleteUser(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new DataNotFoundException("User does not exists!"));
        userRepository.deleteById(id);
        return new RequestResponse().success("User has been deleted!");
    }
}
