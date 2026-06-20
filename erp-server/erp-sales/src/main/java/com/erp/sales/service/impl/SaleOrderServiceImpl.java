package com.erp.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.inventory.domain.entity.InventoryTransaction;
import com.erp.inventory.mapper.InventoryTransactionMapper;
import com.erp.material.domain.entity.Material;
import com.erp.material.mapper.MaterialMapper;
import com.erp.production.domain.entity.ProductionProductStock;
import com.erp.production.mapper.ProductionProductStockMapper;
import com.erp.sales.domain.dto.SaleOrderCreateRequest;
import com.erp.sales.domain.dto.SaleOrderStatusRequest;
import com.erp.sales.domain.dto.ShippingOrderRequest;
import com.erp.sales.domain.entity.Customer;
import com.erp.sales.domain.entity.SaleOrder;
import com.erp.sales.domain.entity.SaleOrderItem;
import com.erp.sales.domain.entity.SaleReturn;
import com.erp.sales.domain.entity.ShippingOrder;
import com.erp.sales.domain.vo.SaleOrderItemVO;
import com.erp.sales.domain.vo.SaleOrderVO;
import com.erp.sales.domain.vo.SaleReceivableStatVO;
import com.erp.sales.domain.vo.ShippingVO;
import com.erp.sales.mapper.CustomerMapper;
import com.erp.sales.mapper.SaleOrderItemMapper;
import com.erp.sales.mapper.SaleOrderMapper;
import com.erp.sales.mapper.SaleReturnMapper;
import com.erp.sales.mapper.ShippingOrderMapper;
import com.erp.sales.service.SaleExceptionService;
import com.erp.sales.service.SaleOrderService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaleOrderServiceImpl implements SaleOrderService {
    private static final Logger log = LoggerFactory.getLogger(SaleOrderServiceImpl.class);

    private final SaleOrderMapper saleOrderMapper;
    private final SaleOrderItemMapper saleOrderItemMapper;
    private final ShippingOrderMapper shippingOrderMapper;
    private final SaleReturnMapper saleReturnMapper;
    private final CustomerMapper customerMapper;
    private final MaterialMapper materialMapper;
    private final ProductionProductStockMapper productStockMapper;
    private final InventoryTransactionMapper inventoryTransactionMapper;
    private final SaleExceptionService saleExceptionService;

    public SaleOrderServiceImpl(
            SaleOrderMapper saleOrderMapper,
            SaleOrderItemMapper saleOrderItemMapper,
            ShippingOrderMapper shippingOrderMapper,
            SaleReturnMapper saleReturnMapper,
            CustomerMapper customerMapper,
            MaterialMapper materialMapper,
            ProductionProductStockMapper productStockMapper,
            InventoryTransactionMapper inventoryTransactionMapper,
            SaleExceptionService saleExceptionService
    ) {
        this.saleOrderMapper = saleOrderMapper;
        this.saleOrderItemMapper = saleOrderItemMapper;
        this.shippingOrderMapper = shippingOrderMapper;
        this.saleReturnMapper = saleReturnMapper;
        this.customerMapper = customerMapper;
        this.materialMapper = materialMapper;
        this.productStockMapper = productStockMapper;
        this.inventoryTransactionMapper = inventoryTransactionMapper;
        this.saleExceptionService = saleExceptionService;
    }

    @Override
    public PageVO<SaleOrderVO> listOrders(long pageNum, long pageSize, String status, String customerName) {
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<SaleOrder>()
                .eq(status != null && !status.isBlank(), SaleOrder::getStatus, status)
                .like(customerName != null && !customerName.isBlank(), SaleOrder::getCustomerName, customerName)
                .orderByDesc(SaleOrder::getCreatedAt);
        Page<SaleOrder> page = saleOrderMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<SaleOrderVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public SaleOrderVO detail(UUID id) {
        return toVO(getOrder(id));
    }

    @Override
    @Transactional
    public SaleOrderVO create(SaleOrderCreateRequest request) {
        Customer customer = customerMapper.selectById(request.getCustomerId());
        if (customer == null) {
            throw new BizException(10004, "客户不存在");
        }

        SaleOrder order = new SaleOrder();
        order.setId(UUID.randomUUID());
        order.setOrderNo(generateOrderNo("SO"));
        order.setCustomerId(customer.getId());
        order.setCustomerName(customer.getName());
        order.setOrderSource(request.getOrderSource());
        order.setPlatformOrderNo(request.getPlatformOrderNo());
        order.setPlatformData(request.getPlatformData());
        order.setStatus("PENDING_CONFIRM");
        order.setDiscountAmount(safe(request.getDiscountAmount()));
        order.setFreightAmount(safe(request.getFreightAmount()));
        order.setPaidAmount(BigDecimal.ZERO);
        order.setPaymentStatus("UNPAID");
        order.setShippingAddress(request.getShippingAddress());
        order.setRemark(request.getRemark());
        order.setOrderedAt(OffsetDateTime.now());
        order.setCreatedBy(SecurityUtils.getUserId());
        order.setCreatedAt(OffsetDateTime.now());
        order.setUpdatedBy(SecurityUtils.getUserId());
        order.setUpdatedAt(OffsetDateTime.now());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SaleOrderCreateRequest.Item itemReq : request.getItems()) {
            SaleOrderItem item = new SaleOrderItem();
            item.setId(UUID.randomUUID());
            item.setSaleOrderId(order.getId());
            item.setSkuId(itemReq.getSkuId());
            item.setSkuCode(itemReq.getSkuCode());
            item.setProductName(itemReq.getProductName());
            item.setUnit(itemReq.getUnit());
            item.setQuantity(safe(itemReq.getQuantity()));
            item.setShippedQuantity(BigDecimal.ZERO);
            item.setUnitPrice(itemReq.getUnitPrice());
            BigDecimal amount = itemReq.getAmount();
            if (amount == null && itemReq.getUnitPrice() != null && itemReq.getQuantity() != null) {
                amount = itemReq.getUnitPrice().multiply(itemReq.getQuantity());
            }
            item.setAmount(amount);
            item.setRemark(itemReq.getRemark());
            item.setCreatedAt(OffsetDateTime.now());
            saleOrderItemMapper.insert(item);
            if (amount != null) {
                totalAmount = totalAmount.add(amount);
            }
        }

        order.setTotalAmount(totalAmount);
        order.setPayableAmount(totalAmount
                .subtract(safe(order.getDiscountAmount()))
                .add(safe(order.getFreightAmount())));
        saleOrderMapper.insert(order);
        return toVO(order);
    }

    @Override
    @Transactional
    public SaleOrderVO updateDraft(UUID id, SaleOrderCreateRequest request) {
        SaleOrder order = getOrder(id);
        ensureStatus(order, "PENDING_CONFIRM");

        Customer customer = customerMapper.selectById(request.getCustomerId());
        if (customer == null) {
            throw new BizException(10004, "客户不存在");
        }
        order.setCustomerId(customer.getId());
        order.setCustomerName(customer.getName());
        order.setOrderSource(request.getOrderSource());
        order.setPlatformOrderNo(request.getPlatformOrderNo());
        order.setPlatformData(request.getPlatformData());
        order.setDiscountAmount(safe(request.getDiscountAmount()));
        order.setFreightAmount(safe(request.getFreightAmount()));
        order.setShippingAddress(request.getShippingAddress());
        order.setRemark(request.getRemark());
        order.setUpdatedBy(SecurityUtils.getUserId());
        order.setUpdatedAt(OffsetDateTime.now());

        // Replace items
        List<SaleOrderItem> existing = saleOrderItemMapper.selectBySaleOrderId(order.getId());
        for (SaleOrderItem item : existing) {
            saleOrderItemMapper.deleteById(item.getId());
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SaleOrderCreateRequest.Item itemReq : request.getItems()) {
            SaleOrderItem item = new SaleOrderItem();
            item.setId(UUID.randomUUID());
            item.setSaleOrderId(order.getId());
            item.setSkuId(itemReq.getSkuId());
            item.setSkuCode(itemReq.getSkuCode());
            item.setProductName(itemReq.getProductName());
            item.setUnit(itemReq.getUnit());
            item.setQuantity(safe(itemReq.getQuantity()));
            item.setShippedQuantity(BigDecimal.ZERO);
            item.setUnitPrice(itemReq.getUnitPrice());
            BigDecimal amount = itemReq.getAmount();
            if (amount == null && itemReq.getUnitPrice() != null && itemReq.getQuantity() != null) {
                amount = itemReq.getUnitPrice().multiply(itemReq.getQuantity());
            }
            item.setAmount(amount);
            item.setRemark(itemReq.getRemark());
            item.setCreatedAt(OffsetDateTime.now());
            saleOrderItemMapper.insert(item);
            if (amount != null) {
                totalAmount = totalAmount.add(amount);
            }
        }

        order.setTotalAmount(totalAmount);
        order.setPayableAmount(totalAmount
                .subtract(safe(order.getDiscountAmount()))
                .add(safe(order.getFreightAmount())));
        saleOrderMapper.updateById(order);
        return toVO(order);
    }

    @Override
    @Transactional
    public SaleOrderVO changeStatus(UUID id, SaleOrderStatusRequest request) {
        SaleOrder order = getOrder(id);
        String action = request.getAction();
        switch (action) {
            case "confirm" -> {
                ensureStatus(order, "PENDING_CONFIRM");
                order.setStatus("CONFIRMED");
            }
            case "cancel" -> {
                ensureStatus(order, "PENDING_CONFIRM");
                order.setStatus("CANCELLED");
            }
            case "complete" -> {
                ensureStatus(order, "SHIPPED");
                order.setStatus("COMPLETED");
                order.setCompletedAt(OffsetDateTime.now());
            }
            case "requestReturn" -> {
                if (!"SHIPPED".equals(order.getStatus()) && !"COMPLETED".equals(order.getStatus())) {
                    throw new BizException(10004, "当前订单状态不允许申请退货");
                }
                order.setStatus("RETURN_REQUEST");
            }
            default -> throw new BizException(10004, "不支持的销售订单操作: " + action);
        }
        if (request.getRemark() != null && !request.getRemark().isBlank()) {
            order.setRemark(request.getRemark());
        }
        order.setUpdatedBy(SecurityUtils.getUserId());
        order.setUpdatedAt(OffsetDateTime.now());
        saleOrderMapper.updateById(order);
        return toVO(order);
    }

    @Override
    @Transactional
    public SaleOrderVO ship(UUID id, ShippingOrderRequest request) {
        SaleOrder order = getOrder(id);
        if (!"CONFIRMED".equals(order.getStatus()) && !"PENDING_SHIP".equals(order.getStatus())) {
            throw new BizException(10004, "当前订单状态不允许发货");
        }

        // Create shipping order
        ShippingOrder shipping = new ShippingOrder();
        shipping.setId(UUID.randomUUID());
        shipping.setSaleOrderId(order.getId());
        shipping.setCarrierCode(request.getCarrierCode());
        shipping.setCarrierName(request.getCarrierName());
        shipping.setTrackingNumber(request.getTrackingNumber());
        shipping.setStatus("SHIPPED");
        shipping.setShippedAt(OffsetDateTime.now());
        shipping.setRemark(request.getRemark());
        shipping.setCreatedBy(SecurityUtils.getUserId());
        shipping.setCreatedAt(OffsetDateTime.now());
        shipping.setUpdatedBy(SecurityUtils.getUserId());
        shipping.setUpdatedAt(OffsetDateTime.now());
        shippingOrderMapper.insert(shipping);

        // Update order items shipped quantity
        List<SaleOrderItem> items = saleOrderItemMapper.selectBySaleOrderId(order.getId());
        for (SaleOrderItem item : items) {
            item.setShippedQuantity(item.getQuantity());
            saleOrderItemMapper.updateById(item);

            // Inventory outbound: create SALE_OUT transaction
            try {
                createOutboundTransaction(order, item);
            } catch (BizException ex) {
                saleExceptionService.createOrderException(order, item, "SHIPMENT", ex.getMessage());
                throw ex;
            }
        }

        order.setStatus("SHIPPED");
        order.setShippedAt(OffsetDateTime.now());
        order.setUpdatedBy(SecurityUtils.getUserId());
        order.setUpdatedAt(OffsetDateTime.now());
        saleOrderMapper.updateById(order);
        return toVO(order);
    }

    @Override
    public PageVO<ShippingVO> listShipping(long pageNum, long pageSize) {
        Page<ShippingOrder> page = shippingOrderMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ShippingOrder>().orderByDesc(ShippingOrder::getCreatedAt)
        );
        List<ShippingVO> records = page.getRecords().stream().map(this::toShippingVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public ShippingVO shippingDetail(UUID id) {
        ShippingOrder shipping = shippingOrderMapper.selectById(id);
        if (shipping == null) {
            throw new BizException(10006, "发货单不存在");
        }
        return toShippingVO(shipping);
    }

    @Override
    public PageVO<SaleReceivableStatVO> listReceivableStats(long pageNum, long pageSize) {
        List<SaleOrder> orders = saleOrderMapper.selectList(
                new LambdaQueryWrapper<SaleOrder>()
                        .notIn(SaleOrder::getStatus, List.of("PENDING_CONFIRM", "CANCELLED"))
        );

        Map<UUID, List<SaleOrder>> ordersByCustomer = orders.stream()
                .filter(order -> order.getCustomerId() != null)
                .collect(Collectors.groupingBy(SaleOrder::getCustomerId));

        List<SaleReturn> returns = saleReturnMapper.selectList(
                new LambdaQueryWrapper<SaleReturn>()
                        .in(SaleReturn::getStatus, List.of("REFUNDED", "COMPLETED"))
        );
        Map<UUID, List<SaleReturn>> returnsByCustomer = returns.stream()
                .filter(saleReturn -> saleReturn.getCustomerId() != null)
                .collect(Collectors.groupingBy(SaleReturn::getCustomerId));

        List<SaleReceivableStatVO> stats = ordersByCustomer.entrySet().stream().map(entry -> {
            UUID customerId = entry.getKey();
            List<SaleOrder> customerOrders = entry.getValue();
            List<SaleReturn> customerReturns = returnsByCustomer.getOrDefault(customerId, List.of());

            BigDecimal orderAmount = customerOrders.stream()
                    .map(SaleOrder::getPayableAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal returnAmount = customerReturns.stream()
                    .map(SaleReturn::getTotalAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            SaleReceivableStatVO vo = new SaleReceivableStatVO();
            vo.setCustomerId(customerId);
            vo.setCustomerName(customerOrders.get(0).getCustomerName());
            vo.setOrderAmount(orderAmount);
            vo.setReturnAmount(returnAmount);
            vo.setNetReceivableAmount(orderAmount.subtract(returnAmount));
            vo.setOrderCount((long) customerOrders.size());
            vo.setReturnCount((long) customerReturns.size());
            return vo;
        }).toList();

        int fromIndex = (int) Math.max((pageNum - 1) * pageSize, 0);
        int toIndex = (int) Math.min(fromIndex + pageSize, stats.size());
        List<SaleReceivableStatVO> records = fromIndex >= stats.size() ? List.of() : stats.subList(fromIndex, toIndex);
        return new PageVO<>(records, (long) stats.size(), pageNum, pageSize);
    }

    // ========== Inventory outbound integration ==========

    private void createOutboundTransaction(SaleOrder order, SaleOrderItem item) {
        BigDecimal quantity = safe(item.getQuantity());
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(10004, "发货数量必须大于0");
        }
        InventoryTransaction txn = new InventoryTransaction();
        txn.setId(UUID.randomUUID());
        txn.setTransactionType("SALE_OUT");
        txn.setQuantity(quantity.negate());
        txn.setSourceType("SALE");
        txn.setSourceOrderId(order.getId());
        txn.setSourceOrderNo(order.getOrderNo());
        txn.setSourceItemId(item.getId());
        txn.setRemark("销售出库: " + order.getOrderNo());
        txn.setCreatedBy(SecurityUtils.getUserId());
        txn.setCreatedAt(OffsetDateTime.now());

        ProductionProductStock productStock = findProductStock(item);
        if (productStock != null) {
            BigDecimal currentStock = safe(productStock.getCurrentStock());
            if (currentStock.compareTo(quantity) < 0) {
                throw new BizException(10004, "成品库存不足: " + productStock.getProductName());
            }
            BigDecimal newStock = currentStock.subtract(quantity);
            productStock.setCurrentStock(newStock);
            productStock.setUpdatedBy(SecurityUtils.getUserId());
            productStock.setUpdatedAt(OffsetDateTime.now());
            productStockMapper.updateById(productStock);
            txn.setMaterialId(productStock.getProductId());
            txn.setMaterialCode(productStock.getProductCode());
            txn.setMaterialName(productStock.getProductName());
            txn.setBalanceAfter(newStock);
            inventoryTransactionMapper.insert(txn);
            return;
        }

        Material material = findMaterialFallback(item);
        if (material == null) {
            throw new BizException(10004, "未找到可扣减库存的成品或原料: " + item.getSkuCode());
        }
        BigDecimal currentStock = safe(material.getCurrentStock());
        if (currentStock.compareTo(quantity) < 0) {
            throw new BizException(10004, "原料库存不足: " + material.getName());
        }
        BigDecimal newStock = currentStock.subtract(quantity);
        material.setCurrentStock(newStock);
        material.setUpdatedBy(SecurityUtils.getUserId());
        material.setUpdatedAt(OffsetDateTime.now());
        materialMapper.updateById(material);
        txn.setMaterialId(material.getId());
        txn.setMaterialCode(material.getCode());
        txn.setMaterialName(material.getName());
        txn.setBalanceAfter(newStock);
        inventoryTransactionMapper.insert(txn);
    }

    private ProductionProductStock findProductStock(SaleOrderItem item) {
        if (item.getSkuId() != null) {
            ProductionProductStock byProductId = productStockMapper.selectOne(
                    new LambdaQueryWrapper<ProductionProductStock>().eq(ProductionProductStock::getProductId, item.getSkuId()));
            if (byProductId != null) {
                return byProductId;
            }
        }
        if (item.getSkuCode() != null && !item.getSkuCode().isBlank()) {
            return productStockMapper.selectOne(
                    new LambdaQueryWrapper<ProductionProductStock>().eq(ProductionProductStock::getProductCode, item.getSkuCode()));
        }
        return null;
    }

    private Material findMaterialFallback(SaleOrderItem item) {
        if (item.getSkuCode() == null || item.getSkuCode().isBlank()) {
            return null;
        }
        List<Material> candidates = materialMapper.selectList(
                new LambdaQueryWrapper<Material>().eq(Material::getCode, item.getSkuCode()));
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    // ========== Helpers ==========

    private SaleOrder getOrder(UUID id) {
        SaleOrder order = saleOrderMapper.selectById(id);
        if (order == null) {
            throw new BizException(10006, "销售订单不存在");
        }
        return order;
    }

    private void ensureStatus(SaleOrder order, String expectedStatus) {
        if (!expectedStatus.equals(order.getStatus())) {
            throw new BizException(10004, "当前订单状态不允许该操作（当前: " + order.getStatus() + "）");
        }
    }

    private SaleOrderVO toVO(SaleOrder order) {
        SaleOrderVO vo = new SaleOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setCustomerId(order.getCustomerId());
        vo.setCustomerName(order.getCustomerName());
        vo.setOrderSource(order.getOrderSource());
        vo.setPlatformOrderNo(order.getPlatformOrderNo());
        vo.setPlatformData(order.getPlatformData());
        vo.setStatus(order.getStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setFreightAmount(order.getFreightAmount());
        vo.setPayableAmount(order.getPayableAmount());
        vo.setPaidAmount(order.getPaidAmount());
        vo.setPaymentStatus(order.getPaymentStatus());
        vo.setShippingAddress(order.getShippingAddress());
        vo.setRemark(order.getRemark());
        vo.setOrderedAt(order.getOrderedAt());
        vo.setPaidAt(order.getPaidAt());
        vo.setShippedAt(order.getShippedAt());
        vo.setCompletedAt(order.getCompletedAt());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setUpdatedAt(order.getUpdatedAt());
        vo.setItems(saleOrderItemMapper.selectBySaleOrderId(order.getId()).stream().map(this::toItemVO).toList());
        vo.setShippingOrders(shippingOrderMapper.selectBySaleOrderId(order.getId()).stream().map(this::toShippingVO).toList());
        return vo;
    }

    private SaleOrderItemVO toItemVO(SaleOrderItem item) {
        SaleOrderItemVO vo = new SaleOrderItemVO();
        vo.setId(item.getId());
        vo.setSaleOrderId(item.getSaleOrderId());
        vo.setSkuId(item.getSkuId());
        vo.setSkuCode(item.getSkuCode());
        vo.setProductName(item.getProductName());
        vo.setUnit(item.getUnit());
        vo.setQuantity(item.getQuantity());
        vo.setShippedQuantity(item.getShippedQuantity());
        vo.setUnitPrice(item.getUnitPrice());
        vo.setAmount(item.getAmount());
        vo.setRemark(item.getRemark());
        return vo;
    }

    private ShippingVO toShippingVO(ShippingOrder shipping) {
        ShippingVO vo = new ShippingVO();
        vo.setId(shipping.getId());
        vo.setSaleOrderId(shipping.getSaleOrderId());
        vo.setCarrierCode(shipping.getCarrierCode());
        vo.setCarrierName(shipping.getCarrierName());
        vo.setTrackingNumber(shipping.getTrackingNumber());
        vo.setStatus(shipping.getStatus());
        vo.setShippedAt(shipping.getShippedAt());
        vo.setReceivedAt(shipping.getReceivedAt());
        vo.setRemark(shipping.getRemark());
        vo.setCreatedAt(shipping.getCreatedAt());
        vo.setUpdatedAt(shipping.getUpdatedAt());
        return vo;
    }

    private String generateOrderNo(String prefix) {
        return prefix + "-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
