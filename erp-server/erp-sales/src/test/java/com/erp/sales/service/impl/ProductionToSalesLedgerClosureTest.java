package com.erp.sales.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.erp.inventory.domain.entity.InventoryTransaction;
import com.erp.inventory.mapper.InventoryTransactionMapper;
import com.erp.material.mapper.MaterialMapper;
import com.erp.product.domain.entity.Product;
import com.erp.product.domain.entity.ProductSku;
import com.erp.product.mapper.ProductMapper;
import com.erp.product.mapper.ProductPackageMapper;
import com.erp.product.mapper.ProductSkuMapper;
import com.erp.production.domain.ProductionBatchStatusMachine;
import com.erp.production.domain.entity.ProductionBatch;
import com.erp.production.domain.entity.ProductionProductStock;
import com.erp.production.mapper.ProductionBatchMapper;
import com.erp.production.mapper.ProductionBomItemMapper;
import com.erp.production.mapper.ProductionBomMapper;
import com.erp.production.mapper.ProductionBoxMapper;
import com.erp.production.mapper.ProductionProcessMapper;
import com.erp.production.mapper.ProductionProcessStepMapper;
import com.erp.production.mapper.ProductionProductStockMapper;
import com.erp.production.mapper.ProductionReportMapper;
import com.erp.production.mapper.SerialNumberMapper;
import com.erp.production.service.ProductionSerialNumberService;
import com.erp.production.service.impl.ProductionServiceImpl;
import com.erp.sales.domain.SaleOrderStatusMachine;
import com.erp.sales.domain.dto.ShippingOrderRequest;
import com.erp.sales.domain.entity.SaleOrder;
import com.erp.sales.domain.entity.SaleOrderItem;
import com.erp.sales.domain.entity.ShippingOrder;
import com.erp.sales.domain.entity.ShippingOrderItem;
import com.erp.sales.mapper.CustomerMapper;
import com.erp.sales.mapper.SaleOrderItemMapper;
import com.erp.sales.mapper.SaleOrderMapper;
import com.erp.sales.mapper.SaleReturnMapper;
import com.erp.sales.mapper.ShippingOrderItemMapper;
import com.erp.sales.mapper.ShippingOrderMapper;
import com.erp.sales.service.SaleExceptionService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductionToSalesLedgerClosureTest {
    @Mock private ProductMapper productMapper;
    @Mock private ProductPackageMapper productPackageMapper;
    @Mock private MaterialMapper materialMapper;
    @Mock private ProductionProcessMapper processMapper;
    @Mock private ProductionProcessStepMapper processStepMapper;
    @Mock private ProductionBomMapper bomMapper;
    @Mock private ProductionBomItemMapper bomItemMapper;
    @Mock private ProductionBatchMapper batchMapper;
    @Mock private ProductionBoxMapper boxMapper;
    @Mock private ProductionProductStockMapper productStockMapper;
    @Mock private ProductionReportMapper reportMapper;
    @Mock private SerialNumberMapper serialNumberMapper;
    @Mock private InventoryTransactionMapper inventoryTransactionMapper;
    @Mock private ProductionSerialNumberService serialNumberService;
    @Mock private SaleOrderMapper saleOrderMapper;
    @Mock private SaleOrderItemMapper saleOrderItemMapper;
    @Mock private ShippingOrderMapper shippingOrderMapper;
    @Mock private ShippingOrderItemMapper shippingOrderItemMapper;
    @Mock private SaleReturnMapper saleReturnMapper;
    @Mock private CustomerMapper customerMapper;
    @Mock private ProductSkuMapper productSkuMapper;
    @Mock private SaleExceptionService saleExceptionService;

    private ProductionServiceImpl productionService;
    private SaleOrderServiceImpl saleOrderService;

    @BeforeEach
    void setUp() {
        productionService = new ProductionServiceImpl(
                productMapper,
                productPackageMapper,
                materialMapper,
                processMapper,
                processStepMapper,
                bomMapper,
                bomItemMapper,
                batchMapper,
                boxMapper,
                productStockMapper,
                reportMapper,
                serialNumberMapper,
                inventoryTransactionMapper,
                serialNumberService
        );
        saleOrderService = new SaleOrderServiceImpl(
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
                serialNumberService
        );
    }

    @Test
    void producedProductStockIsReservedAndConsumedBySalesShippingReview() {
        Product product = product();
        ProductionBatch batch = completedBatch(product.getId());
        when(batchMapper.selectById(batch.getId())).thenReturn(batch);
        when(productMapper.selectById(product.getId())).thenReturn(product);
        when(productStockMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(productStockMapper.selectById(any(UUID.class))).thenReturn(null);

        productionService.receiveBatch(batch.getId());

        ArgumentCaptor<ProductionProductStock> stockCaptor = ArgumentCaptor.forClass(ProductionProductStock.class);
        verify(productStockMapper).insert(stockCaptor.capture());
        ProductionProductStock stock = stockCaptor.getValue();
        assertEquals(new BigDecimal("10.00"), stock.getCurrentStock());
        assertEquals(ProductionBatchStatusMachine.CLOSED, batch.getStatus());

        SaleOrder order = confirmedOrder();
        SaleOrderItem orderItem = orderItem(order.getId());
        ProductSku sku = sku(orderItem.getSkuId(), product.getId());
        when(saleOrderMapper.selectById(order.getId())).thenReturn(order);
        when(saleOrderItemMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of(orderItem));
        when(shippingOrderMapper.selectBySaleOrderId(order.getId())).thenReturn(List.of());
        when(productSkuMapper.selectById(orderItem.getSkuId())).thenReturn(sku);
        when(productStockMapper.selectOne(any(Wrapper.class))).thenReturn(stock);

        saleOrderService.ship(order.getId(), shippingRequest(orderItem.getId()));

        assertEquals(SaleOrderStatusMachine.PENDING_SHIP, order.getStatus());
        assertEquals(new BigDecimal("10.00"), stock.getCurrentStock());
        assertEquals(new BigDecimal("4.00"), stock.getReservedStock());

        ArgumentCaptor<ShippingOrder> shippingCaptor = ArgumentCaptor.forClass(ShippingOrder.class);
        ArgumentCaptor<ShippingOrderItem> shippingItemCaptor = ArgumentCaptor.forClass(ShippingOrderItem.class);
        verify(shippingOrderMapper).insert(shippingCaptor.capture());
        verify(shippingOrderItemMapper).insert(shippingItemCaptor.capture());
        ShippingOrder shipping = shippingCaptor.getValue();
        ShippingOrderItem shippingItem = shippingItemCaptor.getValue();

        when(shippingOrderMapper.selectById(shipping.getId())).thenReturn(shipping);
        when(shippingOrderItemMapper.selectByShippingOrderId(shipping.getId())).thenReturn(List.of(shippingItem));
        when(saleOrderItemMapper.selectById(orderItem.getId())).thenReturn(orderItem);
        when(productStockMapper.decreaseReservedIfEnough(eq(stock.getId()), eq(new BigDecimal("4.00")), any(), any())).thenReturn(1);

        saleOrderService.reviewShipping(shipping.getId());

        assertEquals(SaleOrderStatusMachine.SHIPPED, order.getStatus());
        assertEquals(new BigDecimal("4.00"), orderItem.getShippedQuantity());
        assertEquals(new BigDecimal("6.00"), stock.getCurrentStock());
        assertEquals(new BigDecimal("0.00"), stock.getReservedStock());

        ArgumentCaptor<InventoryTransaction> transactionCaptor = ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(inventoryTransactionMapper, times(2)).insert(transactionCaptor.capture());
        InventoryTransaction productionIn = transactionCaptor.getAllValues().get(0);
        InventoryTransaction saleOut = transactionCaptor.getAllValues().get(1);
        assertEquals("PRODUCTION_IN", productionIn.getTransactionType());
        assertEquals(new BigDecimal("10.00"), productionIn.getQuantity());
        assertEquals("SALE_OUT", saleOut.getTransactionType());
        assertEquals(new BigDecimal("-4.00"), saleOut.getQuantity());
        assertEquals(new BigDecimal("6.00"), saleOut.getBalanceAfter());
    }

    private Product product() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setCode("P-001");
        product.setName("测试产品");
        return product;
    }

    private ProductionBatch completedBatch(UUID productId) {
        ProductionBatch batch = new ProductionBatch();
        batch.setId(UUID.randomUUID());
        batch.setBatchNo("WO-001");
        batch.setProductId(productId);
        batch.setStatus(ProductionBatchStatusMachine.COMPLETED);
        batch.setCompletedQuantity(new BigDecimal("10.00"));
        return batch;
    }

    private SaleOrder confirmedOrder() {
        SaleOrder order = new SaleOrder();
        order.setId(UUID.randomUUID());
        order.setOrderNo("SO-001");
        order.setStatus(SaleOrderStatusMachine.CONFIRMED);
        return order;
    }

    private SaleOrderItem orderItem(UUID orderId) {
        SaleOrderItem item = new SaleOrderItem();
        item.setId(UUID.randomUUID());
        item.setSaleOrderId(orderId);
        item.setSkuId(UUID.randomUUID());
        item.setSkuCode("SKU-001");
        item.setProductName("测试产品");
        item.setQuantity(new BigDecimal("4.00"));
        item.setShippedQuantity(BigDecimal.ZERO);
        return item;
    }

    private ProductSku sku(UUID skuId, UUID productId) {
        ProductSku sku = new ProductSku();
        sku.setId(skuId);
        sku.setProductId(productId);
        sku.setSkuCode("SKU-001");
        return sku;
    }

    private ShippingOrderRequest shippingRequest(UUID orderItemId) {
        ShippingOrderRequest request = new ShippingOrderRequest();
        request.setCarrierName("测试物流");
        request.setTrackingNumber("TRACK-001");
        ShippingOrderRequest.Item item = new ShippingOrderRequest.Item();
        item.setSaleOrderItemId(orderItemId);
        item.setQuantity(new BigDecimal("4.00"));
        request.setItems(List.of(item));
        return request;
    }
}
