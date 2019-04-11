package com.future.apix.controller;

import com.future.apix.request.TeamInUserRequest;
import com.future.apix.response.RequestResponse;
import com.future.apix.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity currentUser(@AuthenticationPrincipal UserDetails userDetails) {
        Map<Object, Object> model = new HashMap<>();


        model.put("username", userDetails.getUsername());
        model.put("roles", userDetails.getAuthorities()
                .stream()
                .map(a -> ((GrantedAuthority) a).getAuthority())
                .collect(toList())
        );

        return ResponseEntity.ok(model);
    }

    @GetMapping("/principal") // Retrieve all user data in class User (in ['principal'])
    public ResponseEntity getPrincipal(Principal user){
        return ResponseEntity.ok(user);
    }

    @GetMapping("/teamsIn")
    public RequestResponse isTeamIn( Principal user, @RequestBody TeamInUserRequest userTeam) {
        return userService.checkUserTeams(user.getName(), userTeam.getTeams());
    }


}
