package com.erp.system.controller;

import com.erp.common.core.domain.R;
import com.erp.system.domain.dto.RoleCreateRequest;
import com.erp.system.domain.dto.RoleUpdateRequest;
import com.erp.system.domain.dto.StatusUpdateRequest;
import com.erp.system.domain.vo.PageVO;
import com.erp.system.domain.vo.RoleVO;
import com.erp.system.service.SysRoleService;
import jakarta.validation.Valid;
import java.util.UUID;
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
    public R<PageVO<RoleVO>> list(@RequestParam(defaultValue = "1") long pageNum,
                                  @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(sysRoleService.listRoles(pageNum, pageSize));
    }

    @PostMapping
    public R<RoleVO> create(@Valid @RequestBody RoleCreateRequest request) {
        return R.ok(sysRoleService.createRole(request));
    }

    @PutMapping("/{id}")
    public R<RoleVO> update(@PathVariable UUID id, @RequestBody RoleUpdateRequest request) {
        return R.ok(sysRoleService.updateRole(id, request));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest request) {
        sysRoleService.updateStatus(id, request);
        return R.ok(null);
    }
}
