package com.erp.system.controller;

import com.erp.common.core.domain.R;
import com.erp.system.domain.dto.LoginRequest;
import com.erp.system.domain.dto.RefreshTokenRequest;
import com.erp.system.domain.vo.LoginResponse;
import com.erp.system.domain.vo.UserInfoVO;
import com.erp.system.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public R<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return R.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok(null);
    }

    @GetMapping("/userinfo")
    public R<UserInfoVO> userInfo() {
        return R.ok(authService.userInfo());
    }
}
