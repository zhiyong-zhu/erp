package com.erp.material.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.material.domain.dto.SupplierQuoteRequest;
import com.erp.material.domain.vo.SupplierQuoteVO;
import com.erp.material.service.SupplierQuoteService;
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
@RequestMapping("/api/v1/material/quotes")
public class SupplierQuoteController {
    private final SupplierQuoteService supplierQuoteService;

    public SupplierQuoteController(SupplierQuoteService supplierQuoteService) {
        this.supplierQuoteService = supplierQuoteService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.material.permission.MaterialPermissionCodes).QUOTE_LIST)")
    public R<PageVO<SupplierQuoteVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) UUID materialId
    ) {
        return R.ok(supplierQuoteService.list(pageNum, pageSize, supplierId, materialId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.erp.material.permission.MaterialPermissionCodes).QUOTE_CREATE)")
    public R<SupplierQuoteVO> create(@Valid @RequestBody SupplierQuoteRequest request) {
        return R.ok(supplierQuoteService.save(null, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.material.permission.MaterialPermissionCodes).QUOTE_UPDATE)")
    public R<SupplierQuoteVO> update(@PathVariable UUID id, @Valid @RequestBody SupplierQuoteRequest request) {
        return R.ok(supplierQuoteService.save(id, request));
    }
}
