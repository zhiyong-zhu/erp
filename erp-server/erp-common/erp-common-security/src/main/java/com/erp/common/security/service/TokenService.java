package com.erp.common.security.service;

import com.erp.common.security.domain.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private final SecretKey key;
    private final long accessTokenExpireSeconds;
    private final long refreshTokenExpireSeconds;

    public TokenService(
            @Value("${erp.security.jwt-secret:erp-dev-secret-key-please-change-123456}") String secret,
            @Value("${erp.security.access-token-expire-seconds:7200}") long accessTokenExpireSeconds,
            @Value("${erp.security.refresh-token-expire-seconds:604800}") long refreshTokenExpireSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpireSeconds = accessTokenExpireSeconds;
        this.refreshTokenExpireSeconds = refreshTokenExpireSeconds;
    }

    public String createAccessToken(LoginUser loginUser) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(loginUser.getUsername())
                .claims(Map.of(
                        "userId", loginUser.getUserId(),
                        "realName", loginUser.getRealName()))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenExpireSeconds)))
                .signWith(key)
                .compact();
    }

    public String createRefreshToken(LoginUser loginUser) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(loginUser.getUsername())
                .claims(Map.of("userId", loginUser.getUserId(), "type", "refresh"))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTokenExpireSeconds)))
                .signWith(key)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @SuppressWarnings("unchecked")
    public List<String> readRoles(Claims claims) {
        Object roles = claims.get("roles");
        return roles instanceof List<?> list ? (List<String>) list : List.of();
    }

    @SuppressWarnings("unchecked")
    public List<String> readPermissions(Claims claims) {
        Object permissions = claims.get("permissions");
        return permissions instanceof List<?> list ? (List<String>) list : List.of();
    }

    public long getAccessTokenExpireSeconds() {
        return accessTokenExpireSeconds;
    }
}
