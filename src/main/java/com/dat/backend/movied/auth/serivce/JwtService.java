package com.dat.backend.movied.auth.serivce;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

public interface JwtService {
    String generateAccessToken(String email);
    String generateRefreshToken(String email);
    String extractUsername(String token);
    boolean isTokenValid(String token, UserDetails userDetails);
    Date extractExpiredTime(String token);
    boolean isTokenExpired(String token);
}
