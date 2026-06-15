package com.erp.sales.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.sales.domain.dto.SaleOrderCreateRequest;
import com.erp.sales.domain.dto.SaleOrderStatusRequest;
import com.erp.sales.domain.dto.ShippingOrderRequest;
import com.erp.sales.domain.vo.SaleOrderVO;
import com.erp.sales.domain.vo.SaleReceivableStatVO;
import com.erp.sales.permission.SalesPermissionCodes;
import com.erp.sales.service.DeliveryNoteService;
import com.erp.sales.service.SaleOrderService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/sales/orders")
public class SaleOrderController {
    private final SaleOrderService saleOrderService;
    private final DeliveryNoteService deliveryNoteService;

    public SaleOrderController(SaleOrderService saleOrderService, DeliveryNoteService deliveryNoteService) {
        this.saleOrderService = saleOrderService;
        this.deliveryNoteService = deliveryNoteService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).ORDER_LIST)")
    public R<PageVO<SaleOrderVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerName) {
        return R.ok(saleOrderService.listOrders(pageNum, pageSize, status, customerName));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).ORDER_LIST)")
    public R<SaleOrderVO> detail(@PathVariable UUID id) {
        return R.ok(saleOrderService.detail(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).ORDER_CREATE)")
    public R<SaleOrderVO> create(@Valid @RequestBody SaleOrderCreateRequest request) {
        return R.ok(saleOrderService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).ORDER_UPDATE)")
    public R<SaleOrderVO> update(@PathVariable UUID id, @Valid @RequestBody SaleOrderCreateRequest request) {
        return R.ok(saleOrderService.updateDraft(id, request));
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).ORDER_UPDATE)")
    public R<SaleOrderVO> changeStatus(@PathVariable UUID id, @Valid @RequestBody SaleOrderStatusRequest request) {
        return R.ok(saleOrderService.changeStatus(id, request));
    }

    @PostMapping("/{id}/ship")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).SHIPPING_UPDATE)")
    public R<SaleOrderVO> ship(@PathVariable UUID id, @RequestBody ShippingOrderRequest request) {
        return R.ok(saleOrderService.ship(id, request));
    }

    @GetMapping("/receivables")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).RECEIVABLE_LIST)")
    public R<PageVO<SaleReceivableStatVO>> receivables(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(saleOrderService.listReceivableStats(pageNum, pageSize));
    }

    @GetMapping("/{id}/delivery-note")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).ORDER_LIST)")
    public ResponseEntity<byte[]> deliveryNote(@PathVariable UUID id) {
        byte[] pdf = deliveryNoteService.generatePdf(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"delivery-note.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdf.length)
                .body(pdf);
    }
}
