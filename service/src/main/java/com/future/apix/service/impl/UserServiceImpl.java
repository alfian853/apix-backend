package com.future.apix.service.impl;

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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
            response.setMessage("User is authenticated");
            return response;
        }
        else throw new InvalidAuthenticationException("User is not authenticated!");

    }

    @Override
    public RequestResponse checkUserTeams(String username, List<String> teams) {
        User user = userRepository.findByUsernameAndTeamsIn(username, teams);
        if (user != null) return RequestResponse.success("User is belonged to team");
        return RequestResponse.failed("User is not belonged to team!");
    }

    @Override
    public RequestResponse createUser(User user) {

        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setTeams(user.getTeams());
        newUser.setRoles(user.getRoles());
        userRepository.save(newUser);

        return RequestResponse.success("User is created!");
    }

    @Override
    public List<User> getUsers() {
        return userRepository.findAll();
    }

    @Override
    public RequestResponse deleteUser(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new DataNotFoundException("User does not exists!"));
        userRepository.deleteById(id);
        return RequestResponse.success("User has been deleted!");
    }

    @Override
    public List<UserProfileResponse> getUsersByTeam(String team) {
        List<User> users = userRepository.findByTeams(team);
        List<UserProfileResponse> profileResponses = new ArrayList<>();
        for (User user: users) {
            UserProfileResponse response = new UserProfileResponse();
            response = oMapper.convertValue(user, UserProfileResponse.class);
            profileResponses.add(response);
            response.setStatusToSuccess();
        }
        return profileResponses;
    }
}
