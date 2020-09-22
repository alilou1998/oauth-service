package com.project.oauthservice.controller;

import com.project.oauthservice.dto.LoginRequest;
import com.project.oauthservice.dto.LoginResponse;
import com.project.oauthservice.model.User;
import com.project.oauthservice.service.UserService;
import com.project.oauthservice.util.SecurityCipher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @PostMapping(value="/login" , consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> login(
            @CookieValue(name = "accessToken",required = false) String accessToken,
            @CookieValue(name = "refreshToken",required = false) String refreshToken,
            @Valid @RequestBody LoginRequest request){

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(),request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String decryptedAccessToken = SecurityCipher.decrypt(accessToken);
        String decryotedRefreshToken = SecurityCipher.decrypt(refreshToken);
        return userService.login(request,decryptedAccessToken,decryotedRefreshToken);
    }

    @PostMapping(value="/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponse> refresh(@CookieValue(name = "accessToken",required = false) String accessToken,
                                                 @CookieValue(name = "refreshToken",required = false) String refreshToken){

        String decryptedAccessToken = SecurityCipher.decrypt(accessToken);
        String decryotedRefreshToken = SecurityCipher.decrypt(refreshToken);
        return userService.refresh(decryptedAccessToken,decryotedRefreshToken);
    }


    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user){
        if(!userService.isFound(user.getEmail())){
            return userService.register(user);
        }
        return ResponseEntity.unprocessableEntity().body(user);
    }


}
