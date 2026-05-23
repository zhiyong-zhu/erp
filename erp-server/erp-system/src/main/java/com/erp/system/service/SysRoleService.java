package com.erp.system.service;

import com.erp.common.core.domain.PageVO;
import com.erp.system.domain.dto.RoleCreateRequest;
import com.erp.system.domain.dto.RolePermissionUpdateRequest;
import com.erp.system.domain.dto.RoleUpdateRequest;
import com.erp.system.domain.dto.StatusUpdateRequest;
import com.erp.system.domain.vo.PermissionVO;
import com.erp.system.domain.vo.RoleVO;
import java.util.List;
import java.util.UUID;

public interface SysRoleService {
    PageVO<RoleVO> listRoles(long pageNum, long pageSize);
    RoleVO createRole(RoleCreateRequest request);
    RoleVO updateRole(UUID id, RoleUpdateRequest request);
    void updateStatus(UUID id, StatusUpdateRequest request);
    RoleVO getRoleDetail(UUID id);
    List<PermissionVO> listPermissionTree();
    void updateRolePermissions(UUID id, RolePermissionUpdateRequest request);
}
