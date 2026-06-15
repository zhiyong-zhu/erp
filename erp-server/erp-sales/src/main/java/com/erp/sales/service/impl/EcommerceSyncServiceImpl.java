package com.erp.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.sales.domain.entity.EcommerceShop;
import com.erp.sales.domain.entity.EcommerceSkuMapping;
import com.erp.sales.domain.entity.SaleOrder;
import com.erp.sales.domain.entity.SaleOrderItem;
import com.erp.sales.integration.EcommercePlatformService;
import com.erp.sales.integration.PlatformOrder;
import com.erp.sales.mapper.EcommerceShopMapper;
import com.erp.sales.mapper.EcommerceSkuMappingMapper;
import com.erp.sales.mapper.SaleOrderItemMapper;
import com.erp.sales.mapper.SaleOrderMapper;
import com.erp.sales.service.EcommerceSyncService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EcommerceSyncServiceImpl implements EcommerceSyncService {
    private static final Logger log = LoggerFactory.getLogger(EcommerceSyncServiceImpl.class);

    private final EcommerceShopMapper ecommerceShopMapper;
    private final EcommerceSkuMappingMapper ecommerceSkuMappingMapper;
    private final SaleOrderMapper saleOrderMapper;
    private final SaleOrderItemMapper saleOrderItemMapper;
    private final List<EcommercePlatformService> platformAdapters;

    public EcommerceSyncServiceImpl(
            EcommerceShopMapper ecommerceShopMapper,
            EcommerceSkuMappingMapper ecommerceSkuMappingMapper,
            SaleOrderMapper saleOrderMapper,
            SaleOrderItemMapper saleOrderItemMapper,
            List<EcommercePlatformService> platformAdapters
    ) {
        this.ecommerceShopMapper = ecommerceShopMapper;
        this.ecommerceSkuMappingMapper = ecommerceSkuMappingMapper;
        this.saleOrderMapper = saleOrderMapper;
        this.saleOrderItemMapper = saleOrderItemMapper;
        this.platformAdapters = platformAdapters;
    }

    @Override
    @Transactional
    public int syncOrdersFromShop(UUID shopId) {
        EcommerceShop shop = ecommerceShopMapper.selectById(shopId);
        if (shop == null) {
            throw new BizException(10004, "店铺不存在");
        }
        if (shop.getStatus() != 1) {
            throw new BizException(10004, "店铺已禁用，无法同步");
        }

        EcommercePlatformService adapter = platformAdapters.stream()
                .filter(a -> a.getPlatformCode().equals(shop.getPlatform()))
                .findFirst()
                .orElseThrow(() -> new BizException(10004, "不支持的平台: " + shop.getPlatform()));

        List<PlatformOrder> platformOrders = adapter.pullOrders(shopId, null, null);
        int syncedCount = 0;

        for (PlatformOrder platformOrder : platformOrders) {
            // Check for duplicate platform order no
            Long existing = saleOrderMapper.selectCount(
                    new LambdaQueryWrapper<SaleOrder>()
                            .eq(SaleOrder::getPlatformOrderNo, platformOrder.getPlatformOrderNo()));
            if (existing > 0) {
                log.info("Skipping duplicate platform order: {}", platformOrder.getPlatformOrderNo());
                continue;
            }

            SaleOrder order = new SaleOrder();
            order.setId(UUID.randomUUID());
            order.setOrderNo(generateOrderNo("SO"));
            order.setCustomerId(null); // No customer mapping for platform orders yet
            order.setCustomerName(platformOrder.getCustomerName() != null ? platformOrder.getCustomerName() : "电商客户");
            order.setOrderSource(shop.getPlatform());
            order.setPlatformOrderNo(platformOrder.getPlatformOrderNo());
            order.setPlatformData(platformOrder.getRawJson());
            order.setStatus("PENDING_CONFIRM");
            order.setDiscountAmount(safe(platformOrder.getDiscountAmount()));
            order.setFreightAmount(safe(platformOrder.getFreightAmount()));
            order.setPaidAmount(BigDecimal.ZERO);
            order.setPaymentStatus("UNPAID");
            order.setShippingAddress(platformOrder.getShippingAddress());
            order.setOrderedAt(OffsetDateTime.now());
            order.setCreatedBy(SecurityUtils.getUserId());
            order.setCreatedAt(OffsetDateTime.now());
            order.setUpdatedBy(SecurityUtils.getUserId());
            order.setUpdatedAt(OffsetDateTime.now());

            // Load SKU mappings for this shop
            List<EcommerceSkuMapping> skuMappings = ecommerceSkuMappingMapper.selectList(
                    new LambdaQueryWrapper<EcommerceSkuMapping>().eq(EcommerceSkuMapping::getShopId, shopId));
            var skuMap = skuMappings.stream()
                    .collect(java.util.stream.Collectors.toMap(
                            EcommerceSkuMapping::getPlatformSkuId, m -> m, (a, b) -> a));

            BigDecimal totalAmount = BigDecimal.ZERO;
            if (platformOrder.getItems() != null) {
                for (PlatformOrder.PlatformOrderItem pItem : platformOrder.getItems()) {
                    SaleOrderItem item = new SaleOrderItem();
                    item.setId(UUID.randomUUID());
                    item.setSaleOrderId(order.getId());

                    EcommerceSkuMapping mapping = skuMap.get(pItem.getPlatformSkuId());
                    if (mapping != null) {
                        item.setSkuId(mapping.getSkuId());
                    }
                    item.setSkuCode(pItem.getPlatformSkuId());
                    item.setProductName(pItem.getSkuName());
                    item.setQuantity(pItem.getQuantity());
                    item.setShippedQuantity(BigDecimal.ZERO);
                    item.setUnitPrice(pItem.getUnitPrice());
                    BigDecimal amount = pItem.getQuantity() != null && pItem.getUnitPrice() != null
                            ? pItem.getQuantity().multiply(pItem.getUnitPrice()) : BigDecimal.ZERO;
                    item.setAmount(amount);
                    item.setCreatedAt(OffsetDateTime.now());
                    saleOrderItemMapper.insert(item);
                    totalAmount = totalAmount.add(amount);
                }
            }

            order.setTotalAmount(totalAmount);
            order.setPayableAmount(totalAmount.subtract(safe(order.getDiscountAmount())).add(safe(order.getFreightAmount())));
            saleOrderMapper.insert(order);
            syncedCount++;
        }

        log.info("Synced {} orders from shop {} ({})", syncedCount, shop.getShopName(), shop.getPlatform());
        return syncedCount;
    }

    private String generateOrderNo(String prefix) {
        return prefix + "-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
