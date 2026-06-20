package com.erp.sales.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.sales.domain.dto.SaleExceptionHandleRequest;
import com.erp.sales.domain.vo.SaleExceptionVO;
import com.erp.sales.permission.SalesPermissionCodes;
import com.erp.sales.service.SaleExceptionService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales/exceptions")
public class SaleExceptionController {
    private final SaleExceptionService saleExceptionService;

    public SaleExceptionController(SaleExceptionService saleExceptionService) {
        this.saleExceptionService = saleExceptionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).EXCEPTION_LIST)")
    public R<PageVO<SaleExceptionVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return R.ok(saleExceptionService.list(pageNum, pageSize));
    }

    @PostMapping("/{id}/handle")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).EXCEPTION_UPDATE)")
    public R<SaleExceptionVO> handle(
            @PathVariable UUID id,
            @Valid @RequestBody SaleExceptionHandleRequest request
    ) {
        return R.ok(saleExceptionService.handle(id, request));
    }
}
