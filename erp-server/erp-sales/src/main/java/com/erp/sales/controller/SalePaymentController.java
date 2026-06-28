package com.erp.sales.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.sales.domain.dto.SalePaymentRequest;
import com.erp.sales.domain.vo.SalePaymentVO;
import com.erp.sales.permission.SalesPermissionCodes;
import com.erp.sales.service.SalePaymentService;
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
@RequestMapping("/api/v1/sales/payments")
public class SalePaymentController {
    private final SalePaymentService salePaymentService;

    public SalePaymentController(SalePaymentService salePaymentService) {
        this.salePaymentService = salePaymentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).RECEIVABLE_LIST)")
    public R<PageVO<SalePaymentVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) UUID saleOrderId,
            @RequestParam(required = false) UUID customerId
    ) {
        return R.ok(salePaymentService.listPayments(pageNum, pageSize, saleOrderId, customerId));
    }

    @PostMapping("/{saleOrderId}")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).ORDER_UPDATE)")
    public R<SalePaymentVO> create(@PathVariable UUID saleOrderId, @Valid @RequestBody SalePaymentRequest request) {
        return R.ok(salePaymentService.createPayment(saleOrderId, request));
    }
}
