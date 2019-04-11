package com.future.apix.service;

import com.future.apix.entity.User;
import com.future.apix.response.RequestResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UserService {
    User userProfile (UserDetails user);
    RequestResponse checkUserTeams(String username, List<String> teams); // untuk mengecek apakah teams dalam Project ada dalam User

    // Create Array of Users
    RequestResponse createUsers(List<User> users);
    List<User> getUsers();
    RequestResponse deleteUser(String id);

}
