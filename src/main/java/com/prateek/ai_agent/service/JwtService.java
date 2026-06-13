package com.prateek.ai_agent.service;

import com.prateek.ai_agent.entity.TokenType;
import com.prateek.ai_agent.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secretKey}")
    private String jwtSecretKey;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(jwtSecretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .subject(user.getId())
                .claim("roles", user.getRole().name())
                .claim("email", user.getEmail())
                .claim("type", TokenType.ACCESS.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60*10))
                .signWith(getSecretKey())
                .compact();
    }
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .subject(user.getId())
                .claim("type", TokenType.REFRESH.name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60*60*24*30))//if changing this then change in sessionService generate new session too.
                .signWith(getSecretKey())
                .compact();
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    private TokenType getTokenType(Claims claims) {
        try {
            String type = (String) claims.get("type");
            if (type == null) {
                throw new JwtException("Missing token type");
            }
            return TokenType.valueOf(type);
        } catch (Exception e) {
            throw new JwtException("Invalid token type");
        }
    }
    public String getUserIdFromAccessToken(String token) {

        Claims claims = parseToken(token);
        if (getTokenType(claims) != TokenType.ACCESS) {
            throw new JwtException("Invalid token type for API access");
        }
        return claims.getSubject();
    }
    public String getUserIdFromRefreshToken(String token) {

        Claims claims = parseToken(token);
        if (getTokenType(claims) != TokenType.REFRESH) {
            throw new JwtException("Invalid token type for refresh");
        }
        return claims.getSubject();
    }



//    public String getUserIdFromToken(String token) {
//        Claims claims = Jwts.parser()
//                .verifyWith(getSecretKey())
//                .build()
//                .parseSignedClaims(token)
//                .getPayload();
//        return (claims.getSubject()); //this will give userId bcoz we passed userID above
//    }
}