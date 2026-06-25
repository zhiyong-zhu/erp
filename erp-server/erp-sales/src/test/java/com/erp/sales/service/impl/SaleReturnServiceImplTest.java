package com.erp.sales.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.erp.common.core.exception.BizException;
import com.erp.inventory.domain.entity.InventoryTransaction;
import com.erp.inventory.mapper.InventoryTransactionMapper;
import com.erp.product.domain.entity.ProductSku;
import com.erp.product.mapper.ProductSkuMapper;
import com.erp.production.domain.entity.ProductionProductStock;
import com.erp.production.mapper.ProductionProductStockMapper;
import com.erp.sales.domain.SaleOrderStatusMachine;
import com.erp.sales.domain.dto.SaleReturnRequest;
import com.erp.sales.domain.entity.SaleOrder;
import com.erp.sales.domain.entity.SaleOrderItem;
import com.erp.sales.domain.entity.SaleReturn;
import com.erp.sales.domain.entity.SaleReturnItem;
import com.erp.sales.mapper.SaleOrderItemMapper;
import com.erp.sales.mapper.SaleOrderMapper;
import com.erp.sales.mapper.SaleReturnItemMapper;
import com.erp.sales.mapper.SaleReturnMapper;
import com.erp.sales.service.SaleExceptionService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SaleReturnServiceImplTest {
    @Mock private SaleReturnMapper saleReturnMapper;
    @Mock private SaleReturnItemMapper saleReturnItemMapper;
    @Mock private SaleOrderMapper saleOrderMapper;
    @Mock private SaleOrderItemMapper saleOrderItemMapper;
    @Mock private ProductSkuMapper productSkuMapper;
    @Mock private ProductionProductStockMapper productStockMapper;
    @Mock private InventoryTransactionMapper inventoryTransactionMapper;
    @Mock private SaleExceptionService saleExceptionService;

    private SaleReturnServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SaleReturnServiceImpl(
                saleReturnMapper,
                saleReturnItemMapper,
                saleOrderMapper,
                saleOrderItemMapper,
                productSkuMapper,
                productStockMapper,
                inventoryTransactionMapper,
                saleExceptionService
        );
    }

    @Test
    void createInsertsReturnBeforeItemsToSatisfyForeignKey() {
        SaleOrder order = order();
        order.setStatus(SaleOrderStatusMachine.COMPLETED);

        SaleOrderItem orderItem = new SaleOrderItem();
        orderItem.setId(UUID.randomUUID());
        orderItem.setSaleOrderId(order.getId());
        orderItem.setSkuId(UUID.randomUUID());
        orderItem.setSkuCode("SKU-001");
        orderItem.setProductName("测试产品");
        orderItem.setShippedQuantity(new BigDecimal("2.00"));
        orderItem.setUnitPrice(new BigDecimal("10.00"));

        SaleReturnRequest request = new SaleReturnRequest();
        request.setSaleOrderId(order.getId());
        SaleReturnRequest.Item requestItem = new SaleReturnRequest.Item();
        requestItem.setSaleOrderItemId(orderItem.getId());
        requestItem.setQuantity(new BigDecimal("1.00"));
        request.setItems(List.of(requestItem));

        when(saleOrderMapper.selectById(order.getId())).thenReturn(order);
        when(saleOrderItemMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of(orderItem));
        when(saleReturnMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(saleReturnItemMapper.selectBySaleReturnId(any())).thenReturn(List.of());

        service.create(request);

        InOrder inOrder = inOrder(saleReturnMapper, saleReturnItemMapper);
        inOrder.verify(saleReturnMapper).insert(any(SaleReturn.class));
        inOrder.verify(saleReturnItemMapper).insert(any(SaleReturnItem.class));
        inOrder.verify(saleReturnMapper).updateById(any(SaleReturn.class));
    }

    @Test
    void inspectAddsProductStockResolvedBySku() {
        SaleReturn saleReturn = new SaleReturn();
        saleReturn.setId(UUID.randomUUID());
        saleReturn.setReturnNo("SR-001");
        saleReturn.setStatus("APPROVED");

        UUID skuId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        SaleReturnItem item = new SaleReturnItem();
        item.setId(UUID.randomUUID());
        item.setSaleReturnId(saleReturn.getId());
        item.setSkuId(skuId);
        item.setSkuCode("SKU-001");
        item.setProductName("测试产品");
        item.setQuantity(new BigDecimal("2.00"));

        ProductSku sku = new ProductSku();
        sku.setId(skuId);
        sku.setProductId(productId);
        sku.setSkuCode("SKU-001");

        ProductionProductStock stock = new ProductionProductStock();
        stock.setId(UUID.randomUUID());
        stock.setProductId(productId);
        stock.setProductCode("P-001");
        stock.setProductName("测试产品");
        stock.setCurrentStock(new BigDecimal("5.00"));

        when(saleReturnMapper.selectById(saleReturn.getId())).thenReturn(saleReturn);
        when(saleReturnItemMapper.selectBySaleReturnId(saleReturn.getId())).thenReturn(List.of(item));
        when(productSkuMapper.selectById(skuId)).thenReturn(sku);
        when(productStockMapper.selectOne(any(Wrapper.class))).thenReturn(stock);

        service.changeStatus(saleReturn.getId(), "inspect", null);

        ArgumentCaptor<InventoryTransaction> txnCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(productStockMapper).updateById(stock);
        verify(inventoryTransactionMapper).insert(txnCaptor.capture());
        InventoryTransaction txn = txnCaptor.getValue();
        assertEquals(new BigDecimal("7.00"), stock.getCurrentStock());
        assertEquals(productId, txn.getMaterialId());
        assertEquals("P-001", txn.getMaterialCode());
        assertEquals(new BigDecimal("2.00"), txn.getQuantity());
        assertEquals(new BigDecimal("7.00"), txn.getBalanceAfter());
    }

    @Test
    void createRejectsReturnQuantityBeyondShippedAndExistingReturns() {
        SaleOrder order = order();
        order.setStatus(SaleOrderStatusMachine.COMPLETED);

        SaleOrderItem orderItem = new SaleOrderItem();
        orderItem.setId(UUID.randomUUID());
        orderItem.setSaleOrderId(order.getId());
        orderItem.setSkuId(UUID.randomUUID());
        orderItem.setSkuCode("SKU-001");
        orderItem.setProductName("测试产品");
        orderItem.setQuantity(new BigDecimal("10.00"));
        orderItem.setShippedQuantity(new BigDecimal("5.00"));
        orderItem.setUnitPrice(new BigDecimal("10.00"));

        SaleReturn existingReturn = new SaleReturn();
        existingReturn.setId(UUID.randomUUID());
        existingReturn.setSaleOrderId(order.getId());
        existingReturn.setStatus("APPROVED");

        SaleReturnItem existingReturnItem = new SaleReturnItem();
        existingReturnItem.setId(UUID.randomUUID());
        existingReturnItem.setSaleReturnId(existingReturn.getId());
        existingReturnItem.setSaleOrderItemId(orderItem.getId());
        existingReturnItem.setQuantity(new BigDecimal("2.00"));

        SaleReturnRequest request = new SaleReturnRequest();
        request.setSaleOrderId(order.getId());
        SaleReturnRequest.Item requestItem = new SaleReturnRequest.Item();
        requestItem.setSaleOrderItemId(orderItem.getId());
        requestItem.setQuantity(new BigDecimal("4.00"));
        request.setItems(List.of(requestItem));

        when(saleOrderMapper.selectById(order.getId())).thenReturn(order);
        when(saleOrderItemMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of(orderItem));
        when(saleReturnMapper.selectList(any(Wrapper.class))).thenReturn(List.of(existingReturn));
        when(saleReturnItemMapper.selectBySaleReturnId(existingReturn.getId())).thenReturn(List.of(existingReturnItem));

        assertThrows(BizException.class, () -> service.create(request));
    }

    @Test
    void returnFlowInspectsRefundsCompletesAndKeepsOrderReturningWhenPartiallyReturned() {
        SaleOrder order = order();
        order.setStatus(SaleOrderStatusMachine.COMPLETED);
        SaleOrderItem orderItem = new SaleOrderItem();
        orderItem.setId(UUID.randomUUID());
        orderItem.setSaleOrderId(order.getId());
        orderItem.setShippedQuantity(new BigDecimal("3.00"));

        SaleReturn saleReturn = new SaleReturn();
        saleReturn.setId(UUID.randomUUID());
        saleReturn.setReturnNo("SR-001");
        saleReturn.setSaleOrderId(order.getId());
        saleReturn.setStatus("PENDING_REVIEW");

        UUID skuId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        SaleReturnItem item = new SaleReturnItem();
        item.setId(UUID.randomUUID());
        item.setSaleReturnId(saleReturn.getId());
        item.setSkuId(skuId);
        item.setSkuCode("SKU-001");
        item.setProductName("测试产品");
        item.setQuantity(new BigDecimal("2.00"));
        item.setSaleOrderItemId(orderItem.getId());

        ProductSku sku = new ProductSku();
        sku.setId(skuId);
        sku.setProductId(productId);

        ProductionProductStock stock = new ProductionProductStock();
        stock.setId(UUID.randomUUID());
        stock.setProductId(productId);
        stock.setProductCode("P-001");
        stock.setProductName("测试产品");
        stock.setCurrentStock(new BigDecimal("5.00"));

        when(saleReturnMapper.selectById(saleReturn.getId())).thenReturn(saleReturn);
        when(saleReturnItemMapper.selectBySaleReturnId(saleReturn.getId())).thenReturn(List.of(item));
        when(productSkuMapper.selectById(skuId)).thenReturn(sku);
        when(productStockMapper.selectOne(any(Wrapper.class))).thenReturn(stock);
        when(saleOrderMapper.selectById(order.getId())).thenReturn(order);
        when(saleOrderItemMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of(orderItem));
        when(saleReturnMapper.selectList(any(Wrapper.class))).thenReturn(List.of(saleReturn));

        service.changeStatus(saleReturn.getId(), "approve", null);
        assertEquals("APPROVED", saleReturn.getStatus());

        service.changeStatus(saleReturn.getId(), "inspect", null);
        assertEquals("INSPECTED", saleReturn.getStatus());
        assertEquals(new BigDecimal("7.00"), stock.getCurrentStock());

        service.changeStatus(saleReturn.getId(), "refund", null);
        assertEquals("REFUNDED", saleReturn.getStatus());

        service.changeStatus(saleReturn.getId(), "complete", null);
        assertEquals("COMPLETED", saleReturn.getStatus());
        assertEquals(SaleOrderStatusMachine.RETURNING, order.getStatus());
        verify(productStockMapper).updateById(stock);
        verify(inventoryTransactionMapper).insert(any(InventoryTransaction.class));
        verify(saleOrderMapper).updateById(order);
    }

    @Test
    void completingFullyReturnedOrderMarksOrderReturned() {
        SaleOrder order = order();
        order.setStatus(SaleOrderStatusMachine.RETURNING);
        SaleOrderItem orderItem = new SaleOrderItem();
        orderItem.setId(UUID.randomUUID());
        orderItem.setSaleOrderId(order.getId());
        orderItem.setShippedQuantity(new BigDecimal("2.00"));

        SaleReturn saleReturn = new SaleReturn();
        saleReturn.setId(UUID.randomUUID());
        saleReturn.setReturnNo("SR-001");
        saleReturn.setSaleOrderId(order.getId());
        saleReturn.setStatus("REFUNDED");

        SaleReturnItem item = new SaleReturnItem();
        item.setId(UUID.randomUUID());
        item.setSaleReturnId(saleReturn.getId());
        item.setSaleOrderItemId(orderItem.getId());
        item.setQuantity(new BigDecimal("2.00"));

        when(saleReturnMapper.selectById(saleReturn.getId())).thenReturn(saleReturn);
        when(saleOrderMapper.selectById(order.getId())).thenReturn(order);
        when(saleOrderItemMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of(orderItem));
        when(saleReturnMapper.selectList(any(Wrapper.class))).thenReturn(List.of(saleReturn));
        when(saleReturnItemMapper.selectBySaleReturnId(saleReturn.getId())).thenReturn(List.of(item));

        service.changeStatus(saleReturn.getId(), "complete", null);

        assertEquals("COMPLETED", saleReturn.getStatus());
        assertEquals(SaleOrderStatusMachine.RETURNED, order.getStatus());
        verify(saleOrderMapper).updateById(order);
    }

    private SaleOrder order() {
        SaleOrder order = new SaleOrder();
        order.setId(UUID.randomUUID());
        order.setOrderNo("SO-001");
        order.setCustomerId(UUID.randomUUID());
        order.setCustomerName("测试客户");
        return order;
    }
}
