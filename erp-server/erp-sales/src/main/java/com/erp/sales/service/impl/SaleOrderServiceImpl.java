package com.erp.sales.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.report.excel.ExcelExportUtils;
import com.erp.common.security.util.SecurityUtils;
import com.erp.inventory.domain.entity.InventoryTransaction;
import com.erp.product.domain.entity.ProductSku;
import com.erp.product.mapper.ProductSkuMapper;
import com.erp.production.domain.entity.ProductionProductStock;
import com.erp.inventory.mapper.InventoryTransactionMapper;
import com.erp.production.mapper.ProductionProductStockMapper;
import com.erp.production.service.ProductionSerialNumberService;
import com.erp.sales.domain.SaleOrderStatusMachine;
import com.erp.sales.domain.dto.SaleOrderCreateRequest;
import com.erp.sales.domain.dto.SaleOrderStatusRequest;
import com.erp.sales.domain.dto.ShippingOrderRequest;
import com.erp.sales.domain.entity.Customer;
import com.erp.sales.domain.entity.SaleException;
import com.erp.sales.domain.entity.SaleOrder;
import com.erp.sales.domain.entity.SaleOrderItem;
import com.erp.sales.domain.entity.SaleReturn;
import com.erp.sales.domain.entity.ShippingOrder;
import com.erp.sales.domain.entity.ShippingOrderItem;
import com.erp.sales.domain.vo.SaleOrderItemVO;
import com.erp.sales.domain.vo.SaleOrderVO;
import com.erp.sales.domain.vo.SaleReceivableStatVO;
import com.erp.sales.domain.vo.ShippingItemVO;
import com.erp.sales.domain.vo.ShippingVO;
import com.erp.sales.mapper.CustomerMapper;
import com.erp.sales.mapper.SaleExceptionMapper;
import com.erp.sales.mapper.SaleOrderItemMapper;
import com.erp.sales.mapper.SaleOrderMapper;
import com.erp.sales.mapper.SaleReturnMapper;
import com.erp.sales.mapper.ShippingOrderItemMapper;
import com.erp.sales.mapper.ShippingOrderMapper;
import com.erp.sales.service.SaleExceptionService;
import com.erp.sales.service.SaleOrderService;
import com.erp.system.service.SysParamService;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaleOrderServiceImpl implements SaleOrderService {
    private static final Logger log = LoggerFactory.getLogger(SaleOrderServiceImpl.class);
    private static final String SHIPPING_PENDING_REVIEW = "PENDING_REVIEW";
    private static final String SHIPPING_SHIPPED = "SHIPPED";
    private static final String SHIPPING_CANCELLED = "CANCELLED";
    /** 系统参数：订单确认/发货时是否校验并预占库存（默认开启） */
    private static final String PARAM_RESERVE_STOCK = "sale_order.confirm_reserve_stock";

    private final SaleOrderMapper saleOrderMapper;
    private final SaleOrderItemMapper saleOrderItemMapper;
    private final ShippingOrderMapper shippingOrderMapper;
    private final ShippingOrderItemMapper shippingOrderItemMapper;
    private final SaleReturnMapper saleReturnMapper;
    private final CustomerMapper customerMapper;
    private final ProductSkuMapper productSkuMapper;
    private final ProductionProductStockMapper productStockMapper;
    private final InventoryTransactionMapper inventoryTransactionMapper;
    private final SaleExceptionService saleExceptionService;
    private final SaleExceptionMapper saleExceptionMapper;
    private final SysParamService sysParamService;
    private final ProductionSerialNumberService serialNumberService;

    public SaleOrderServiceImpl(
            SaleOrderMapper saleOrderMapper,
            SaleOrderItemMapper saleOrderItemMapper,
            ShippingOrderMapper shippingOrderMapper,
            ShippingOrderItemMapper shippingOrderItemMapper,
            SaleReturnMapper saleReturnMapper,
            CustomerMapper customerMapper,
            ProductSkuMapper productSkuMapper,
            ProductionProductStockMapper productStockMapper,
            InventoryTransactionMapper inventoryTransactionMapper,
            SaleExceptionService saleExceptionService,
            SaleExceptionMapper saleExceptionMapper,
            SysParamService sysParamService,
            ProductionSerialNumberService serialNumberService
    ) {
        this.saleOrderMapper = saleOrderMapper;
        this.saleOrderItemMapper = saleOrderItemMapper;
        this.shippingOrderMapper = shippingOrderMapper;
        this.shippingOrderItemMapper = shippingOrderItemMapper;
        this.saleReturnMapper = saleReturnMapper;
        this.customerMapper = customerMapper;
        this.productSkuMapper = productSkuMapper;
        this.productStockMapper = productStockMapper;
        this.inventoryTransactionMapper = inventoryTransactionMapper;
        this.saleExceptionService = saleExceptionService;
        this.saleExceptionMapper = saleExceptionMapper;
        this.sysParamService = sysParamService;
        this.serialNumberService = serialNumberService;
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
        order.setStatus(SaleOrderStatusMachine.PENDING_CONFIRM);
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
        order.setTotalAmount(BigDecimal.ZERO);
        order.setPayableAmount(safe(order.getFreightAmount()).subtract(safe(order.getDiscountAmount())));
        saleOrderMapper.insert(order);

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
    public SaleOrderVO updateDraft(UUID id, SaleOrderCreateRequest request) {
        SaleOrder order = getOrder(id);
        SaleOrderStatusMachine.ensureCanEdit(order.getStatus());

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

        // Diff 策略更新明细：
        // - 产品或数量未变的行（按提交的 item id 命中且 skuId+quantity 不变）：原地更新单价等，保留明细行与其引用的异常。
        // - 产品或数量变化的行（含被删除的行）：先级联清理引用该明细的异常，再删明细；新增行直接插入。
        // 这样只有真正改动了产品/数量的明细才会连带清理异常，其余异常得以保留。
        List<SaleOrderItem> existing = saleOrderItemMapper.selectBySaleOrderId(order.getId());
        Map<UUID, SaleOrderItem> existingById = existing.stream()
                .collect(Collectors.toMap(SaleOrderItem::getId, i -> i, (a, b) -> a));
        Set<UUID> submittedIds = request.getItems().stream()
                .map(SaleOrderCreateRequest.Item::getId)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        // 删除：被移除的明细（未出现在提交列表），先级联清理引用它的异常
        for (SaleOrderItem old : existing) {
            if (!submittedIds.contains(old.getId())) {
                saleExceptionMapper.delete(new LambdaQueryWrapper<SaleException>()
                        .eq(SaleException::getSaleOrderItemId, old.getId()));
                saleOrderItemMapper.deleteById(old.getId());
            }
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        OffsetDateTime now = OffsetDateTime.now();
        for (SaleOrderCreateRequest.Item itemReq : request.getItems()) {
            BigDecimal amount = itemReq.getAmount();
            if (amount == null && itemReq.getUnitPrice() != null && itemReq.getQuantity() != null) {
                amount = itemReq.getUnitPrice().multiply(itemReq.getQuantity());
            }

            SaleOrderItem matched = itemReq.getId() != null ? existingById.get(itemReq.getId()) : null;
            boolean productOrQtyChanged = matched == null
                    || !java.util.Objects.equals(matched.getSkuId(), itemReq.getSkuId())
                    || matched.getQuantity().compareTo(safe(itemReq.getQuantity())) != 0;

            if (matched != null && !productOrQtyChanged) {
                // 保留明细：仅更新单价/备注/金额等，不重建，异常引用保留
                matched.setSkuId(itemReq.getSkuId());
                matched.setSkuCode(itemReq.getSkuCode());
                matched.setProductName(itemReq.getProductName());
                matched.setUnit(itemReq.getUnit());
                matched.setUnitPrice(itemReq.getUnitPrice());
                matched.setAmount(amount);
                matched.setRemark(itemReq.getRemark());
                saleOrderItemMapper.updateById(matched);
            } else {
                // 产品或数量变化：删除旧明细（级联清异常）后插入新明细；或直接新增
                if (matched != null) {
                    saleExceptionMapper.delete(new LambdaQueryWrapper<SaleException>()
                            .eq(SaleException::getSaleOrderItemId, matched.getId()));
                    saleOrderItemMapper.deleteById(matched.getId());
                }
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
                item.setAmount(amount);
                item.setRemark(itemReq.getRemark());
                item.setCreatedAt(now);
                saleOrderItemMapper.insert(item);
            }
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
        String nextStatus = SaleOrderStatusMachine.next(order.getStatus(), action);
        List<SaleOrderItem> items = getOrderItems(order.getId());
        if (SaleOrderStatusMachine.ACTION_CONFIRM.equals(action)) {
            // 系统参数控制：关闭时确认不校验、不锁定库存，订单直接确认进入待发货
            boolean reserveStock = sysParamService.getBoolean(PARAM_RESERVE_STOCK, true);
            if (reserveStock) {
                reserveOrderStock(order, items);
            }
        }
        if (SaleOrderStatusMachine.ACTION_CANCEL.equals(action)) {
            cancelPendingShippingOrders(order);
            releaseOrderReservation(order, items);
        }
        order.setStatus(nextStatus);
        if (SaleOrderStatusMachine.COMPLETED.equals(nextStatus)) {
            order.setCompletedAt(OffsetDateTime.now());
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
        SaleOrderStatusMachine.ensureCanShip(order.getStatus());
        ShippingOrderRequest actualRequest = request == null ? new ShippingOrderRequest() : request;
        List<SaleOrderItem> items = getOrderItems(order.getId());
        if (SaleOrderStatusMachine.CONFIRMED.equals(order.getStatus())) {
            // 系统参数控制：关闭时发货不校验、不预占库存（与确认逻辑一致）
            boolean reserveStock = sysParamService.getBoolean(PARAM_RESERVE_STOCK, true);
            if (reserveStock) {
                reserveOrderStock(order, items);
            }
            order.setStatus(SaleOrderStatusMachine.PENDING_SHIP);
        }
        Map<UUID, BigDecimal> pendingQuantities = pendingShippingQuantities(order.getId());
        List<ShippingLine> shippingLines = resolveShippingLines(actualRequest, items, pendingQuantities);

        OffsetDateTime now = OffsetDateTime.now();
        ShippingOrder shipping = new ShippingOrder();
        shipping.setId(UUID.randomUUID());
        shipping.setSaleOrderId(order.getId());
        shipping.setCarrierCode(actualRequest.getCarrierCode());
        shipping.setCarrierName(actualRequest.getCarrierName());
        shipping.setTrackingNumber(actualRequest.getTrackingNumber());
        shipping.setStatus(SHIPPING_PENDING_REVIEW);
        shipping.setRemark(actualRequest.getRemark());
        shipping.setCreatedBy(SecurityUtils.getUserId());
        shipping.setCreatedAt(now);
        shipping.setUpdatedBy(SecurityUtils.getUserId());
        shipping.setUpdatedAt(now);
        shippingOrderMapper.insert(shipping);

        for (ShippingLine line : shippingLines) {
            ShippingOrderItem shippingItem = new ShippingOrderItem();
            shippingItem.setId(UUID.randomUUID());
            shippingItem.setShippingOrderId(shipping.getId());
            shippingItem.setSaleOrderItemId(line.item().getId());
            shippingItem.setSkuId(line.item().getSkuId());
            shippingItem.setSkuCode(line.item().getSkuCode());
            shippingItem.setProductName(line.item().getProductName());
            shippingItem.setQuantity(line.quantity());
            shippingItem.setSerialNos(joinSerialNos(line.serialNos()));
            shippingItem.setCreatedAt(now);
            shippingOrderItemMapper.insert(shippingItem);
        }

        if (!SaleOrderStatusMachine.PARTIAL_SHIPPED.equals(order.getStatus())) {
            order.setStatus(SaleOrderStatusMachine.PENDING_SHIP);
        }
        order.setUpdatedBy(SecurityUtils.getUserId());
        order.setUpdatedAt(now);
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
    public ByteArrayInputStream exportShipping() {
        List<ShippingVO> records = shippingOrderMapper.selectList(new LambdaQueryWrapper<ShippingOrder>().orderByDesc(ShippingOrder::getCreatedAt))
                .stream()
                .map(this::toShippingVO)
                .collect(Collectors.toList());
        return ExcelExportUtils.export("shipping-orders", List.of(
                ExcelExportUtils.column("销售订单ID", ShippingVO::getSaleOrderId),
                ExcelExportUtils.column("承运商编码", ShippingVO::getCarrierCode),
                ExcelExportUtils.column("承运商", ShippingVO::getCarrierName),
                ExcelExportUtils.column("运单号", ShippingVO::getTrackingNumber),
                ExcelExportUtils.column("状态", ShippingVO::getStatus),
                ExcelExportUtils.column("发货数量", this::shippingQuantity),
                ExcelExportUtils.column("发货时间", ShippingVO::getShippedAt),
                ExcelExportUtils.column("签收时间", ShippingVO::getReceivedAt),
                ExcelExportUtils.column("备注", ShippingVO::getRemark),
                ExcelExportUtils.column("创建时间", ShippingVO::getCreatedAt),
                ExcelExportUtils.column("更新时间", ShippingVO::getUpdatedAt)
        ), records, "发货单导出失败");
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
    @Transactional
    public ShippingVO reviewShipping(UUID id) {
        ShippingOrder shipping = shippingOrderMapper.selectById(id);
        if (shipping == null) {
            throw new BizException(10006, "发货单不存在");
        }
        if (!SHIPPING_PENDING_REVIEW.equals(shipping.getStatus())) {
            throw new BizException(10004, "只有待复核发货单允许复核出库");
        }

        SaleOrder order = getOrder(shipping.getSaleOrderId());
        List<ShippingOrderItem> shippingItems = shippingOrderItemMapper.selectByShippingOrderId(shipping.getId());
        if (shippingItems == null || shippingItems.isEmpty()) {
            throw new BizException(10004, "发货单没有可复核明细");
        }

        for (ShippingOrderItem shippingItem : shippingItems) {
            SaleOrderItem item = saleOrderItemMapper.selectById(shippingItem.getSaleOrderItemId());
            if (item == null) {
                throw new BizException(10006, "销售订单明细不存在: " + shippingItem.getSaleOrderItemId());
            }
            try {
                UUID productId = createOutboundTransaction(order, item, shippingItem.getQuantity());
                serialNumberService.markShipped(productId, splitSerialNos(shippingItem.getSerialNos()), OffsetDateTime.now());
            } catch (BizException ex) {
                saleExceptionService.createOrderException(order, item, "SHIPMENT_REVIEW", ex.getMessage());
                throw ex;
            }
            item.setShippedQuantity(safe(item.getShippedQuantity()).add(shippingItem.getQuantity()));
            saleOrderItemMapper.updateById(item);
        }

        OffsetDateTime now = OffsetDateTime.now();
        shipping.setStatus(SHIPPING_SHIPPED);
        shipping.setShippedAt(now);
        shipping.setUpdatedBy(SecurityUtils.getUserId());
        shipping.setUpdatedAt(now);
        shippingOrderMapper.updateById(shipping);

        List<SaleOrderItem> orderItems = getOrderItems(order.getId());
        boolean allShipped = orderItems.stream()
                .allMatch(item -> safe(item.getShippedQuantity()).compareTo(safe(item.getQuantity())) >= 0);
        order.setStatus(allShipped ? SaleOrderStatusMachine.SHIPPED : SaleOrderStatusMachine.PARTIAL_SHIPPED);
        if (allShipped) {
            order.setShippedAt(now);
        }
        order.setUpdatedBy(SecurityUtils.getUserId());
        order.setUpdatedAt(now);
        saleOrderMapper.updateById(order);
        return toShippingVO(shipping);
    }

    @Override
    public PageVO<SaleReceivableStatVO> listReceivableStats(long pageNum, long pageSize) {
        List<SaleOrder> orders = saleOrderMapper.selectList(
                new LambdaQueryWrapper<SaleOrder>()
                        .notIn(SaleOrder::getStatus, List.of(SaleOrderStatusMachine.PENDING_CONFIRM, SaleOrderStatusMachine.CANCELLED))
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

            BigDecimal receivedAmount = customerOrders.stream()
                    .map(order -> order.getPaidAmount() == null ? BigDecimal.ZERO : order.getPaidAmount())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            vo.setReceivedAmount(receivedAmount);
            vo.setUnreceivedAmount(orderAmount.subtract(returnAmount).subtract(receivedAmount));

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

    private UUID createOutboundTransaction(SaleOrder order, SaleOrderItem item, BigDecimal quantity) {
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
        OffsetDateTime now = OffsetDateTime.now();
        // 库存校验开关：开启时走预占扣减（校验 current + reserved）；关闭时不卡库存，
        // 有库存记录则仅扣 current，无库存记录则只记出库流水、不阻断发货。
        boolean reserveStock = sysParamService.getBoolean(PARAM_RESERVE_STOCK, true);
        if (productStock != null) {
            BigDecimal currentStock = safe(productStock.getCurrentStock());
            BigDecimal reservedStock = safe(productStock.getReservedStock());
            if (reserveStock) {
                if (currentStock.compareTo(quantity) < 0) {
                    throw new BizException(10004, "成品库存不足: " + productStock.getProductName());
                }
                if (reservedStock.compareTo(quantity) < 0) {
                    throw new BizException(10004, "成品占用库存不足: " + productStock.getProductName());
                }
                int updatedRows = productStockMapper.decreaseReservedIfEnough(productStock.getId(), quantity, SecurityUtils.getUserId(), now);
                if (updatedRows == 0) {
                    throw new BizException(10004, "成品库存不足或已被其他发货作业占用: " + productStock.getProductName());
                }
            } else {
                int updatedRows = productStockMapper.decreaseCurrentIfEnough(productStock.getId(), quantity, SecurityUtils.getUserId(), now);
                if (updatedRows == 0) {
                    // 库存不足时也允许出库，current_stock 扣到负数（仅记流水，不阻断业务流程）
                    productStockMapper.decreaseCurrentForce(productStock.getId(), quantity, SecurityUtils.getUserId(), now);
                }
            }
            BigDecimal newStock = currentStock.subtract(quantity);
            BigDecimal newReservedStock = reserveStock ? reservedStock.subtract(quantity) : reservedStock;
            productStock.setCurrentStock(newStock);
            productStock.setReservedStock(newReservedStock);
            productStock.setUpdatedBy(SecurityUtils.getUserId());
            productStock.setUpdatedAt(now);
            txn.setMaterialId(productStock.getProductId());
            txn.setMaterialCode(productStock.getProductCode());
            txn.setMaterialName(productStock.getProductName());
            txn.setBalanceBefore(currentStock);
            txn.setBalanceAfter(newStock);
            inventoryTransactionMapper.insert(txn);
            return productStock.getProductId();
        }

        // 开启库存校验时，无库存记录不允许出库
        if (reserveStock) {
            throw new BizException(10004, "未找到可扣减库存的成品库存: " + item.getSkuCode());
        }
        // 关闭库存校验时，无库存记录也允许出库：仅记出库流水，productId 从 SKU 反查
        ProductSku sku = item.getSkuId() != null ? productSkuMapper.selectById(item.getSkuId()) : null;
        UUID productId = sku != null ? sku.getProductId() : null;
        txn.setMaterialId(productId);
        txn.setMaterialCode(item.getSkuCode());
        txn.setMaterialName(item.getProductName());
        txn.setBalanceBefore(BigDecimal.ZERO);
        txn.setBalanceAfter(BigDecimal.ZERO.subtract(quantity));
        inventoryTransactionMapper.insert(txn);
        return productId;
    }

    private void reserveOrderStock(SaleOrder order, List<SaleOrderItem> items) {
        if (items.isEmpty()) {
            throw new BizException(10004, "销售订单没有可预占明细");
        }
        Map<UUID, BigDecimal> pendingQuantities = pendingShippingQuantities(order.getId());
        for (SaleOrderItem item : items) {
            BigDecimal quantity = remainingQuantity(item).subtract(pendingQuantities.getOrDefault(item.getId(), BigDecimal.ZERO));
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            try {
                reserveProductStock(item, quantity);
            } catch (BizException ex) {
                saleExceptionService.createOrderException(order, item, "STOCK_RESERVE", ex.getMessage());
                throw ex;
            }
        }
    }

    private void reserveProductStock(SaleOrderItem item, BigDecimal quantity) {
        ProductionProductStock productStock = findProductStock(item);
        if (productStock == null) {
            throw new BizException(10004, "未找到可预占库存的成品库存: " + item.getSkuCode());
        }
        BigDecimal currentStock = safe(productStock.getCurrentStock());
        BigDecimal reservedStock = safe(productStock.getReservedStock());
        BigDecimal availableStock = currentStock.subtract(reservedStock);
        if (availableStock.compareTo(quantity) < 0) {
            throw new BizException(10004, "成品可用库存不足: " + productStock.getProductName());
        }
        productStock.setReservedStock(reservedStock.add(quantity));
        productStock.setUpdatedBy(SecurityUtils.getUserId());
        productStock.setUpdatedAt(OffsetDateTime.now());
        productStockMapper.updateById(productStock);
    }

    private void releaseOrderReservation(SaleOrder order, List<SaleOrderItem> items) {
        for (SaleOrderItem item : items) {
            BigDecimal quantity = remainingQuantity(item);
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            releaseProductStock(item, quantity);
        }
    }

    private void releaseProductStock(SaleOrderItem item, BigDecimal quantity) {
        ProductionProductStock productStock = findProductStock(item);
        if (productStock == null) {
            return;
        }
        BigDecimal reservedStock = safe(productStock.getReservedStock());
        BigDecimal releaseQuantity = reservedStock.min(quantity);
        if (releaseQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        productStock.setReservedStock(reservedStock.subtract(releaseQuantity));
        productStock.setUpdatedBy(SecurityUtils.getUserId());
        productStock.setUpdatedAt(OffsetDateTime.now());
        productStockMapper.updateById(productStock);
    }

    private void cancelPendingShippingOrders(SaleOrder order) {
        List<ShippingOrder> shippings = shippingOrderMapper.selectBySaleOrderId(order.getId());
        if (shippings == null || shippings.isEmpty()) {
            return;
        }
        OffsetDateTime now = OffsetDateTime.now();
        for (ShippingOrder shipping : shippings) {
            if (SHIPPING_PENDING_REVIEW.equals(shipping.getStatus())) {
                shipping.setStatus(SHIPPING_CANCELLED);
                shipping.setUpdatedBy(SecurityUtils.getUserId());
                shipping.setUpdatedAt(now);
                shippingOrderMapper.updateById(shipping);
            }
        }
    }

    private Map<UUID, BigDecimal> pendingShippingQuantities(UUID saleOrderId) {
        List<ShippingOrder> shippings = shippingOrderMapper.selectBySaleOrderId(saleOrderId);
        if (shippings == null || shippings.isEmpty()) {
            return Map.of();
        }
        return shippings.stream()
                .filter(shipping -> SHIPPING_PENDING_REVIEW.equals(shipping.getStatus()))
                .flatMap(shipping -> {
                    List<ShippingOrderItem> shippingItems = shippingOrderItemMapper.selectByShippingOrderId(shipping.getId());
                    return shippingItems == null ? List.<ShippingOrderItem>of().stream() : shippingItems.stream();
                })
                .collect(Collectors.groupingBy(
                        ShippingOrderItem::getSaleOrderItemId,
                        Collectors.mapping(ShippingOrderItem::getQuantity, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))
                ));
    }

    private List<ShippingLine> resolveShippingLines(ShippingOrderRequest request, List<SaleOrderItem> items, Map<UUID, BigDecimal> pendingQuantities) {
        if (items.isEmpty()) {
            throw new BizException(10004, "销售订单没有可发货明细");
        }

        List<ShippingLine> lines = new ArrayList<>();
        if (request.getItems() == null || request.getItems().isEmpty()) {
            for (SaleOrderItem item : items) {
                BigDecimal remaining = remainingQuantity(item, pendingQuantities);
                if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                    lines.add(new ShippingLine(item, remaining, List.of()));
                }
            }
        } else {
            Map<UUID, SaleOrderItem> itemMap = items.stream()
                    .collect(Collectors.toMap(SaleOrderItem::getId, item -> item));
            Set<UUID> requestedItemIds = new HashSet<>();
            for (ShippingOrderRequest.Item requestItem : request.getItems()) {
                if (requestItem.getSaleOrderItemId() == null) {
                    throw new BizException(10004, "发货明细必须指定销售订单明细");
                }
                if (!requestedItemIds.add(requestItem.getSaleOrderItemId())) {
                    throw new BizException(10004, "发货明细重复: " + requestItem.getSaleOrderItemId());
                }
                SaleOrderItem item = itemMap.get(requestItem.getSaleOrderItemId());
                if (item == null) {
                    throw new BizException(10004, "销售订单明细不存在: " + requestItem.getSaleOrderItemId());
                }
                BigDecimal quantity = safe(requestItem.getQuantity());
                if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BizException(10004, "发货数量必须大于0");
                }
                BigDecimal remaining = remainingQuantity(item, pendingQuantities);
                if (quantity.compareTo(remaining) > 0) {
                    throw new BizException(10004, "发货数量不能超过未发数量: " + item.getSkuCode());
                }
                List<String> serialNos = normalizeSerialNos(requestItem.getSerialNos());
                validateSerialQuantity(quantity, serialNos, item.getSkuCode());
                lines.add(new ShippingLine(item, quantity, serialNos));
            }
        }

        if (lines.isEmpty()) {
            throw new BizException(10004, "销售订单没有剩余可发货数量");
        }
        return lines;
    }

    private BigDecimal remainingQuantity(SaleOrderItem item) {
        return safe(item.getQuantity()).subtract(safe(item.getShippedQuantity()));
    }

    private BigDecimal remainingQuantity(SaleOrderItem item, Map<UUID, BigDecimal> pendingQuantities) {
        return remainingQuantity(item).subtract(pendingQuantities.getOrDefault(item.getId(), BigDecimal.ZERO));
    }

    private List<SaleOrderItem> getOrderItems(UUID orderId) {
        List<SaleOrderItem> items = saleOrderItemMapper.selectBySaleOrderId(orderId);
        return items == null ? List.of() : items;
    }

    private ProductionProductStock findProductStock(SaleOrderItem item) {
        ProductSku sku = findSku(item.getSkuId(), item.getSkuCode());
        if (sku == null || sku.getProductId() == null) {
            throw new BizException(10004, "未找到销售明细对应的产品 SKU: " + item.getSkuCode());
        }
        return productStockMapper.selectOne(
                new LambdaQueryWrapper<ProductionProductStock>().eq(ProductionProductStock::getProductId, sku.getProductId()));
    }

    private ProductSku findSku(UUID skuId, String skuCode) {
        if (skuId != null) {
            ProductSku byId = productSkuMapper.selectById(skuId);
            if (byId != null) {
                return byId;
            }
        }
        if (skuCode != null && !skuCode.isBlank()) {
            return productSkuMapper.selectBySkuCode(skuCode);
        }
        return null;
    }

    private List<String> normalizeSerialNos(List<String> serialNos) {
        if (serialNos == null || serialNos.isEmpty()) {
            return List.of();
        }
        List<String> normalized = serialNos.stream()
                .filter(serialNo -> serialNo != null && !serialNo.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        if (normalized.size() != serialNos.stream().filter(serialNo -> serialNo != null && !serialNo.isBlank()).count()) {
            throw new BizException(10004, "发货序列号不能重复");
        }
        return normalized;
    }

    private void validateSerialQuantity(BigDecimal quantity, List<String> serialNos, String skuCode) {
        if (serialNos.isEmpty()) {
            return;
        }
        try {
            int expected = quantity.stripTrailingZeros().intValueExact();
            if (serialNos.size() != expected) {
                throw new BizException(10004, "发货序列号数量必须等于发货数量: " + skuCode);
            }
        } catch (ArithmeticException ex) {
            throw new BizException(10004, "填写序列号时发货数量必须为整数: " + skuCode);
        }
    }

    private String joinSerialNos(List<String> serialNos) {
        return serialNos == null || serialNos.isEmpty() ? null : String.join(",", serialNos);
    }

    private List<String> splitSerialNos(String serialNos) {
        if (serialNos == null || serialNos.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(serialNos.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private record ShippingLine(SaleOrderItem item, BigDecimal quantity, List<String> serialNos) {
    }

    // ========== Helpers ==========

    private SaleOrder getOrder(UUID id) {
        SaleOrder order = saleOrderMapper.selectById(id);
        if (order == null) {
            throw new BizException(10006, "销售订单不存在");
        }
        return order;
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
        List<SaleOrderItem> items = saleOrderItemMapper.selectBySaleOrderId(order.getId());
        List<ShippingOrder> shippingOrders = shippingOrderMapper.selectBySaleOrderId(order.getId());
        vo.setItems((items == null ? List.<SaleOrderItem>of() : items).stream().map(this::toItemVO).toList());
        vo.setShippingOrders((shippingOrders == null ? List.<ShippingOrder>of() : shippingOrders).stream().map(this::toShippingVO).toList());
        vo.setHasOpenException(saleExceptionService.countOpenByOrderId(order.getId()) > 0);
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
        List<ShippingOrderItem> shippingItems = shippingOrderItemMapper.selectByShippingOrderId(shipping.getId());
        vo.setItems((shippingItems == null ? List.<ShippingOrderItem>of() : shippingItems).stream().map(this::toShippingItemVO).toList());
        vo.setCreatedAt(shipping.getCreatedAt());
        vo.setUpdatedAt(shipping.getUpdatedAt());
        return vo;
    }

    private ShippingItemVO toShippingItemVO(ShippingOrderItem item) {
        ShippingItemVO vo = new ShippingItemVO();
        vo.setId(item.getId());
        vo.setShippingOrderId(item.getShippingOrderId());
        vo.setSaleOrderItemId(item.getSaleOrderItemId());
        vo.setSkuId(item.getSkuId());
        vo.setSkuCode(item.getSkuCode());
        vo.setProductName(item.getProductName());
        vo.setQuantity(item.getQuantity());
        vo.setSerialNos(splitSerialNos(item.getSerialNos()));
        vo.setCreatedAt(item.getCreatedAt());
        return vo;
    }

    private BigDecimal shippingQuantity(ShippingVO shipping) {
        if (shipping.getItems() == null || shipping.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }
        return shipping.getItems().stream()
                .map(ShippingItemVO::getQuantity)
                .map(this::safe)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateOrderNo(String prefix) {
        return prefix + "-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
