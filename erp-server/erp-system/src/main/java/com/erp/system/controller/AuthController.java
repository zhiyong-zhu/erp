package com.erp.system.controller;

import com.erp.common.core.domain.R;
import com.erp.system.domain.dto.LoginRequest;
import com.erp.system.domain.dto.RefreshTokenRequest;
import com.erp.system.domain.vo.LoginResponse;
import com.erp.system.domain.vo.UserInfoVO;
import com.erp.system.logging.OperationLog;
import com.erp.system.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestHeader;
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
    @OperationLog(module = "auth", action = "login", description = "用户登录")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return R.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public R<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return R.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    @OperationLog(module = "auth", action = "logout", description = "用户登出")
    public R<Void> logout(@RequestHeader(value = "X-Refresh-Token", required = false) String refreshToken) {
        authService.logout(refreshToken);
        return R.ok(null);
    }

    @GetMapping("/userinfo")
    public R<UserInfoVO> userInfo() {
        return R.ok(authService.userInfo());
    }
}
