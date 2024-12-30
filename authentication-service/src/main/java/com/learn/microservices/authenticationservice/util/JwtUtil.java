package com.learn.microservices.authenticationservice.util;

import com.learn.microservices.authenticationservice.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {


    private final SecretKey secretKey;

    private final Long expirationTime;

    public JwtUtil(@Value("${token.secret-key}") String secretStr,
                   @Value("${token.expiration-time}") Long expirationTime) {
        this.secretKey = Keys.hmacShaKeyFor(secretStr.getBytes());
        this.expirationTime = expirationTime;
    }

    public String generateToken(User userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getRole());
        return createToken(claims, userDetails.getEmail());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey).compact();
    }
}
