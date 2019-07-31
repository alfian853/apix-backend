package com.future.apix.controller;

import com.future.apix.response.RequestResponse;
import com.future.apix.response.UserProfileResponse;
import com.future.apix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public UserProfileResponse getAuth(Authentication authentication) {
        return userService.userProfile(authentication);
    }

}
