package com.erp.system.controller;

import com.erp.common.core.domain.R;
import com.erp.system.domain.dto.UserCreateRequest;
import com.erp.system.domain.dto.UserStatusUpdateRequest;
import com.erp.system.domain.dto.UserUpdateRequest;
import com.erp.system.domain.vo.DataScopeVO;
import com.erp.system.domain.vo.PageVO;
import com.erp.system.domain.vo.UserFieldPermissionVO;
import com.erp.system.domain.vo.UserVO;
import com.erp.system.security.DataScopeContext;
import com.erp.system.logging.OperationLog;
import com.erp.system.permission.SystemPermissionCodes;
import com.erp.system.service.SysUserService;
import jakarta.validation.Valid;
import java.util.ArrayList;
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
@RequestMapping("/api/v1/system/users")
public class SysUserController {
    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).USER_LIST)")
    public R<PageVO<UserVO>> list(@RequestParam(defaultValue = "1") long pageNum,
                                  @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(sysUserService.listUsers(pageNum, pageSize));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).USER_CREATE)")
    @OperationLog(module = "system:user", action = "create", description = "创建用户")
    public R<UserVO> create(@Valid @RequestBody UserCreateRequest request) {
        return R.ok(sysUserService.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).USER_UPDATE)")
    @OperationLog(module = "system:user", action = "update", description = "更新用户")
    public R<UserVO> update(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request) {
        return R.ok(sysUserService.updateUser(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).USER_UPDATE)")
    @OperationLog(module = "system:user", action = "status", description = "启停用户")
    public R<Void> updateStatus(@PathVariable UUID id, @Valid @RequestBody UserStatusUpdateRequest request) {
        sysUserService.updateStatus(id, request);
        return R.ok(null);
    }

    @GetMapping("/data-scope")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).USER_LIST)")
    public R<DataScopeVO> dataScope() {
        DataScopeContext context = sysUserService.getCurrentDataScope();
        DataScopeVO vo = new DataScopeVO();
        vo.setLevel(context.getLevel().name());
        vo.setUserId(context.getUserId());
        vo.setDepartmentIds(new ArrayList<>(context.getDepartmentIds()));
        return R.ok(vo);
    }

    @GetMapping("/field-permissions")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).USER_LIST)")
    public R<UserFieldPermissionVO> fieldPermissions() {
        return R.ok(sysUserService.getCurrentFieldPermissions());
    }
}
