package com.eaglebank.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
  private final SecretKey secretKey;
  private final long expirationMs;

  public JwtUtil(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.expiration}") long expirationMs) {
    this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
    this.expirationMs = expirationMs;
  }

  public String generateToken(String subject) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .subject(subject)
        .issuedAt(new Date(now))
        .expiration(new Date(now + expirationMs))
        .signWith(secretKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public String validateAndGetSubject(String token) {
    return Jwts.parser().verifyWith(secretKey).build()
      .parseSignedClaims(token).getPayload().getSubject();
  }
}
