package com.future.apix.service;

import com.future.apix.entity.User;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.UserProfileResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UserService {
    UserProfileResponse userProfile (Authentication authentication);
    RequestResponse checkUserTeams(String username, List<String> teams); // untuk mengecek apakah teams dalam Project ada dalam User

    // Create Array of Users
    RequestResponse createUser(User user);
    List<User> getUsers();
    RequestResponse deleteUser(String id);

}
