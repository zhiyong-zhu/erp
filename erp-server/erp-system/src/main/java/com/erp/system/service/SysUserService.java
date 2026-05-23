package com.erp.system.service;

import com.erp.system.domain.dto.UserCreateRequest;
import com.erp.system.domain.dto.UserStatusUpdateRequest;
import com.erp.system.domain.dto.UserUpdateRequest;
import com.erp.system.domain.entity.SysUser;
import com.erp.system.security.DataScopeContext;
import com.erp.system.domain.vo.PageVO;
import com.erp.system.domain.vo.UserFieldPermissionVO;
import com.erp.system.domain.vo.UserVO;
import java.util.List;
import java.util.UUID;

public interface SysUserService {
    SysUser getByUsername(String username);
    List<String> getRoleCodes(UUID userId);
    List<Integer> getRoleDataScopes(UUID userId);
    List<String> getPermissions(UUID userId);
    PageVO<UserVO> listUsers(long pageNum, long pageSize);
    UserVO createUser(UserCreateRequest request);
    UserVO updateUser(UUID id, UserUpdateRequest request);
    void updateStatus(UUID id, UserStatusUpdateRequest request);
    DataScopeContext getCurrentDataScope();
    UserFieldPermissionVO getCurrentFieldPermissions();
}
