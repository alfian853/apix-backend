package com.future.apix.controller;

import com.future.apix.entity.User;
import com.future.apix.request.UserCreateRequest;
import com.future.apix.response.RequestResponse;
import com.future.apix.response.UserCreateResponse;
import com.future.apix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    UserService userService;

    @GetMapping("/users")
    public  List<User> getUsers(){
        return userService.getUsers();
    }

    @PostMapping("/users")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UserCreateResponse createUser(@Valid @RequestBody UserCreateRequest request) {
        return userService.createUser(request);
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public RequestResponse deleteUser(@PathVariable String id) {
        return userService.deleteUser(id);
    }

}
