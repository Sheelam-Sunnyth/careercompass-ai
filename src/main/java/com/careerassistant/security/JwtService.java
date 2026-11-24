package com.careerassistant.security;

import com.careerassistant.config.AppProperties;
import com.careerassistant.user.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {
    private final AppProperties properties;
    public JwtService(AppProperties properties) { this.properties = properties; }
    public String issue(UserAccount user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.getJwt().getExpirationMinutes(), ChronoUnit.MINUTES)))
                .signWith(key())
                .compact();
    }
    public Long subject(String token) {
        Claims claims = Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
        return Long.parseLong(claims.getSubject());
    }
    private SecretKey key() {
        String secret = properties.getJwt().getSecret();
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters.");
        }
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
