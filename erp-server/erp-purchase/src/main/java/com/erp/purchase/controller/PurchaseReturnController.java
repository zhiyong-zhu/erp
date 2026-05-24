package com.erp.purchase.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.purchase.domain.dto.PurchaseReturnRequest;
import com.erp.purchase.domain.vo.PurchaseReturnVO;
import com.erp.purchase.service.PurchaseReturnService;
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
@RequestMapping("/api/v1/purchase/returns")
public class PurchaseReturnController {
    private final PurchaseReturnService purchaseReturnService;

    public PurchaseReturnController(PurchaseReturnService purchaseReturnService) {
        this.purchaseReturnService = purchaseReturnService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.purchase.permission.PurchasePermissionCodes).ORDER_LIST)")
    public R<PageVO<PurchaseReturnVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return R.ok(purchaseReturnService.list(pageNum, pageSize));
    }

    @PostMapping("/{purchaseOrderId}")
    @PreAuthorize("hasAuthority(T(com.erp.purchase.permission.PurchasePermissionCodes).ORDER_UPDATE)")
    public R<PurchaseReturnVO> create(
            @PathVariable UUID purchaseOrderId,
            @Valid @RequestBody PurchaseReturnRequest request
    ) {
        return R.ok(purchaseReturnService.create(purchaseOrderId, request));
    }
}
