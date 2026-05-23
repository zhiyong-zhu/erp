package com.erp.common.security.filter;

import com.erp.common.security.domain.LoginUser;
import com.erp.common.security.service.TokenService;
import com.erp.common.security.util.SecurityUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final TokenService tokenService;

    public JwtAuthenticationFilter(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Bearer ")) {
            String token = authorization.substring(7);
            try {
                Claims claims = tokenService.parseToken(token);
                if ("refresh".equals(claims.get("type", String.class))) {
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
                if (!tokenService.isAccessTokenActive(token)) {
                    SecurityContextHolder.clearContext();
                    filterChain.doFilter(request, response);
                    return;
                }
                UUID userId = UUID.fromString(claims.get("userId", String.class));
                String username = claims.getSubject();
                String realName = claims.get("realName", String.class);
                LoginUser loginUser = new LoginUser(
                        userId,
                        username,
                        "",
                        realName,
                        true,
                        tokenService.readRoles(claims),
                        tokenService.readPermissions(claims));
                SecurityUtils.setAuthentication(loginUser);
            } catch (Exception ex) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
