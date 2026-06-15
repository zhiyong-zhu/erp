package com.erp.sales.integration.impl;

import com.erp.sales.integration.EcommercePlatformService;
import com.erp.sales.integration.PlatformOrder;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Mock adapter for 1688 (Alibaba Wholesale) platform integration.
 * Returns hardcoded test data. Replace with real API calls when 1688 SDK is integrated.
 */
@Service
public class Alibaba1688Adapter implements EcommercePlatformService {

    @Override
    public String getPlatformCode() {
        return "ALIBABA_1688";
    }

    @Override
    public List<PlatformOrder> pullOrders(UUID shopId, OffsetDateTime startTime, OffsetDateTime endTime) {
        // Mock data - replace with real 1688 API calls
        List<PlatformOrder> orders = new ArrayList<>();

        PlatformOrder order1 = new PlatformOrder();
        order1.setPlatformOrderNo("1688-ORD-20260604001");
        order1.setCustomerName("广州XX贸易公司");
        order1.setTotalAmount(new BigDecimal("12800.00"));
        order1.setDiscountAmount(new BigDecimal("500.00"));
        order1.setFreightAmount(new BigDecimal("200.00"));
        order1.setShippingAddress("{\"province\":\"广东省\",\"city\":\"广州市\",\"district\":\"天河区\",\"detail\":\"XX路88号\"}");
        order1.setOrderedAt("2026-06-04T10:30:00+08:00");
        order1.setRawJson("{\"orderId\":\"1688-ORD-20260604001\",\"buyerName\":\"广州XX贸易公司\"}");

        PlatformOrder.PlatformOrderItem item1 = new PlatformOrder.PlatformOrderItem();
        item1.setPlatformSkuId("1688-SKU-001");
        item1.setSkuName("产品A-规格1");
        item1.setQuantity(new BigDecimal("100"));
        item1.setUnitPrice(new BigDecimal("80.00"));

        PlatformOrder.PlatformOrderItem item2 = new PlatformOrder.PlatformOrderItem();
        item2.setPlatformSkuId("1688-SKU-002");
        item2.setSkuName("产品B-规格2");
        item2.setQuantity(new BigDecimal("50"));
        item2.setUnitPrice(new BigDecimal("96.00"));

        order1.setItems(List.of(item1, item2));
        orders.add(order1);

        PlatformOrder order2 = new PlatformOrder();
        order2.setPlatformOrderNo("1688-ORD-20260604002");
        order2.setCustomerName("深圳YY科技");
        order2.setTotalAmount(new BigDecimal("5600.00"));
        order2.setDiscountAmount(BigDecimal.ZERO);
        order2.setFreightAmount(new BigDecimal("150.00"));
        order2.setShippingAddress("{\"province\":\"广东省\",\"city\":\"深圳市\",\"district\":\"南山区\",\"detail\":\"XX大道100号\"}");
        order2.setOrderedAt("2026-06-04T14:20:00+08:00");
        order2.setRawJson("{\"orderId\":\"1688-ORD-20260604002\",\"buyerName\":\"深圳YY科技\"}");

        PlatformOrder.PlatformOrderItem item3 = new PlatformOrder.PlatformOrderItem();
        item3.setPlatformSkuId("1688-SKU-003");
        item3.setSkuName("产品C-规格1");
        item3.setQuantity(new BigDecimal("200"));
        item3.setUnitPrice(new BigDecimal("28.00"));

        order2.setItems(List.of(item3));
        orders.add(order2);

        return orders;
    }
}
