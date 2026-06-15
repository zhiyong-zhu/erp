package com.erp.sales.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.domain.R;
import com.erp.common.core.exception.BizException;
import com.erp.sales.domain.entity.EcommerceShop;
import com.erp.sales.mapper.EcommerceShopMapper;
import com.erp.sales.permission.SalesPermissionCodes;
import com.erp.sales.service.EcommerceSyncService;
import java.util.Map;
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
@RequestMapping("/api/v1/sales/ecommerce")
public class EcommerceController {
    private final EcommerceShopMapper ecommerceShopMapper;
    private final EcommerceSyncService ecommerceSyncService;

    public EcommerceController(EcommerceShopMapper ecommerceShopMapper, EcommerceSyncService ecommerceSyncService) {
        this.ecommerceShopMapper = ecommerceShopMapper;
        this.ecommerceSyncService = ecommerceSyncService;
    }

    @GetMapping("/shops")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).ORDER_LIST)")
    public R<PageVO<EcommerceShop>> listShops(
            @RequestParam(defaultValue = "1") long pageNum,
            @RequestParam(defaultValue = "10") long pageSize) {
        Page<EcommerceShop> page = ecommerceShopMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<EcommerceShop>().orderByDesc(EcommerceShop::getCreatedAt));
        return R.ok(new PageVO<>(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize()));
    }

    @PostMapping("/shops")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).ORDER_CREATE)")
    public R<EcommerceShop> createShop(@RequestBody EcommerceShop shop) {
        if (shop.getId() == null) {
            shop.setId(UUID.randomUUID());
        }
        if (shop.getStatus() == null) {
            shop.setStatus(1);
        }
        ecommerceShopMapper.insert(shop);
        return R.ok(shop);
    }

    @PostMapping("/shops/{shopId}/sync-orders")
    @PreAuthorize("hasAuthority(T(com.erp.sales.permission.SalesPermissionCodes).ORDER_CREATE)")
    public R<Map<String, Object>> syncOrders(@PathVariable UUID shopId) {
        int count = ecommerceSyncService.syncOrdersFromShop(shopId);
        return R.ok(Map.of("syncedCount", count));
    }
}
