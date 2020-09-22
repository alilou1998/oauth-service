package com.project.oauthservice.service;

import com.project.oauthservice.dto.Token;

import java.time.LocalDateTime;

public interface TokenProvider {

    Token generateAccessToken(String subject);

    Token generateRefreshToken(String subject);

    String getUsernameFromToken(String token);

    LocalDateTime getExpiryDateFromToken(String token);

    Boolean validateToken(String token);

}
