package com.project.oauthservice.controller;

import com.project.oauthservice.dto.UserDetails;
import com.project.oauthservice.dto.UserSummary;
import com.project.oauthservice.model.User;
import com.project.oauthservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/profile")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserSummary> me(){
        return ResponseEntity.ok(userService.getUserProfile());
    }


    @GetMapping("/{id}")
    public ResponseEntity<UserDetails> user(@PathVariable("id") long userid){
        return userService.getUserDetails(userid);
    }


}
