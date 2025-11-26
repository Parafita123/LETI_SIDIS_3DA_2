package com.LETI_SIDIS_3DA2.identity_service.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expirationMs;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    // 👉 método chamado pelo AuthController
    public String generateToken(UserDetails userDetails) {
        // converte authorities do Spring (ex.: ROLE_PATIENT) para ["PATIENT"]
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(a -> a.getAuthority().startsWith("ROLE_")
                        ? a.getAuthority().substring(5)
                        : a.getAuthority())
                .collect(Collectors.toList());

        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("authorities", authorities)  // claim essencial
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
