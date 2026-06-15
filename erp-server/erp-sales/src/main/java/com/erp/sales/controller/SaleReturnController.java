package com.erp.sales.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.sales.domain.dto.SaleReturnRequest;
import com.erp.sales.domain.vo.SaleReturnVO;
import com.erp.sales.permission.SalesPermissionCodes;
import com.erp.sales.service.SaleReturnService;
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
@RequestMapping("/api/v1/sales/returns")
public class SaleReturnController {
    private final SaleReturnService saleReturnService;

    public SaleReturnController(SaleReturnService saleReturnService) {
        this.saleReturnService = saleReturnService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).RETURN_LIST)")
    public R<PageVO<SaleReturnVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(saleReturnService.listReturns(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).RETURN_LIST)")
    public R<SaleReturnVO> detail(@PathVariable UUID id) {
        return R.ok(saleReturnService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).RETURN_CREATE)")
    public R<SaleReturnVO> create(@Valid @RequestBody SaleReturnRequest request) {
        return R.ok(saleReturnService.create(request));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).RETURN_UPDATE)")
    public R<SaleReturnVO> changeStatus(@PathVariable UUID id, @RequestBody java.util.Map<String, String> body) {
        String action = body.get("action");
        String remark = body.get("remark");
        return R.ok(saleReturnService.changeStatus(id, action, remark));
    }
}
