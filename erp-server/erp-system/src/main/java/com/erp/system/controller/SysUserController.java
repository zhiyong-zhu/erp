package com.erp.system.controller;

import com.erp.common.core.domain.R;
import com.erp.system.domain.dto.UserCreateRequest;
import com.erp.system.domain.dto.UserStatusUpdateRequest;
import com.erp.system.domain.dto.UserUpdateRequest;
import com.erp.system.domain.vo.UserVO;
import com.erp.system.service.SysUserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/users")
public class SysUserController {
    private final SysUserService sysUserService;

    public SysUserController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @GetMapping
    public R<List<UserVO>> list() {
        return R.ok(sysUserService.listUsers());
    }

    @PostMapping
    public R<UserVO> create(@Valid @RequestBody UserCreateRequest request) {
        return R.ok(sysUserService.createUser(request));
    }

    @PutMapping("/{id}")
    public R<UserVO> update(@PathVariable UUID id, @RequestBody UserUpdateRequest request) {
        return R.ok(sysUserService.updateUser(id, request));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable UUID id, @Valid @RequestBody UserStatusUpdateRequest request) {
        sysUserService.updateStatus(id, request);
        return R.ok(null);
    }
}
