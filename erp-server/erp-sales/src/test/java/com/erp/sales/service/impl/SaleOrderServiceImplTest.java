package com.erp.sales.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.erp.common.core.exception.BizException;
import com.erp.common.core.domain.PageVO;
import com.erp.inventory.domain.entity.InventoryTransaction;
import com.erp.inventory.mapper.InventoryTransactionMapper;
import com.erp.product.domain.entity.ProductSku;
import com.erp.product.mapper.ProductSkuMapper;
import com.erp.production.domain.entity.ProductionProductStock;
import com.erp.production.mapper.ProductionProductStockMapper;
import com.erp.production.service.ProductionSerialNumberService;
import com.erp.sales.domain.SaleOrderStatusMachine;
import com.erp.sales.domain.dto.SaleOrderStatusRequest;
import com.erp.sales.domain.dto.SaleOrderCreateRequest;
import com.erp.sales.domain.entity.Customer;
import com.erp.sales.domain.dto.ShippingOrderRequest;
import com.erp.sales.domain.entity.SaleOrder;
import com.erp.sales.domain.entity.SaleOrderItem;
import com.erp.sales.domain.entity.SaleReturn;
import com.erp.sales.domain.entity.ShippingOrder;
import com.erp.sales.domain.entity.ShippingOrderItem;
import com.erp.sales.domain.vo.SaleReceivableStatVO;
import com.erp.sales.mapper.CustomerMapper;
import com.erp.sales.mapper.SaleOrderItemMapper;
import com.erp.sales.mapper.SaleOrderMapper;
import com.erp.sales.mapper.SaleReturnMapper;
import com.erp.sales.mapper.ShippingOrderItemMapper;
import com.erp.sales.mapper.ShippingOrderMapper;
import com.erp.sales.mapper.SaleExceptionMapper;
import com.erp.sales.service.SaleExceptionService;
import com.erp.system.service.SysParamService;
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
class SaleOrderServiceImplTest {
    @Mock private SaleOrderMapper saleOrderMapper;
    @Mock private SaleOrderItemMapper saleOrderItemMapper;
    @Mock private ShippingOrderMapper shippingOrderMapper;
    @Mock private ShippingOrderItemMapper shippingOrderItemMapper;
    @Mock private SaleReturnMapper saleReturnMapper;
    @Mock private CustomerMapper customerMapper;
    @Mock private ProductSkuMapper productSkuMapper;
    @Mock private ProductionProductStockMapper productStockMapper;
    @Mock private InventoryTransactionMapper inventoryTransactionMapper;
    @Mock private SaleExceptionService saleExceptionService;
    @Mock private SaleExceptionMapper saleExceptionMapper;
    @Mock private SysParamService sysParamService;
    @Mock private ProductionSerialNumberService serialNumberService;

