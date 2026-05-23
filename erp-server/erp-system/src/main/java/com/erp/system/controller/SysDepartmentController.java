package com.erp.system.controller;

import com.erp.common.core.domain.R;
import com.erp.system.domain.dto.DepartmentCreateRequest;
import com.erp.system.domain.dto.DepartmentUpdateRequest;
import com.erp.system.domain.dto.StatusUpdateRequest;
import com.erp.system.domain.vo.DepartmentVO;
import com.erp.system.logging.OperationLog;
import com.erp.system.permission.SystemPermissionCodes;
import com.erp.system.service.SysDepartmentService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system/departments")
public class SysDepartmentController {
    private final SysDepartmentService sysDepartmentService;

    public SysDepartmentController(SysDepartmentService sysDepartmentService) {
        this.sysDepartmentService = sysDepartmentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).DEPT_LIST)")
    public R<List<DepartmentVO>> list() {
        return R.ok(sysDepartmentService.listDepartments());
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).DEPT_CREATE)")
    @OperationLog(module = "system:dept", action = "create", description = "创建部门")
    public R<DepartmentVO> create(@Valid @RequestBody DepartmentCreateRequest request) {
        return R.ok(sysDepartmentService.createDepartment(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).DEPT_UPDATE)")
    @OperationLog(module = "system:dept", action = "update", description = "更新部门")
    public R<DepartmentVO> update(@PathVariable UUID id, @Valid @RequestBody DepartmentUpdateRequest request) {
        return R.ok(sysDepartmentService.updateDepartment(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority(T(com.erp.system.permission.SystemPermissionCodes).DEPT_UPDATE)")
    @OperationLog(module = "system:dept", action = "status", description = "启停部门")
    public R<Void> updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest request) {
        sysDepartmentService.updateStatus(id, request);
        return R.ok(null);
    }
}
