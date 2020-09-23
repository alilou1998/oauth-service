package com.project.oauthservice.service;

import com.project.oauthservice.dto.*;
import com.project.oauthservice.model.User;
import com.project.oauthservice.repository.UserRepository;
import com.project.oauthservice.util.CookieUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Service
public class UserServiceImpl implements UserService {

    Logger logger = LoggerFactory.getLogger(this.getClass());


    private final UserRepository userRepository;

    private final TokenProvider tokenProvider;

    private final CookieUtil cookieUtil;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, TokenProvider tokenProvider, CookieUtil cookieUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.cookieUtil = cookieUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public ResponseEntity<LoginResponse> login( LoginRequest loginRequest, String accessToken, String refreshToken) {
        String email = loginRequest.getEmail();
        User user = userRepository.findByEmail(email).orElseThrow(()->new IllegalArgumentException("User not found with email "+ email));

        Boolean accessTokenValid = tokenProvider.validateToken(accessToken);
        Boolean refreshTokenValid = tokenProvider.validateToken(refreshToken);

        HttpHeaders responseHeaders = new HttpHeaders();
        Token newAccessToken;
        Token newRefreshToken;

        if(!accessTokenValid && !refreshTokenValid){
            newAccessToken = tokenProvider.generateAccessToken(user.getEmail());
            newRefreshToken = tokenProvider.generateRefreshToken(user.getEmail());
            addAccessTokenCookie(responseHeaders,newAccessToken);
            addRefreshTokenCookie(responseHeaders,newRefreshToken);
        }if(!accessTokenValid && refreshTokenValid){
            newAccessToken = tokenProvider.generateAccessToken(user.getEmail());
            addAccessTokenCookie(responseHeaders,newAccessToken);
        }if(accessTokenValid && refreshTokenValid){
            newAccessToken = tokenProvider.generateAccessToken(user.getEmail());
            newRefreshToken = tokenProvider.generateRefreshToken(user.getEmail());
            addAccessTokenCookie(responseHeaders,newAccessToken);
            addRefreshTokenCookie(responseHeaders,newRefreshToken);
        }

        responseHeaders.add(HttpHeaders.SET_COOKIE,"logged_in=true;Path=/");
        LoginResponse loginResponse = new LoginResponse(LoginResponse.SuccessFailure.SUCCESS,"Authentication successful, Tokens in Cookies");
        return ResponseEntity.ok().headers(responseHeaders).body(loginResponse);
    }

    @Override
    public ResponseEntity<LogoutResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        SecurityContextHolder.clearContext();
        session = request.getSession(false);
        if(session!=null){
            session.invalidate();
        }
        if(request.getCookies()!=null){
            for(Cookie cookie:request.getCookies()){
                cookie.setMaxAge(0);
                cookie.setValue("");
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        }
        return ResponseEntity.ok().body(new LogoutResponse("True","The user has been logged out"));
    }

    @Override
    public ResponseEntity<LoginResponse> refresh(String accessToken, String refreshToken) {
        boolean refreshTokenValid = tokenProvider.validateToken(refreshToken);
        if(!refreshTokenValid){
            throw new IllegalArgumentException("Refresh Token is invalid");
        }
        String currentUserEmail = tokenProvider.getUsernameFromToken(accessToken);
        Token newAccessToken = tokenProvider.generateAccessToken(currentUserEmail);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(HttpHeaders.SET_COOKIE,cookieUtil.createAccessTokenCookie(newAccessToken.getTokenValue(),newAccessToken.getDuration()).toString());
        LoginResponse loginResponse = new LoginResponse(LoginResponse.SuccessFailure.SUCCESS,"Authentication successful, Tokens in Cookies");
        return ResponseEntity.ok().headers(responseHeaders).body(loginResponse);
    }

    @Override
    public UserSummary getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails customeUserDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(customeUserDetails.getUsername()).orElseThrow(()->new IllegalArgumentException("User not found with email -> "+ customeUserDetails.getUsername()));
        return user.toUserSummary();
    }

    @Override
    public boolean isFound(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    @Override
    public ResponseEntity<User> register(User user) {
        User newUser = new User();
        newUser.setFirstname(user.getFirstname());
        newUser.setLastname(user.getLastname());
        newUser.setEmail(user.getEmail());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setBirth(user.getBirth());
        newUser.setCountry(user.getCountry());
        newUser.setEnabled(user.isEnabled());
        newUser.setSex(user.getSex());
        newUser.setUsername(user.getUsername());
        return ResponseEntity.ok(userRepository.save(newUser));
    }

    private void addAccessTokenCookie(HttpHeaders httpHeaders, Token token) {
        httpHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.createAccessTokenCookie(token.getTokenValue(), token.getDuration()).toString());
    }

    private void addRefreshTokenCookie(HttpHeaders httpHeaders, Token token) {
        httpHeaders.add(HttpHeaders.SET_COOKIE, cookieUtil.createRefreshTokenCookie(token.getTokenValue(), token.getDuration()).toString());
    }

}
