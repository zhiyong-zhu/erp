package com.erp.system.service.impl;

import com.erp.common.core.exception.BizException;
import com.erp.common.security.domain.LoginUser;
import com.erp.common.security.service.TokenService;
import com.erp.common.security.util.SecurityUtils;
import com.erp.system.domain.dto.LoginRequest;
import com.erp.system.domain.dto.RefreshTokenRequest;
import com.erp.system.domain.entity.SysUser;
import com.erp.system.domain.vo.LoginResponse;
import com.erp.system.domain.vo.UserInfoVO;
import com.erp.system.service.AuthService;
import com.erp.system.service.SysUserService;
import io.jsonwebtoken.Claims;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final SysUserService sysUserService;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public AuthServiceImpl(SysUserService sysUserService,
                           PasswordEncoder passwordEncoder,
                           TokenService tokenService) {
        this.sysUserService = sysUserService;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserService.getByUsername(request.getUsername());
        if (user == null || Boolean.TRUE.equals(user.getDeleted())) {
            throw new BizException(10001, "用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(10002, "用户已被禁用");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BizException(10001, "用户名或密码错误");
        }
        List<String> roles = sysUserService.getRoleCodes(user.getId());
        List<String> permissions = sysUserService.getPermissions(user.getId());
        LoginUser loginUser = new LoginUser(user.getId(), user.getUsername(), user.getPassword(), user.getRealName(), true, roles, permissions);
        LoginResponse response = new LoginResponse();
        response.setAccessToken(tokenService.createAccessToken(loginUser));
        response.setRefreshToken(tokenService.createRefreshToken(loginUser));
        response.setExpiresIn(tokenService.getAccessTokenExpireSeconds());
        return response;
    }

    @Override
    public LoginResponse refresh(RefreshTokenRequest request) {
        Claims claims = tokenService.parseToken(request.getRefreshToken());
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new BizException(10001, "非法refreshToken");
        }
        SysUser user = sysUserService.getByUsername(claims.getSubject());
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException(10001, "用户不存在或不可用");
        }
        LoginUser loginUser = new LoginUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRealName(),
                true,
                sysUserService.getRoleCodes(user.getId()),
                sysUserService.getPermissions(user.getId()));
        LoginResponse response = new LoginResponse();
        response.setAccessToken(tokenService.createAccessToken(loginUser));
        response.setRefreshToken(tokenService.createRefreshToken(loginUser));
        response.setExpiresIn(tokenService.getAccessTokenExpireSeconds());
        return response;
    }

    @Override
    public void logout() {
    }

    @Override
    public UserInfoVO userInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            throw new BizException(10001, "未登录");
        }
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setUserId(loginUser.getUserId());
        userInfoVO.setUsername(loginUser.getUsername());
        userInfoVO.setRealName(loginUser.getRealName());
        userInfoVO.setRoles(loginUser.getRoles());
        userInfoVO.setPermissions(loginUser.getPermissions());
        return userInfoVO;
    }
}
