package com.erp.purchase.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.purchase.domain.dto.PurchaseDraftGenerateRequest;
import com.erp.purchase.domain.dto.PurchaseOrderCreateRequest;
import com.erp.purchase.domain.dto.PurchaseOrderReceiveRequest;
import com.erp.purchase.domain.dto.PurchaseOrderStatusRequest;
import com.erp.purchase.domain.dto.PurchaseOrderUpdateRequest;
import com.erp.purchase.domain.vo.PurchasePayableStatVO;
import com.erp.purchase.domain.vo.PurchaseOrderVO;
import com.erp.purchase.permission.PurchasePermissionCodes;
import com.erp.purchase.service.PurchaseOrderService;
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
@RequestMapping("/api/v1/purchase/orders")
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.purchase.permission.PurchasePermissionCodes).ORDER_LIST)")
    public R<PageVO<PurchaseOrderVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return R.ok(purchaseOrderService.listOrders(pageNum, pageSize));
    }

    @GetMapping("/payables")
    @PreAuthorize("hasAuthority(T(com.erp.purchase.permission.PurchasePermissionCodes).ORDER_LIST)")
    public R<PageVO<PurchasePayableStatVO>> listPayables(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize
    ) {
        return R.ok(purchaseOrderService.listPayableStats(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.purchase.permission.PurchasePermissionCodes).ORDER_LIST)")
    public R<PurchaseOrderVO> detail(@PathVariable UUID id) {
        return R.ok(purchaseOrderService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.erp.purchase.permission.PurchasePermissionCodes).ORDER_CREATE)")
    public R<PurchaseOrderVO> create(@Valid @RequestBody PurchaseOrderCreateRequest request) {
        return R.ok(purchaseOrderService.createDraft(request));
    }

    @PostMapping("/generate-from-replenishment")
    @PreAuthorize("hasAuthority(T(com.erp.purchase.permission.PurchasePermissionCodes).ORDER_CREATE)")
    public R<PageVO<PurchaseOrderVO>> generateFromReplenishment(@Valid @RequestBody PurchaseDraftGenerateRequest request) {
        return R.ok(purchaseOrderService.generateDraftOrdersFromReplenishment(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.purchase.permission.PurchasePermissionCodes).ORDER_UPDATE)")
    public R<PurchaseOrderVO> updateDraft(@PathVariable UUID id, @Valid @RequestBody PurchaseOrderUpdateRequest request) {
        return R.ok(purchaseOrderService.updateDraft(id, request));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority(T(com.erp.purchase.permission.PurchasePermissionCodes).ORDER_UPDATE)")
    public R<PurchaseOrderVO> changeStatus(@PathVariable UUID id, @Valid @RequestBody PurchaseOrderStatusRequest request) {
        return R.ok(purchaseOrderService.changeStatus(id, request));
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAuthority(T(com.erp.purchase.permission.PurchasePermissionCodes).ORDER_UPDATE)")
    public R<PurchaseOrderVO> receive(@PathVariable UUID id, @Valid @RequestBody PurchaseOrderReceiveRequest request) {
        return R.ok(purchaseOrderService.receiveOrder(id, request));
    }
}
