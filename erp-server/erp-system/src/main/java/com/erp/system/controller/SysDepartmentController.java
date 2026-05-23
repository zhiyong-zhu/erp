package com.erp.system.controller;

import com.erp.common.core.domain.R;
import com.erp.system.domain.dto.DepartmentCreateRequest;
import com.erp.system.domain.dto.DepartmentUpdateRequest;
import com.erp.system.domain.dto.StatusUpdateRequest;
import com.erp.system.domain.vo.DepartmentVO;
import com.erp.system.service.SysDepartmentService;
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
@RequestMapping("/api/v1/system/departments")
public class SysDepartmentController {
    private final SysDepartmentService sysDepartmentService;

    public SysDepartmentController(SysDepartmentService sysDepartmentService) {
        this.sysDepartmentService = sysDepartmentService;
    }

    @GetMapping
    public R<List<DepartmentVO>> list() {
        return R.ok(sysDepartmentService.listDepartments());
    }

    @PostMapping
    public R<DepartmentVO> create(@Valid @RequestBody DepartmentCreateRequest request) {
        return R.ok(sysDepartmentService.createDepartment(request));
    }

    @PutMapping("/{id}")
    public R<DepartmentVO> update(@PathVariable UUID id, @RequestBody DepartmentUpdateRequest request) {
        return R.ok(sysDepartmentService.updateDepartment(id, request));
    }

    @PutMapping("/{id}/status")
    public R<Void> updateStatus(@PathVariable UUID id, @Valid @RequestBody StatusUpdateRequest request) {
        sysDepartmentService.updateStatus(id, request);
        return R.ok(null);
    }
}
