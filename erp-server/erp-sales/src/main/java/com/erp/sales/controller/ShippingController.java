package com.erp.sales.controller;

import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.sales.domain.vo.ShippingVO;
import com.erp.sales.permission.SalesPermissionCodes;
import com.erp.sales.service.SaleOrderService;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sales/shipping")
public class ShippingController {
    private final SaleOrderService saleOrderService;

    public ShippingController(SaleOrderService saleOrderService) {
        this.saleOrderService = saleOrderService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).SHIPPING_LIST)")
    public R<PageVO<ShippingVO>> list(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        return R.ok(saleOrderService.listShipping(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).SHIPPING_LIST)")
    public R<ShippingVO> detail(@PathVariable UUID id) {
        return R.ok(saleOrderService.shippingDetail(id));
    }
}
