package com.erp.purchase.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.purchase.domain.dto.PurchasePaymentRequest;
import com.erp.purchase.domain.vo.PurchasePaymentVO;
import com.erp.purchase.permission.PurchasePermissionCodes;
import com.erp.purchase.service.PurchasePaymentService;
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
@RequestMapping("/api/v1/purchase/payments")
public class PurchasePaymentController {
    private final PurchasePaymentService purchasePaymentService;

    public PurchasePaymentController(PurchasePaymentService purchasePaymentService) {
        this.purchasePaymentService = purchasePaymentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.purchase.permission.PurchasePermissionCodes).ORDER_LIST)")
    public R<PageVO<PurchasePaymentVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) UUID purchaseOrderId,
            @RequestParam(required = false) UUID supplierId
    ) {
        return R.ok(purchasePaymentService.listPayments(pageNum, pageSize, purchaseOrderId, supplierId));
    }

    @PostMapping("/{purchaseOrderId}")
    @PreAuthorize("hasAuthority(T(com.erp.purchase.permission.PurchasePermissionCodes).ORDER_UPDATE)")
    public R<PurchasePaymentVO> create(@PathVariable UUID purchaseOrderId, @Valid @RequestBody PurchasePaymentRequest request) {
        return R.ok(purchasePaymentService.createPayment(purchaseOrderId, request));
    }
}
