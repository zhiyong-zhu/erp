package com.erp.system.security;

import com.erp.common.security.domain.LoginUser;
import com.erp.common.security.util.SecurityUtils;
import com.erp.system.permission.SystemPermissionCodes;
import org.springframework.stereotype.Service;

@Service
public class FieldPermissionServiceImpl implements FieldPermissionService {
    @Override
    public boolean canViewUserSensitiveFields() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return loginUser != null && loginUser.getPermissions().contains(SystemPermissionCodes.USER_SENSITIVE_VIEW);
    }

    @Override
    public boolean canEditUserSensitiveFields() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        return loginUser != null && loginUser.getPermissions().contains(SystemPermissionCodes.USER_SENSITIVE_EDIT);
    }

    @Override
    public String currentPermissionSnapshot() {
        return "userSensitiveView=" + canViewUserSensitiveFields()
                + ", userSensitiveEdit=" + canEditUserSensitiveFields();
    }
}
