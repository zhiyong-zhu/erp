package com.erp.material.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.material.domain.dto.SupplierRequest;
import com.erp.material.domain.vo.SupplierVO;
import com.erp.material.permission.MaterialPermissionCodes;
import com.erp.material.service.SupplierService;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/material/suppliers")
public class SupplierController {
    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.material.permission.MaterialPermissionCodes).SUPPLIER_LIST)")
    public R<PageVO<SupplierVO>> list(@RequestParam(defaultValue = "1") long pageNum,
                                      @RequestParam(defaultValue = "10") long pageSize,
                                      @RequestParam(required = false) String name) {
        return R.ok(supplierService.list(pageNum, pageSize, name));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.erp.material.permission.MaterialPermissionCodes).SUPPLIER_CREATE)")
    public R<SupplierVO> create(@Valid @RequestBody SupplierRequest request) {
        return R.ok(supplierService.save(null, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.material.permission.MaterialPermissionCodes).SUPPLIER_UPDATE)")
    public R<SupplierVO> update(@PathVariable UUID id, @Valid @RequestBody SupplierRequest request) {
        return R.ok(supplierService.save(id, request));
    }
}
