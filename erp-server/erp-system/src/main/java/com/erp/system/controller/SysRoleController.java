package com.erp.system.controller;

import com.erp.common.core.domain.R;
import com.erp.common.core.domain.PageVO;
import com.erp.system.domain.dto.RoleCreateRequest;
import com.erp.system.domain.dto.RolePermissionUpdateRequest;
import com.erp.system.domain.dto.RoleUpdateRequest;
import com.erp.system.domain.dto.StatusUpdateRequest;
import com.erp.system.domain.vo.PermissionVO;
import com.erp.system.domain.vo.RoleVO;
import com.erp.system.logging.OperationLog;
import com.erp.system.permission.SystemPermissionCodes;
import com.erp.system.service.SysRoleService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/roles")
public class SysRoleController {
    private final SysRoleService sysRoleService;

    public SysRoleController(SysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).ROLE_LIST)")
    public R<PageVO<RoleVO>> list(@RequestParam(defaultValue = "1") long pageNum,
                                  @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(sysRoleService.listRoles(pageNum, pageSize));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).ROLE_CREATE)")
    @OperationLog(module = "system:role", action = "create", description = "创建角色")
    public R<RoleVO> create(@Valid @RequestBody RoleCreateRequest request) {
        return R.ok(sysRoleService.createRole(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).ROLE_UPDATE)")
    @OperationLog(module = "system:role", action = "update", description = "更新角色")
    public R<RoleVO> update(@PathVariable UUID id, @Valid @RequestBody RoleUpdateRequest request) {
        return R.ok(sysRoleService.updateRole(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).ROLE_LIST)")
    public R<RoleVO> detail(@PathVariable UUID id) {
        return R.ok(sysRoleService.getRoleDetail(id));
    }

    @GetMapping("/permissions/tree")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).ROLE_LIST)")
    public R<List<PermissionVO>> permissions() {
        return R.ok(sysRoleService.listPermissionTree());
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).ROLE_UPDATE)")
    @OperationLog(module = "system:role", action = "permission", description = "配置角色权限")
    public R<Void> updatePermissions(@PathVariable UUID id, @Valid @RequestBody RolePermissionUpdateRequest request) {
        sysRoleService.updateRolePermissions(id, request);
        return R.ok(null);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).ROLE_UPDATE)")
    @OperationLog(module = "system:role", action = "status", description = "启停角色")
    public R<Void> updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest request) {
        sysRoleService.updateStatus(id, request);
        return R.ok(null);
    }
}
