package com.example.chatify.chat.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger log= LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret.key}")
    private String jwtSecret;

    private SecretKey key;

    @PostConstruct
    public void init()
    {
        this.key= Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        log.info("JWT secret intialized");
    }

    //short period access tokens
    public String generateAccessToken(String username)
    {
        log.info("Generating JWT token for username {}", username);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+15*60*1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // long period access token
    public String generateRefreshToken(String username)
    {
        log.info("Generating refresh JWT token for username {}", username);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+7*24*60*60*1000))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

    }
    public String getUsernameFromToken(String token){
        String username=Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token).getBody().getSubject();
        log.info("JWT username from JWT token is {}", username);
        return username;
    }

    public boolean validateToken(String token){
        try{
            Jwts.parser().setSigningKey(key).build().parseClaimsJws(token);
            log.info("JWT validated successfully");
            return true;
        }
        catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

}
