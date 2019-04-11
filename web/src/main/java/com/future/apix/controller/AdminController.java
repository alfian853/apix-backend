package com.future.apix.controller;

import com.future.apix.entity.User;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
    @ResponseStatus(HttpStatus.CREATED)
    public RequestResponse createUsers(@RequestBody List<User> userList) {
        return userService.createUsers(userList);
    }

    @DeleteMapping("/users/{id}")
    public RequestResponse deleteUser(@PathVariable String id) {
        return userService.deleteUser(id);
    }



}
