package com.dat.backend.movied.auth.serivce.impl;

import com.dat.backend.movied.auth.serivce.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.secret-key}")
    private String SECRET_KEY;
    @Value("${jwt.expiredAccess}")
    private String EXPIRED_ACCESS_TOKEN;
    @Value("${jwt.expiredRefresh}")
    private String EXPIRED_REFRESH_TOKEN;

    public String generateAccessToken(String email) {
        return generateAccessToken(new HashMap<>(), email);
    }

    public String generateRefreshToken(String email) {
        return generateRefreshToken(new HashMap<>(), email);
    }

    private String generateAccessToken(Map<String, Object> claims, String email){
        long expiredAccess = Long.parseLong(EXPIRED_ACCESS_TOKEN.substring(0, 1));
        return Jwts.builder()
                .setClaims(claims)
                .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ expiredAccess*60*60*1000))
                .setSubject(email)
                .compact();
    }

    private String generateRefreshToken(Map<String, Object> claims, String email){
        long expiredRefresh = Long.parseLong(EXPIRED_REFRESH_TOKEN.substring(0, 1));
        return Jwts.builder()
                .setClaims(claims)
                .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+ expiredRefresh*24*60*60*1000))
                .setSubject(email)
                .compact();
    }

    private SecretKey getSigninKey() {
        byte[] key = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(key);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claim){
        Claims claims = extractAllClaims(token);
        return claim.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigninKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiredTime(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        try{
            String username = extractUsername(token);
            //Return true if username in token is equal to username in userDetails and token is not expired and token is not in blacklist
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token) ;
        } catch (Exception e){
            throw new RuntimeException("Invalid token", e);
        }
    }

    public boolean isTokenExpired(String token) {
        return extractExpiredTime(token).before(new Date(System.currentTimeMillis()));
    }
}
