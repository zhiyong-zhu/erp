package com.erp.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.inventory.domain.dto.InventoryReceiptCreateRequest;
import com.erp.inventory.service.InventoryReceiptService;
import com.erp.material.domain.entity.Material;
import com.erp.material.domain.vo.MaterialReplenishmentVO;
import com.erp.material.mapper.MaterialMapper;
import com.erp.material.service.MaterialService;
import com.erp.purchase.domain.dto.PurchaseDraftGenerateRequest;
import com.erp.purchase.domain.dto.PurchaseOrderItemUpdateRequest;
import com.erp.purchase.domain.dto.PurchaseOrderReceiveRequest;
import com.erp.purchase.domain.dto.PurchaseOrderStatusRequest;
import com.erp.purchase.domain.dto.PurchaseOrderUpdateRequest;
import com.erp.purchase.domain.entity.PurchaseReturn;
import com.erp.purchase.domain.vo.PurchasePayableStatVO;
import com.erp.purchase.domain.entity.PurchaseOrder;
import com.erp.purchase.domain.entity.PurchaseOrderItem;
import com.erp.purchase.domain.vo.PurchaseOrderItemVO;
import com.erp.purchase.domain.vo.PurchaseOrderVO;
import com.erp.purchase.mapper.PurchaseOrderItemMapper;
import com.erp.purchase.mapper.PurchaseOrderMapper;
import com.erp.purchase.mapper.PurchaseReturnMapper;
import com.erp.purchase.service.PurchaseExceptionService;
import com.erp.purchase.service.PurchaseOrderService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final MaterialService materialService;
    private final MaterialMapper materialMapper;
    private final InventoryReceiptService inventoryReceiptService;
    private final PurchaseReturnMapper purchaseReturnMapper;
    private final PurchaseExceptionService purchaseExceptionService;

    public PurchaseOrderServiceImpl(
            PurchaseOrderMapper purchaseOrderMapper,
            PurchaseOrderItemMapper purchaseOrderItemMapper,
            MaterialService materialService,
            MaterialMapper materialMapper,
            InventoryReceiptService inventoryReceiptService,
            PurchaseReturnMapper purchaseReturnMapper,
            PurchaseExceptionService purchaseExceptionService
    ) {
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.purchaseOrderItemMapper = purchaseOrderItemMapper;
        this.materialService = materialService;
        this.materialMapper = materialMapper;
        this.inventoryReceiptService = inventoryReceiptService;
        this.purchaseReturnMapper = purchaseReturnMapper;
        this.purchaseExceptionService = purchaseExceptionService;
    }

    @Override
    public PageVO<PurchaseOrderVO> listOrders(long pageNum, long pageSize) {
        Page<PurchaseOrder> page = purchaseOrderMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<PurchaseOrder>().orderByDesc(PurchaseOrder::getCreatedAt)
        );
        List<PurchaseOrderVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public PurchaseOrderVO detail(UUID id) {
        return toVO(getOrder(id));
    }

    @Override
    @Transactional
    public PageVO<PurchaseOrderVO> generateDraftOrdersFromReplenishment(PurchaseDraftGenerateRequest request) {
        List<MaterialReplenishmentVO> suggestions = materialService
                .listReplenishmentSuggestions(1, 1000, null)
                .getRecords()
                .stream()
                .filter(item -> request.getMaterialIds().contains(item.getMaterialId()))
                .toList();

        if (suggestions.isEmpty()) {
            throw new BizException(10004, "未找到可生成采购草稿的补货建议");
        }

        Map<UUID, List<MaterialReplenishmentVO>> grouped = suggestions.stream()
                .filter(item -> item.getSupplierId() != null)
                .collect(Collectors.groupingBy(MaterialReplenishmentVO::getSupplierId));

        if (grouped.isEmpty()) {
            throw new BizException(10004, "所选补货建议缺少可用供应商，无法生成采购草稿");
        }

        List<PurchaseOrderVO> createdOrders = new ArrayList<>();
        for (Map.Entry<UUID, List<MaterialReplenishmentVO>> entry : grouped.entrySet()) {
            PurchaseOrder order = new PurchaseOrder();
            order.setId(UUID.randomUUID());
            order.setOrderNo(generateOrderNo("PO-DRAFT"));
            order.setSupplierId(entry.getKey());
            order.setSupplierName(entry.getValue().get(0).getSupplierName());
            order.setOrderType("DRAFT");
            order.setStatus("DRAFT");
            order.setSourceType("REPLENISHMENT");
            order.setRemark(request.getRemark());
            order.setCreatedBy(SecurityUtils.getUserId());
            order.setCreatedAt(OffsetDateTime.now());
            order.setUpdatedBy(SecurityUtils.getUserId());
            order.setUpdatedAt(OffsetDateTime.now());
            purchaseOrderMapper.insert(order);

            BigDecimal totalAmount = BigDecimal.ZERO;
            for (MaterialReplenishmentVO suggestion : entry.getValue()) {
                PurchaseOrderItem item = new PurchaseOrderItem();
                item.setId(UUID.randomUUID());
                item.setPurchaseOrderId(order.getId());
                item.setMaterialId(suggestion.getMaterialId());
                item.setMaterialCode(suggestion.getMaterialCode());
                item.setMaterialName(suggestion.getMaterialName());
                item.setUnit(suggestion.getUnit());
                item.setQuantity(suggestion.getSuggestedQuantity());
                item.setQuotePrice(suggestion.getQuotePrice());
                item.setEstimatedAmount(suggestion.getEstimatedAmount());
                item.setLeadTimeDays(suggestion.getLeadTimeDays());
                item.setSourceType("REPLENISHMENT");
                item.setSourceRefId(suggestion.getMaterialId());
                item.setReceivedQuantity(BigDecimal.ZERO);
                item.setAcceptedQuantity(BigDecimal.ZERO);
                item.setRejectedQuantity(BigDecimal.ZERO);
                item.setReturnedQuantity(BigDecimal.ZERO);
                item.setCreatedAt(OffsetDateTime.now());
                purchaseOrderItemMapper.insert(item);
                if (item.getEstimatedAmount() != null) {
                    totalAmount = totalAmount.add(item.getEstimatedAmount());
                }
            }
            order.setTotalAmount(totalAmount);
            purchaseOrderMapper.updateById(order);
            createdOrders.add(toVO(order));
        }

        return new PageVO<>(createdOrders, (long) createdOrders.size(), 1L, (long) createdOrders.size());
    }

    @Override
    @Transactional
    public PurchaseOrderVO updateDraft(UUID id, PurchaseOrderUpdateRequest request) {
        PurchaseOrder order = getOrder(id);
        ensureStatus(order, "DRAFT");

        order.setSupplierId(request.getSupplierId());
        order.setSupplierName(request.getSupplierName() == null ? order.getSupplierName() : request.getSupplierName());
        order.setRemark(request.getRemark());
        order.setUpdatedBy(SecurityUtils.getUserId());
        order.setUpdatedAt(OffsetDateTime.now());
        purchaseOrderMapper.updateById(order);

        replaceItems(order, request.getItems());
        return toVO(order);
    }

    @Override
    @Transactional
    public PurchaseOrderVO changeStatus(UUID id, PurchaseOrderStatusRequest request) {
        PurchaseOrder order = getOrder(id);
        String action = request.getAction();
        switch (action) {
            case "submit" -> {
                ensureStatus(order, "DRAFT");
                order.setStatus("PENDING_APPROVAL");
                order.setOrderType("ORDER");
            }
            case "approve" -> {
                ensureStatus(order, "PENDING_APPROVAL");
                order.setStatus("APPROVED");
            }
            case "reject" -> {
                ensureStatus(order, "PENDING_APPROVAL");
                order.setStatus("REJECTED");
            }
            case "cancel" -> {
                if ("RECEIVED".equals(order.getStatus())) {
                    throw new BizException(10004, "已收货采购单不可取消");
                }
                order.setStatus("CANCELLED");
            }
            default -> throw new BizException(10004, "不支持的采购单操作");
        }
        if (request.getRemark() != null && !request.getRemark().isBlank()) {
            order.setRemark(request.getRemark());
        }
        order.setUpdatedBy(SecurityUtils.getUserId());
        order.setUpdatedAt(OffsetDateTime.now());
        purchaseOrderMapper.updateById(order);
        return toVO(order);
    }

    @Override
    @Transactional
    public PurchaseOrderVO receiveOrder(UUID id, PurchaseOrderReceiveRequest request) {
        PurchaseOrder order = getOrder(id);
        if (!"APPROVED".equals(order.getStatus()) && !"PARTIAL_RECEIVED".equals(order.getStatus())) {
            throw new BizException(10004, "当前采购单状态不允许收货");
        }

        Map<UUID, PurchaseOrderItem> itemMap = purchaseOrderItemMapper.selectByPurchaseOrderId(order.getId()).stream()
                .collect(Collectors.toMap(PurchaseOrderItem::getId, item -> item));
        Map<UUID, BigDecimal> receivedMap = new java.util.HashMap<>();

        for (PurchaseOrderReceiveRequest.ReceiveItem receiveItem : request.getItems()) {
            PurchaseOrderItem item = itemMap.get(receiveItem.getItemId());
            if (item == null) {
                throw new BizException(10004, "采购明细不存在");
            }
            BigDecimal receivedQuantity = receiveItem.getReceivedQuantity();
            BigDecimal acceptedQuantity = safe(receiveItem.getAcceptedQuantity());
            BigDecimal rejectedQuantity = safe(receiveItem.getRejectedQuantity());
            if (receivedQuantity == null || receivedQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException(10004, "收货数量必须大于0");
            }
            if (acceptedQuantity.compareTo(BigDecimal.ZERO) < 0 || rejectedQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new BizException(10004, "验收数量不能小于0");
            }
            if (acceptedQuantity.add(rejectedQuantity).compareTo(receivedQuantity) != 0) {
                throw new BizException(10004, "合格数量与不合格数量之和必须等于收货数量");
            }
            BigDecimal totalReceived = safe(item.getReceivedQuantity()).add(receivedQuantity);
            if (totalReceived.compareTo(safe(item.getQuantity())) > 0) {
                throw new BizException(10004, "收货数量不能超过采购数量");
            }
            item.setReceivedQuantity(totalReceived);
            item.setAcceptedQuantity(safe(item.getAcceptedQuantity()).add(acceptedQuantity));
            item.setRejectedQuantity(safe(item.getRejectedQuantity()).add(rejectedQuantity));
            item.setInspectionResult(rejectedQuantity.compareTo(BigDecimal.ZERO) > 0 ? "PARTIAL_REJECTED" : "PASSED");
            item.setExceptionReason(receiveItem.getExceptionReason());
            purchaseOrderItemMapper.updateById(item);
            receivedMap.put(item.getId(), acceptedQuantity);

            if (rejectedQuantity.compareTo(BigDecimal.ZERO) > 0 || (receiveItem.getExceptionReason() != null && !receiveItem.getExceptionReason().isBlank())) {
                purchaseExceptionService.createInspectionException(
                        order,
                        item,
                        receiveItem.getExceptionReason() == null || receiveItem.getExceptionReason().isBlank()
                                ? "验收存在不合格数量"
                                : receiveItem.getExceptionReason()
                );
            }

            Material material = materialMapper.selectById(item.getMaterialId());
            if (material != null) {
                material.setCurrentStock(safe(material.getCurrentStock()).add(acceptedQuantity));
                materialMapper.updateById(material);
            }
        }

        InventoryReceiptCreateRequest receiptRequest = new InventoryReceiptCreateRequest();
        receiptRequest.setSourceType("PURCHASE");
        receiptRequest.setSourceOrderId(order.getId());
        receiptRequest.setSourceOrderNo(order.getOrderNo());
        receiptRequest.setSupplierId(order.getSupplierId());
        receiptRequest.setSupplierName(order.getSupplierName());
        receiptRequest.setRemark("采购收货入库");
        receiptRequest.setItems(
                purchaseOrderItemMapper.selectByPurchaseOrderId(order.getId()).stream()
                        .map(item -> {
                            InventoryReceiptCreateRequest.Item receiptItem = new InventoryReceiptCreateRequest.Item();
                            receiptItem.setMaterialId(item.getMaterialId());
                            receiptItem.setMaterialCode(item.getMaterialCode());
                            receiptItem.setMaterialName(item.getMaterialName());
                            receiptItem.setSourceItemId(item.getId());
                            receiptItem.setQuantity(receivedMap.getOrDefault(item.getId(), BigDecimal.ZERO));
                            return receiptItem;
                        })
                        .toList()
        );
        inventoryReceiptService.createReceipt(receiptRequest);

        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectByPurchaseOrderId(order.getId());
        boolean allReceived = items.stream().allMatch(item -> safe(item.getReceivedQuantity()).compareTo(safe(item.getQuantity())) >= 0);
        order.setStatus(allReceived ? "RECEIVED" : "PARTIAL_RECEIVED");
        order.setReceivedAt(OffsetDateTime.now());
        order.setUpdatedBy(SecurityUtils.getUserId());
        order.setUpdatedAt(OffsetDateTime.now());
        purchaseOrderMapper.updateById(order);
        return toVO(order);
    }

    @Override
    public PageVO<PurchasePayableStatVO> listPayableStats(long pageNum, long pageSize) {
        List<PurchaseOrder> orders = purchaseOrderMapper.selectList(
                new LambdaQueryWrapper<PurchaseOrder>()
                        .notIn(PurchaseOrder::getStatus, java.util.List.of("DRAFT", "CANCELLED", "REJECTED"))
        );
        List<PurchaseReturn> returns = purchaseReturnMapper.selectList(null);

        Map<UUID, List<PurchaseOrder>> ordersBySupplier = orders.stream()
                .filter(order -> order.getSupplierId() != null)
                .collect(Collectors.groupingBy(PurchaseOrder::getSupplierId));
        Map<UUID, List<PurchaseReturn>> returnsBySupplier = returns.stream()
                .filter(item -> item.getSupplierId() != null)
                .collect(Collectors.groupingBy(PurchaseReturn::getSupplierId));

        List<PurchasePayableStatVO> stats = ordersBySupplier.entrySet().stream().map(entry -> {
            UUID supplierId = entry.getKey();
            List<PurchaseOrder> supplierOrders = entry.getValue();
            List<PurchaseReturn> supplierReturns = returnsBySupplier.getOrDefault(supplierId, List.of());

            BigDecimal orderAmount = supplierOrders.stream()
                    .map(PurchaseOrder::getTotalAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal returnAmount = supplierReturns.stream()
                    .map(PurchaseReturn::getTotalAmount)
                    .filter(amount -> amount != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            PurchasePayableStatVO vo = new PurchasePayableStatVO();
            vo.setSupplierId(supplierId);
            vo.setSupplierName(supplierOrders.get(0).getSupplierName());
            vo.setOrderAmount(orderAmount);
            vo.setReturnAmount(returnAmount);
            vo.setNetPayableAmount(orderAmount.subtract(returnAmount));
            vo.setOrderCount((long) supplierOrders.size());
            vo.setReturnCount((long) supplierReturns.size());
            return vo;
        }).toList();

        int fromIndex = (int) Math.max((pageNum - 1) * pageSize, 0);
        int toIndex = (int) Math.min(fromIndex + pageSize, stats.size());
        List<PurchasePayableStatVO> records = fromIndex >= stats.size() ? List.of() : stats.subList(fromIndex, toIndex);
        return new PageVO<>(records, (long) stats.size(), pageNum, pageSize);
    }

    private void replaceItems(PurchaseOrder order, List<PurchaseOrderItemUpdateRequest> itemRequests) {
        List<PurchaseOrderItem> existing = purchaseOrderItemMapper.selectByPurchaseOrderId(order.getId());
        for (PurchaseOrderItem item : existing) {
            purchaseOrderItemMapper.deleteById(item.getId());
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseOrderItemUpdateRequest request : itemRequests) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setId(request.getId() == null ? UUID.randomUUID() : request.getId());
            item.setPurchaseOrderId(order.getId());
            item.setMaterialId(request.getMaterialId());
            item.setMaterialCode(request.getMaterialCode());
            item.setMaterialName(request.getMaterialName());
            item.setUnit(request.getUnit());
            item.setQuantity(request.getQuantity());
            item.setQuotePrice(request.getQuotePrice());
            BigDecimal estimatedAmount = request.getEstimatedAmount();
            if (estimatedAmount == null && request.getQuotePrice() != null && request.getQuantity() != null) {
                estimatedAmount = request.getQuotePrice().multiply(request.getQuantity());
            }
            item.setEstimatedAmount(estimatedAmount);
            item.setLeadTimeDays(request.getLeadTimeDays());
            item.setSourceType(request.getSourceType());
            item.setSourceRefId(request.getSourceRefId());
            item.setReceivedQuantity(request.getReceivedQuantity() == null ? BigDecimal.ZERO : request.getReceivedQuantity());
            item.setAcceptedQuantity(BigDecimal.ZERO);
            item.setRejectedQuantity(BigDecimal.ZERO);
            item.setReturnedQuantity(BigDecimal.ZERO);
            item.setCreatedAt(OffsetDateTime.now());
            purchaseOrderItemMapper.insert(item);
            if (estimatedAmount != null) {
                totalAmount = totalAmount.add(estimatedAmount);
            }
        }
        order.setTotalAmount(totalAmount);
        purchaseOrderMapper.updateById(order);
    }

    private PurchaseOrder getOrder(UUID id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        if (order == null) {
            throw new BizException(10006, "采购单不存在");
        }
        return order;
    }

    private void ensureStatus(PurchaseOrder order, String expectedStatus) {
        if (!expectedStatus.equals(order.getStatus())) {
            throw new BizException(10004, "当前采购单状态不允许该操作");
        }
    }

    private PurchaseOrderVO toVO(PurchaseOrder order) {
        PurchaseOrderVO vo = new PurchaseOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setSupplierId(order.getSupplierId());
        vo.setSupplierName(order.getSupplierName());
        vo.setOrderType(order.getOrderType());
        vo.setStatus(order.getStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setSourceType(order.getSourceType());
        vo.setRemark(order.getRemark());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setReceivedAt(order.getReceivedAt());
        vo.setItems(purchaseOrderItemMapper.selectByPurchaseOrderId(order.getId()).stream().map(this::toItemVO).toList());
        return vo;
    }

    private PurchaseOrderItemVO toItemVO(PurchaseOrderItem item) {
        PurchaseOrderItemVO vo = new PurchaseOrderItemVO();
        vo.setId(item.getId());
        vo.setMaterialId(item.getMaterialId());
        vo.setMaterialCode(item.getMaterialCode());
        vo.setMaterialName(item.getMaterialName());
        vo.setUnit(item.getUnit());
        vo.setQuantity(item.getQuantity());
        vo.setQuotePrice(item.getQuotePrice());
        vo.setEstimatedAmount(item.getEstimatedAmount());
        vo.setLeadTimeDays(item.getLeadTimeDays());
        vo.setSourceType(item.getSourceType());
        vo.setSourceRefId(item.getSourceRefId());
        vo.setReceivedQuantity(item.getReceivedQuantity());
        vo.setAcceptedQuantity(item.getAcceptedQuantity());
        vo.setRejectedQuantity(item.getRejectedQuantity());
        vo.setReturnedQuantity(item.getReturnedQuantity());
        vo.setInspectionResult(item.getInspectionResult());
        vo.setExceptionReason(item.getExceptionReason());
        return vo;
    }

    private String generateOrderNo(String prefix) {
        return prefix + "-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
