package com.erp.system.service;

import com.erp.system.domain.dto.LoginRequest;
import com.erp.system.domain.dto.RefreshTokenRequest;
import com.erp.system.domain.vo.LoginResponse;
import com.erp.system.domain.vo.UserInfoVO;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    LoginResponse refresh(RefreshTokenRequest request);
    void logout();
    UserInfoVO userInfo();
}
