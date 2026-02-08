package com.example.UnityTrustBank.security;

import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
@Component
public class JwtUtil {

    private static final String SECRET =
        "dXRiX3NlY3VyZV9qd3Rfa2V5XzIwMjVfYmFzZTY0X3NhZmU=";

    private static final long EXPIRY = 1000 * 60 * 60 * 24; // 24 hours

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(SECRET)
        );
    }

    public String generateToken(Long userId, String email, String role) {

        return Jwts.builder()
                .setSubject(email)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(
                    new Date(System.currentTimeMillis() + EXPIRY)
                )
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractClaims(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public boolean validate(String token) {

        try {
            extractClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
