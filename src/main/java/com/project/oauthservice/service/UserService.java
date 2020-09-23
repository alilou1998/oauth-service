package com.project.oauthservice.service;

import com.project.oauthservice.dto.LoginRequest;
import com.project.oauthservice.dto.LoginResponse;
import com.project.oauthservice.dto.LogoutResponse;
import com.project.oauthservice.dto.UserSummary;
import com.project.oauthservice.model.User;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface UserService {

    ResponseEntity<LoginResponse> login( LoginRequest loginRequest, String accessToken, String refreshToken);

    ResponseEntity<LogoutResponse> logout(HttpServletRequest request, HttpServletResponse response);

    ResponseEntity<LoginResponse> refresh(String accessToken,String refreshToken);

    UserSummary getUserProfile();

    boolean isFound(String email);

    ResponseEntity<User> register(User user);

}
