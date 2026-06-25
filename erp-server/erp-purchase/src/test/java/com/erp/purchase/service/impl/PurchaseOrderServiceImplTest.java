package com.erp.purchase.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.erp.common.core.domain.PageVO;
import com.erp.inventory.service.InventoryReceiptService;
import com.erp.material.service.MaterialService;
import com.erp.purchase.domain.entity.PurchaseOrder;
import com.erp.purchase.domain.entity.PurchaseOrderItem;
import com.erp.purchase.domain.entity.PurchaseReturn;
import com.erp.purchase.domain.vo.PurchasePayableStatVO;
import com.erp.purchase.mapper.PurchaseOrderItemMapper;
import com.erp.purchase.mapper.PurchaseOrderMapper;
import com.erp.purchase.mapper.PurchaseReturnMapper;
import com.erp.purchase.service.PurchaseExceptionService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceImplTest {
    @Mock private PurchaseOrderMapper purchaseOrderMapper;
    @Mock private PurchaseOrderItemMapper purchaseOrderItemMapper;
    @Mock private MaterialService materialService;
    @Mock private InventoryReceiptService inventoryReceiptService;
    @Mock private PurchaseReturnMapper purchaseReturnMapper;
    @Mock private PurchaseExceptionService purchaseExceptionService;

    private PurchaseOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PurchaseOrderServiceImpl(
                purchaseOrderMapper,
                purchaseOrderItemMapper,
                materialService,
                inventoryReceiptService,
                purchaseReturnMapper,
                purchaseExceptionService
        );
    }

    @Test
    void listPayableStatsUsesAcceptedAmountMinusReturns() {
        UUID supplierId = UUID.randomUUID();
        PurchaseOrder order = order(supplierId);
        PurchaseOrderItem item = item(order.getId(), new BigDecimal("40.00"), new BigDecimal("8.50"));
        PurchaseReturn purchaseReturn = purchaseReturn(supplierId, new BigDecimal("51.00"));

        when(purchaseOrderMapper.selectList(any())).thenReturn(List.of(order));
        when(purchaseOrderItemMapper.selectByPurchaseOrderId(order.getId())).thenReturn(List.of(item));
        when(purchaseReturnMapper.selectList(null)).thenReturn(List.of(purchaseReturn));

        PageVO<PurchasePayableStatVO> page = service.listPayableStats(1, 10);

        PurchasePayableStatVO stat = page.getRecords().get(0);
        assertEquals(new BigDecimal("340.0000"), stat.getOrderAmount());
        assertEquals(new BigDecimal("51.00"), stat.getReturnAmount());
        assertEquals(new BigDecimal("289.0000"), stat.getNetPayableAmount());
        assertEquals(1L, stat.getOrderCount());
        assertEquals(1L, stat.getReturnCount());
    }

    private PurchaseOrder order(UUID supplierId) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(UUID.randomUUID());
        order.setOrderNo("PO-001");
        order.setSupplierId(supplierId);
        order.setSupplierName("测试供应商");
        order.setStatus("RECEIVED");
        order.setTotalAmount(new BigDecimal("425.00"));
        return order;
    }

    private PurchaseOrderItem item(UUID orderId, BigDecimal acceptedQuantity, BigDecimal quotePrice) {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setId(UUID.randomUUID());
        item.setPurchaseOrderId(orderId);
        item.setAcceptedQuantity(acceptedQuantity);
        item.setQuotePrice(quotePrice);
        return item;
    }

    private PurchaseReturn purchaseReturn(UUID supplierId, BigDecimal totalAmount) {
        PurchaseReturn purchaseReturn = new PurchaseReturn();
        purchaseReturn.setId(UUID.randomUUID());
        purchaseReturn.setSupplierId(supplierId);
        purchaseReturn.setSupplierName("测试供应商");
        purchaseReturn.setTotalAmount(totalAmount);
        return purchaseReturn;
    }
}