    private SaleOrderServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SaleOrderServiceImpl(
                saleOrderMapper,
                saleOrderItemMapper,
                shippingOrderMapper,
                shippingOrderItemMapper,
                saleReturnMapper,
                customerMapper,
                productSkuMapper,
                productStockMapper,
                inventoryTransactionMapper,
                saleExceptionService,
                saleExceptionMapper,
                sysParamService,
                serialNumberService
        );
    }

    @Test
    void createInsertsOrderBeforeItemsToSatisfyForeignKey() {
        UUID customerId = UUID.randomUUID();
        UUID skuId = UUID.randomUUID();
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setName("测试客户");

        SaleOrderCreateRequest request = new SaleOrderCreateRequest();
        request.setCustomerId(customerId);
        request.setOrderSource("MANUAL");
        SaleOrderCreateRequest.Item item = new SaleOrderCreateRequest.Item();
        item.setSkuId(skuId);
        item.setSkuCode("SKU-001");
        item.setProductName("测试产品");
        item.setUnit("pcs");
        item.setQuantity(new BigDecimal("2.00"));
        item.setUnitPrice(new BigDecimal("10.00"));
        request.setItems(List.of(item));

        when(customerMapper.selectById(customerId)).thenReturn(customer);
        when(saleOrderItemMapper.selectBySaleOrderId(any(UUID.class))).thenReturn(List.of());
        when(shippingOrderMapper.selectBySaleOrderId(any(UUID.class))).thenReturn(List.of());

        service.create(request);

        InOrder order = inOrder(saleOrderMapper, saleOrderItemMapper);
        order.verify(saleOrderMapper).insert(any(SaleOrder.class));
        order.verify(saleOrderItemMapper).insert(any(SaleOrderItem.class));
        order.verify(saleOrderMapper).updateById(any(SaleOrder.class));
    }

    @Test
    void shipRejectsWhenOrderAlreadyShipped() {
        SaleOrder order = order();
        order.setStatus(SaleOrderStatusMachine.SHIPPED);
        when(saleOrderMapper.selectById(order.getId())).thenReturn(order);

        assertThrows(BizException.class, () -> service.ship(order.getId(), new ShippingOrderRequest()));

        verify(shippingOrderMapper, never()).insert(any(ShippingOrder.class));
        verify(inventoryTransactionMapper, never()).insert(any(InventoryTransaction.class));
    }

    @Test
    void shipRejectsWhenProductStockMappingIsMissing() {
        SaleOrder order = order();
        SaleOrderItem item = new SaleOrderItem();
        item.setId(UUID.randomUUID());
        item.setSaleOrderId(order.getId());
        item.setSkuId(UUID.randomUUID());
        item.setSkuCode("SKU-001");
        item.setProductName("测试产品");
        item.setQuantity(BigDecimal.ONE);

        when(saleOrderMapper.selectById(order.getId())).thenReturn(order);
        when(saleOrderItemMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of(item));
        when(productSkuMapper.selectById(item.getSkuId())).thenReturn(null);
        when(productSkuMapper.selectBySkuCode(item.getSkuCode())).thenReturn(null);

        assertThrows(BizException.class, () -> service.ship(order.getId(), new ShippingOrderRequest()));

        verify(inventoryTransactionMapper, never()).insert(any(InventoryTransaction.class));
    }

    @Test
    void shipCreatesPendingReviewOrderAndReservesConfirmedOrderStock() {
        SaleOrder order = order();
        SaleOrderItem item = new SaleOrderItem();
        UUID skuId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        item.setId(UUID.randomUUID());
        item.setSaleOrderId(order.getId());
        item.setSkuId(skuId);
        item.setSkuCode("SKU-001");
        item.setProductName("测试产品");
        item.setQuantity(new BigDecimal("3.00"));

        ProductSku sku = new ProductSku();
        sku.setId(skuId);
        sku.setProductId(productId);
        sku.setSkuCode("SKU-001");

        ProductionProductStock stock = new ProductionProductStock();
        stock.setId(UUID.randomUUID());
        stock.setProductId(productId);
        stock.setProductCode("P-001");
        stock.setProductName("测试产品");
        stock.setCurrentStock(new BigDecimal("10.00"));
        stock.setReservedStock(BigDecimal.ZERO);

        when(saleOrderMapper.selectById(order.getId())).thenReturn(order);
        when(shippingOrderMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of());
        when(saleOrderItemMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of(item));
        when(productSkuMapper.selectById(skuId)).thenReturn(sku);
        when(productStockMapper.selectOne(any(Wrapper.class))).thenReturn(stock);

        service.ship(order.getId(), new ShippingOrderRequest());

        ArgumentCaptor<ShippingOrder> shippingCaptor = ArgumentCaptor.forClass(ShippingOrder.class);
        ArgumentCaptor<ShippingOrderItem> shippingItemCaptor = ArgumentCaptor.forClass(ShippingOrderItem.class);
        verify(productStockMapper).updateById(stock);
        verify(shippingOrderMapper).insert(shippingCaptor.capture());
        verify(shippingOrderItemMapper).insert(shippingItemCaptor.capture());
        verify(inventoryTransactionMapper, never()).insert(any(InventoryTransaction.class));
        assertEquals(new BigDecimal("10.00"), stock.getCurrentStock());
        assertEquals(new BigDecimal("3.00"), stock.getReservedStock());
        assertEquals("PENDING_REVIEW", shippingCaptor.getValue().getStatus());
        assertEquals(new BigDecimal("3.00"), shippingItemCaptor.getValue().getQuantity());
        assertEquals(SaleOrderStatusMachine.PENDING_SHIP, order.getStatus());
    }

    @Test
    void reviewShippingDeductsReservedProductStockResolvedBySku() {
        SaleOrder order = order();
        order.setStatus(SaleOrderStatusMachine.PENDING_SHIP);
        SaleOrderItem item = new SaleOrderItem();
        UUID skuId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        item.setId(UUID.randomUUID());
        item.setSaleOrderId(order.getId());
        item.setSkuId(skuId);
        item.setSkuCode("SKU-001");
        item.setProductName("测试产品");
        item.setQuantity(new BigDecimal("3.00"));
        item.setShippedQuantity(BigDecimal.ZERO);

        ProductSku sku = new ProductSku();
        sku.setId(skuId);
        sku.setProductId(productId);

        ProductionProductStock stock = new ProductionProductStock();
        stock.setId(UUID.randomUUID());
        stock.setProductId(productId);
        stock.setProductCode("P-001");
        stock.setProductName("测试产品");
        stock.setCurrentStock(new BigDecimal("10.00"));
        stock.setReservedStock(new BigDecimal("3.00"));

        ShippingOrder shipping = new ShippingOrder();
        shipping.setId(UUID.randomUUID());
        shipping.setSaleOrderId(order.getId());
        shipping.setStatus("PENDING_REVIEW");

        ShippingOrderItem shippingItem = new ShippingOrderItem();
        shippingItem.setId(UUID.randomUUID());
        shippingItem.setShippingOrderId(shipping.getId());
        shippingItem.setSaleOrderItemId(item.getId());
        shippingItem.setQuantity(new BigDecimal("3.00"));
        shippingItem.setSerialNos("SN-001,SN-002,SN-003");

        when(shippingOrderMapper.selectById(shipping.getId())).thenReturn(shipping);
        when(saleOrderMapper.selectById(order.getId())).thenReturn(order);
        when(shippingOrderItemMapper.selectByShippingOrderId(shipping.getId())).thenReturn(List.of(shippingItem));
        when(saleOrderItemMapper.selectById(item.getId())).thenReturn(item);
        when(saleOrderItemMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of(item));
        when(productSkuMapper.selectById(skuId)).thenReturn(sku);
        when(productStockMapper.selectOne(any(Wrapper.class))).thenReturn(stock);
        when(productStockMapper.decreaseReservedIfEnough(any(UUID.class), any(BigDecimal.class), any(), any())).thenReturn(1);

        service.reviewShipping(shipping.getId());

        ArgumentCaptor<InventoryTransaction> txnCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(productStockMapper).decreaseReservedIfEnough(stock.getId(), new BigDecimal("3.00"), null, stock.getUpdatedAt());
        verify(inventoryTransactionMapper).insert(txnCaptor.capture());
        InventoryTransaction txn = txnCaptor.getValue();
        assertEquals(new BigDecimal("7.00"), stock.getCurrentStock());
        assertEquals(new BigDecimal("0.00"), stock.getReservedStock());
        assertEquals(productId, txn.getMaterialId());
        assertEquals("P-001", txn.getMaterialCode());
        assertEquals(new BigDecimal("-3.00"), txn.getQuantity());
        assertEquals(new BigDecimal("7.00"), txn.getBalanceAfter());
        assertEquals(SaleOrderStatusMachine.SHIPPED, order.getStatus());
        verify(serialNumberService).markShipped(eq(productId), eq(List.of("SN-001", "SN-002", "SN-003")), any());
    }

    @Test
    void shipAllowsPartialShipmentAndWaitsForReviewBeforeMarkingPartial() {
        SaleOrder order = order();
        order.setStatus(SaleOrderStatusMachine.PARTIAL_SHIPPED);
        SaleOrderItem item = new SaleOrderItem();
        item.setId(UUID.randomUUID());
        item.setSaleOrderId(order.getId());
        item.setSkuId(UUID.randomUUID());
        item.setSkuCode("SKU-001");
        item.setProductName("测试产品");
        item.setQuantity(new BigDecimal("10.00"));
        item.setShippedQuantity(new BigDecimal("2.00"));

        ShippingOrderRequest request = new ShippingOrderRequest();
        ShippingOrderRequest.Item requestItem = new ShippingOrderRequest.Item();
        requestItem.setSaleOrderItemId(item.getId());
        requestItem.setQuantity(new BigDecimal("3.00"));
        request.setItems(List.of(requestItem));

        when(saleOrderMapper.selectById(order.getId())).thenReturn(order);
        when(shippingOrderMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of());
        when(saleOrderItemMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of(item));

        service.ship(order.getId(), request);

        verify(shippingOrderItemMapper).insert(any(ShippingOrderItem.class));
        verify(inventoryTransactionMapper, never()).insert(any(InventoryTransaction.class));
        assertEquals(new BigDecimal("2.00"), item.getShippedQuantity());
        assertEquals(SaleOrderStatusMachine.PARTIAL_SHIPPED, order.getStatus());
    }

    @Test
    void cancelPendingShipOrderReleasesReservedStock() {
        SaleOrder order = order();
        order.setStatus(SaleOrderStatusMachine.PENDING_SHIP);
        SaleOrderItem item = new SaleOrderItem();
        UUID skuId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        item.setId(UUID.randomUUID());
        item.setSaleOrderId(order.getId());
        item.setSkuId(skuId);
        item.setSkuCode("SKU-001");
        item.setProductName("测试产品");
        item.setQuantity(new BigDecimal("4.00"));
        item.setShippedQuantity(BigDecimal.ZERO);

        ProductSku sku = new ProductSku();
        sku.setId(skuId);
        sku.setProductId(productId);

        ProductionProductStock stock = new ProductionProductStock();
        stock.setId(UUID.randomUUID());
        stock.setProductId(productId);
        stock.setCurrentStock(new BigDecimal("10.00"));
        stock.setReservedStock(new BigDecimal("4.00"));

        SaleOrderStatusRequest request = new SaleOrderStatusRequest();
        request.setAction(SaleOrderStatusMachine.ACTION_CANCEL);

        when(saleOrderMapper.selectById(order.getId())).thenReturn(order);
        when(saleOrderItemMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of(item));
        when(shippingOrderMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of());
        when(productSkuMapper.selectById(skuId)).thenReturn(sku);
        when(productStockMapper.selectOne(any(Wrapper.class))).thenReturn(stock);

        service.changeStatus(order.getId(), request);

        assertEquals(new BigDecimal("0.00"), stock.getReservedStock());
        assertEquals(SaleOrderStatusMachine.CANCELLED, order.getStatus());
    }

    @Test
    void saleShipmentFlowReservesThenReviewsPartialAndFullShipment() {
        SaleOrder order = order();
        order.setStatus(SaleOrderStatusMachine.PENDING_CONFIRM);
        SaleOrderItem item = new SaleOrderItem();
        UUID skuId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        item.setId(UUID.randomUUID());
        item.setSaleOrderId(order.getId());
        item.setSkuId(skuId);
        item.setSkuCode("SKU-001");
        item.setProductName("测试产品");
        item.setQuantity(new BigDecimal("10.00"));
        item.setShippedQuantity(BigDecimal.ZERO);

        ProductSku sku = new ProductSku();
        sku.setId(skuId);
        sku.setProductId(productId);

        ProductionProductStock stock = new ProductionProductStock();
        stock.setId(UUID.randomUUID());
        stock.setProductId(productId);
        stock.setProductCode("P-001");
        stock.setProductName("测试产品");
        stock.setCurrentStock(new BigDecimal("10.00"));
        stock.setReservedStock(BigDecimal.ZERO);

        when(saleOrderMapper.selectById(order.getId())).thenReturn(order);
        when(saleOrderItemMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of(item));
        when(shippingOrderMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of());
        when(productSkuMapper.selectById(skuId)).thenReturn(sku);
        when(productStockMapper.selectOne(any(Wrapper.class))).thenReturn(stock);
        // 开启库存校验开关，使确认流程预占库存
        when(sysParamService.getBoolean(eq("sale_order.confirm_reserve_stock"), eq(true))).thenReturn(true);

        SaleOrderStatusRequest confirmRequest = new SaleOrderStatusRequest();
        confirmRequest.setAction(SaleOrderStatusMachine.ACTION_CONFIRM);
        service.changeStatus(order.getId(), confirmRequest);

        assertEquals(SaleOrderStatusMachine.PENDING_SHIP, order.getStatus());
        assertEquals(new BigDecimal("10.00"), stock.getCurrentStock());
        assertEquals(new BigDecimal("10.00"), stock.getReservedStock());

        ShippingOrderRequest partialRequest = new ShippingOrderRequest();
        ShippingOrderRequest.Item partialItem = new ShippingOrderRequest.Item();
        partialItem.setSaleOrderItemId(item.getId());
        partialItem.setQuantity(new BigDecimal("4.00"));
        partialRequest.setItems(List.of(partialItem));
        service.ship(order.getId(), partialRequest);

        ArgumentCaptor<ShippingOrder> firstShippingCaptor = ArgumentCaptor.forClass(ShippingOrder.class);
        ArgumentCaptor<ShippingOrderItem> firstShippingItemCaptor = ArgumentCaptor.forClass(ShippingOrderItem.class);
        verify(shippingOrderMapper).insert(firstShippingCaptor.capture());
        verify(shippingOrderItemMapper).insert(firstShippingItemCaptor.capture());
        ShippingOrder firstShipping = firstShippingCaptor.getValue();
        ShippingOrderItem firstShippingItem = firstShippingItemCaptor.getValue();
        assertEquals("PENDING_REVIEW", firstShipping.getStatus());
        assertEquals(new BigDecimal("4.00"), firstShippingItem.getQuantity());
        assertEquals(BigDecimal.ZERO, item.getShippedQuantity());

        when(shippingOrderMapper.selectById(firstShipping.getId())).thenReturn(firstShipping);
        when(shippingOrderItemMapper.selectByShippingOrderId(firstShipping.getId())).thenReturn(List.of(firstShippingItem));
        when(saleOrderItemMapper.selectById(item.getId())).thenReturn(item);
        when(productStockMapper.decreaseReservedIfEnough(any(UUID.class), any(BigDecimal.class), any(), any())).thenReturn(1);
        service.reviewShipping(firstShipping.getId());

        assertEquals(SaleOrderStatusMachine.PARTIAL_SHIPPED, order.getStatus());
        assertEquals(new BigDecimal("4.00"), item.getShippedQuantity());
        assertEquals(new BigDecimal("6.00"), stock.getCurrentStock());
        assertEquals(new BigDecimal("6.00"), stock.getReservedStock());

        service.ship(order.getId(), new ShippingOrderRequest());

        ArgumentCaptor<ShippingOrder> allShippingCaptor = ArgumentCaptor.forClass(ShippingOrder.class);
        ArgumentCaptor<ShippingOrderItem> allShippingItemCaptor = ArgumentCaptor.forClass(ShippingOrderItem.class);
        verify(shippingOrderMapper, times(2)).insert(allShippingCaptor.capture());
        verify(shippingOrderItemMapper, times(2)).insert(allShippingItemCaptor.capture());
        ShippingOrder secondShipping = allShippingCaptor.getAllValues().get(1);
        ShippingOrderItem secondShippingItem = allShippingItemCaptor.getAllValues().get(1);
        assertEquals("PENDING_REVIEW", secondShipping.getStatus());
        assertEquals(new BigDecimal("6.00"), secondShippingItem.getQuantity());

        when(shippingOrderMapper.selectById(secondShipping.getId())).thenReturn(secondShipping);
        when(shippingOrderItemMapper.selectByShippingOrderId(secondShipping.getId())).thenReturn(List.of(secondShippingItem));
        service.reviewShipping(secondShipping.getId());

        assertEquals(SaleOrderStatusMachine.SHIPPED, order.getStatus());
        assertEquals(new BigDecimal("10.00"), item.getShippedQuantity());
        assertEquals(new BigDecimal("0.00"), stock.getCurrentStock());
        assertEquals(new BigDecimal("0.00"), stock.getReservedStock());
        verify(inventoryTransactionMapper, times(2)).insert(any(InventoryTransaction.class));
    }

    @Test
    void completeAllowedAfterFullShipmentAndSetsCompletedAt() {
        SaleOrder order = order();
        order.setStatus(SaleOrderStatusMachine.SHIPPED);

        SaleOrderStatusRequest request = new SaleOrderStatusRequest();
        request.setAction(SaleOrderStatusMachine.ACTION_COMPLETE);

        when(saleOrderMapper.selectById(order.getId())).thenReturn(order);
        when(saleOrderItemMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of());
        when(shippingOrderMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of());

        service.changeStatus(order.getId(), request);

        assertEquals(SaleOrderStatusMachine.COMPLETED, order.getStatus());
        assertNotNull(order.getCompletedAt());
        verify(saleOrderMapper).updateById(order);
    }

    @Test
    void receivableStatsDeductRefundedAndCompletedReturns() {
        UUID customerId = UUID.randomUUID();
        SaleOrder completedOrder = order();
        completedOrder.setCustomerId(customerId);
        completedOrder.setCustomerName("测试客户");
        completedOrder.setStatus(SaleOrderStatusMachine.COMPLETED);
        completedOrder.setPayableAmount(new BigDecimal("100.00"));

        SaleReturn refundedReturn = new SaleReturn();
        refundedReturn.setId(UUID.randomUUID());
        refundedReturn.setCustomerId(customerId);
        refundedReturn.setCustomerName("测试客户");
        refundedReturn.setStatus("REFUNDED");
        refundedReturn.setTotalAmount(new BigDecimal("30.00"));

        when(saleOrderMapper.selectList(any(Wrapper.class))).thenReturn(List.of(completedOrder));
        when(saleReturnMapper.selectList(any(Wrapper.class))).thenReturn(List.of(refundedReturn));

        PageVO<SaleReceivableStatVO> page = service.listReceivableStats(1, 10);

        assertEquals(1L, page.getTotal());
        SaleReceivableStatVO stat = page.getRecords().get(0);
        assertEquals(customerId, stat.getCustomerId());
        assertEquals(new BigDecimal("100.00"), stat.getOrderAmount());
        assertEquals(new BigDecimal("30.00"), stat.getReturnAmount());
        assertEquals(new BigDecimal("70.00"), stat.getNetReceivableAmount());
        assertEquals(1L, stat.getOrderCount());
        assertEquals(1L, stat.getReturnCount());
    }

    private SaleOrder order() {
        SaleOrder order = new SaleOrder();
        order.setId(UUID.randomUUID());
        order.setOrderNo("SO-001");
        order.setStatus(SaleOrderStatusMachine.CONFIRMED);
        return order;
    }
}
