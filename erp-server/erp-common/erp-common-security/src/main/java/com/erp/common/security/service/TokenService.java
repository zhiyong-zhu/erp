package com.erp.common.security.service;

import com.erp.common.security.domain.LoginUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private static final String ACCESS_TOKEN_PREFIX = "erp:security:access:";
    private static final String REFRESH_TOKEN_PREFIX = "erp:security:refresh:";

    private final SecretKey key;
    private final long accessTokenExpireSeconds;
    private final long refreshTokenExpireSeconds;
    private final StringRedisTemplate stringRedisTemplate;

    public TokenService(
            @Value("${erp.security.jwt-secret:erp-dev-secret-key-please-change-123456}") String secret,
            @Value("${erp.security.access-token-expire-seconds:7200}") long accessTokenExpireSeconds,
            @Value("${erp.security.refresh-token-expire-seconds:604800}") long refreshTokenExpireSeconds,
            StringRedisTemplate stringRedisTemplate) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpireSeconds = accessTokenExpireSeconds;
        this.refreshTokenExpireSeconds = refreshTokenExpireSeconds;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public String createAccessToken(LoginUser loginUser) {
        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", loginUser.getUserId());
        claims.put("roles", loginUser.getRoles());
        claims.put("permissions", loginUser.getPermissions());
        if (loginUser.getRealName() != null) {
            claims.put("realName", loginUser.getRealName());
        }
        String token = Jwts.builder()
                .subject(loginUser.getUsername())
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenExpireSeconds)))
                .signWith(key)
                .compact();
        stringRedisTemplate.opsForValue().set(accessTokenKey(token), loginUser.getUserId().toString(), Duration.ofSeconds(accessTokenExpireSeconds));
        return token;
    }

    public String createRefreshToken(LoginUser loginUser) {
        Instant now = Instant.now();
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", loginUser.getUserId());
        claims.put("type", "refresh");
        claims.put("roles", loginUser.getRoles());
        claims.put("permissions", loginUser.getPermissions());
        String token = Jwts.builder()
                .subject(loginUser.getUsername())
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTokenExpireSeconds)))
                .signWith(key)
                .compact();
        stringRedisTemplate.opsForValue().set(refreshTokenKey(token), loginUser.getUserId().toString(), Duration.ofSeconds(refreshTokenExpireSeconds));
        return token;
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

    public boolean isAccessTokenActive(String token) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(accessTokenKey(token)));
    }

    public boolean isRefreshTokenActive(String token) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(refreshTokenKey(token)));
    }

    public void revokeAccessToken(String token) {
        stringRedisTemplate.delete(accessTokenKey(token));
    }

    public void revokeRefreshToken(String token) {
        stringRedisTemplate.delete(refreshTokenKey(token));
    }

    public void revokeUserTokens(String accessToken, String refreshToken) {
        if (accessToken != null && !accessToken.isBlank()) {
            revokeAccessToken(accessToken);
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            revokeRefreshToken(refreshToken);
        }
    }

    private String accessTokenKey(String token) {
        return ACCESS_TOKEN_PREFIX + token;
    }

    private String refreshTokenKey(String token) {
        return REFRESH_TOKEN_PREFIX + token;
    }
}
