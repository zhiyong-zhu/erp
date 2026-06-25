package com.erp.purchase.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.erp.common.core.exception.BizException;
import com.erp.inventory.domain.entity.InventoryBalance;
import com.erp.inventory.domain.entity.InventoryTransaction;
import com.erp.inventory.mapper.InventoryTransactionMapper;
import com.erp.inventory.service.InventoryBalanceService;
import com.erp.inventory.service.InventoryBalanceService.InventoryPosition;
import com.erp.material.domain.entity.Material;
import com.erp.material.mapper.MaterialMapper;
import com.erp.purchase.domain.dto.PurchaseReturnRequest;
import com.erp.purchase.domain.entity.PurchaseOrder;
import com.erp.purchase.domain.entity.PurchaseOrderItem;
import com.erp.purchase.mapper.PurchaseOrderItemMapper;
import com.erp.purchase.mapper.PurchaseOrderMapper;
import com.erp.purchase.mapper.PurchaseReturnItemMapper;
import com.erp.purchase.mapper.PurchaseReturnMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PurchaseReturnServiceImplTest {
    @Mock private PurchaseReturnMapper purchaseReturnMapper;
    @Mock private PurchaseReturnItemMapper purchaseReturnItemMapper;
    @Mock private PurchaseOrderMapper purchaseOrderMapper;
    @Mock private PurchaseOrderItemMapper purchaseOrderItemMapper;
    @Mock private MaterialMapper materialMapper;
    @Mock private InventoryTransactionMapper inventoryTransactionMapper;
    @Mock private InventoryBalanceService inventoryBalanceService;

    private PurchaseReturnServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PurchaseReturnServiceImpl(
                purchaseReturnMapper,
                purchaseReturnItemMapper,
                purchaseOrderMapper,
                purchaseOrderItemMapper,
                materialMapper,
                inventoryTransactionMapper,
                inventoryBalanceService
        );
    }

    @Test
    void createDecreasesInventoryBalanceAndWritesReturnTransaction() {
        UUID purchaseOrderId = UUID.randomUUID();
        UUID purchaseOrderItemId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();
        PurchaseOrder order = order(purchaseOrderId);
        PurchaseOrderItem orderItem = orderItem(purchaseOrderId, purchaseOrderItemId, materialId);
        Material material = material(materialId, new BigDecimal("40.00"));
        InventoryBalance balance = balance(materialId, new BigDecimal("34.00"));
        PurchaseReturnRequest request = request(purchaseOrderItemId, new BigDecimal("6.00"));

        when(purchaseOrderMapper.selectById(purchaseOrderId)).thenReturn(order);
        when(purchaseOrderItemMapper.selectById(purchaseOrderItemId)).thenReturn(orderItem);
        when(materialMapper.selectById(materialId)).thenReturn(material);
        when(inventoryBalanceService.decrease(any(Material.class), any(BigDecimal.class), any(InventoryPosition.class))).thenReturn(balance);
        when(purchaseReturnItemMapper.selectByPurchaseReturnId(any(UUID.class))).thenReturn(List.of());

        service.create(purchaseOrderId, request);

        assertEquals(new BigDecimal("6.00"), orderItem.getReturnedQuantity());
        verify(purchaseOrderItemMapper).updateById(orderItem);
        verify(inventoryBalanceService).decrease(
                material,
                new BigDecimal("6.00"),
                InventoryPosition.defaults()
        );
        verify(inventoryTransactionMapper).insert(argThat((InventoryTransaction transaction) ->
                "PURCHASE_RETURN".equals(transaction.getTransactionType())
                        && new BigDecimal("-6.00").compareTo(transaction.getQuantity()) == 0
                        && new BigDecimal("40.00").compareTo(transaction.getBalanceBefore()) == 0
                        && new BigDecimal("34.00").compareTo(transaction.getBalanceAfter()) == 0
                        && "MAIN".equals(transaction.getWarehouseCode())
                        && "DEFAULT".equals(transaction.getLocationCode())
                        && "DEFAULT".equals(transaction.getBatchNo())
                        && transaction.getSourceOrderId() != null
                        && transaction.getSourceOrderNo() != null
                        && transaction.getSourceOrderNo().startsWith("PR-")
                        && transaction.getSourceItemId() != null
        ));
    }

    @Test
    void createRejectsQuantityGreaterThanAcceptedMinusReturned() {
        UUID purchaseOrderId = UUID.randomUUID();
        UUID purchaseOrderItemId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();
        PurchaseOrderItem orderItem = orderItem(purchaseOrderId, purchaseOrderItemId, materialId);
        orderItem.setReturnedQuantity(new BigDecimal("38.00"));

        when(purchaseOrderMapper.selectById(purchaseOrderId)).thenReturn(order(purchaseOrderId));
        when(purchaseOrderItemMapper.selectById(purchaseOrderItemId)).thenReturn(orderItem);

        BizException exception = assertThrows(
                BizException.class,
                () -> service.create(purchaseOrderId, request(purchaseOrderItemId, new BigDecimal("3.00")))
        );

        assertEquals("退货数量不能超过可退数量", exception.getMessage());
    }

    private PurchaseOrder order(UUID id) {
        PurchaseOrder order = new PurchaseOrder();
        order.setId(id);
        order.setOrderNo("PO-001");
        order.setSupplierId(UUID.randomUUID());
        order.setSupplierName("测试供应商");
        return order;
    }

    private PurchaseOrderItem orderItem(UUID orderId, UUID itemId, UUID materialId) {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setId(itemId);
        item.setPurchaseOrderId(orderId);
        item.setMaterialId(materialId);
        item.setMaterialCode("MAT-001");
        item.setMaterialName("测试原料");
        item.setUnit("kg");
        item.setAcceptedQuantity(new BigDecimal("40.00"));
        item.setReturnedQuantity(BigDecimal.ZERO);
        item.setQuotePrice(new BigDecimal("8.50"));
        return item;
    }

    private Material material(UUID materialId, BigDecimal currentStock) {
        Material material = new Material();
        material.setId(materialId);
        material.setCode("MAT-001");
        material.setName("测试原料");
        material.setCurrentStock(currentStock);
        return material;
    }

    private InventoryBalance balance(UUID materialId, BigDecimal quantity) {
        InventoryBalance balance = new InventoryBalance();
        balance.setId(UUID.randomUUID());
        balance.setMaterialId(materialId);
        balance.setWarehouseCode("MAIN");
        balance.setWarehouseName("主仓");
        balance.setLocationCode("DEFAULT");
        balance.setLocationName("默认库位");
        balance.setBatchNo("DEFAULT");
        balance.setAvailableQuantity(quantity);
        balance.setFrozenQuantity(BigDecimal.ZERO);
        balance.setTotalQuantity(quantity);
        return balance;
    }

    private PurchaseReturnRequest request(UUID itemId, BigDecimal quantity) {
        PurchaseReturnRequest.ReturnItem item = new PurchaseReturnRequest.ReturnItem();
        item.setItemId(itemId);
        item.setReturnQuantity(quantity);
        item.setReason("复检退回");
        PurchaseReturnRequest request = new PurchaseReturnRequest();
        request.setItems(List.of(item));
        request.setRemark("采购退货");
        return request;
    }
}
