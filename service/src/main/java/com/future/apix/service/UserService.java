package com.future.apix.service;

import com.future.apix.entity.User;
import com.future.apix.request.AuthenticationRequest;
import com.future.apix.request.UserCreateRequest;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.UserCreateResponse;
import com.future.apix.response.UserProfileResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface UserService {
    UserProfileResponse userProfile (Authentication authentication);

    /* For Admin -> User Management */
    UserCreateResponse createUser(UserCreateRequest request); // Create 1 user (with @Valid)
    List<User> getUsers();
    RequestResponse deleteUser(String id);

    /* For Auth */
    User getUser(String username);

    /* For User -> Team Management */
//    List<UserProfileResponse> getUsersByTeam(String team);

}
