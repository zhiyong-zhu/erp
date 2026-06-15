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
import com.erp.sales.domain.dto.SaleReturnRequest;
import com.erp.sales.domain.entity.SaleOrder;
import com.erp.sales.domain.entity.SaleOrderItem;
import com.erp.sales.domain.entity.SaleReturn;
import com.erp.sales.domain.entity.SaleReturnItem;
import com.erp.sales.domain.vo.SaleReturnItemVO;
import com.erp.sales.domain.vo.SaleReturnVO;
import com.erp.sales.mapper.SaleOrderItemMapper;
import com.erp.sales.mapper.SaleOrderMapper;
import com.erp.sales.mapper.SaleReturnItemMapper;
import com.erp.sales.mapper.SaleReturnMapper;
import com.erp.sales.service.SaleReturnService;
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
public class SaleReturnServiceImpl implements SaleReturnService {
    private static final Logger log = LoggerFactory.getLogger(SaleReturnServiceImpl.class);

    private final SaleReturnMapper saleReturnMapper;
    private final SaleReturnItemMapper saleReturnItemMapper;
    private final SaleOrderMapper saleOrderMapper;
    private final SaleOrderItemMapper saleOrderItemMapper;
    private final MaterialMapper materialMapper;
    private final InventoryTransactionMapper inventoryTransactionMapper;

    public SaleReturnServiceImpl(
            SaleReturnMapper saleReturnMapper,
            SaleReturnItemMapper saleReturnItemMapper,
            SaleOrderMapper saleOrderMapper,
            SaleOrderItemMapper saleOrderItemMapper,
            MaterialMapper materialMapper,
            InventoryTransactionMapper inventoryTransactionMapper
    ) {
        this.saleReturnMapper = saleReturnMapper;
        this.saleReturnItemMapper = saleReturnItemMapper;
        this.saleOrderMapper = saleOrderMapper;
        this.saleOrderItemMapper = saleOrderItemMapper;
        this.materialMapper = materialMapper;
        this.inventoryTransactionMapper = inventoryTransactionMapper;
    }

    @Override
    public PageVO<SaleReturnVO> listReturns(long pageNum, long pageSize) {
        Page<SaleReturn> page = saleReturnMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<SaleReturn>().orderByDesc(SaleReturn::getCreatedAt)
        );
        List<SaleReturnVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public SaleReturnVO detail(UUID id) {
        return toVO(getReturn(id));
    }

    @Override
    @Transactional
    public SaleReturnVO create(SaleReturnRequest request) {
        SaleOrder order = saleOrderMapper.selectById(request.getSaleOrderId());
        if (order == null) {
            throw new BizException(10004, "销售订单不存在");
        }
        if (!"SHIPPED".equals(order.getStatus()) && !"COMPLETED".equals(order.getStatus()) && !"RETURN_REQUEST".equals(order.getStatus())) {
            throw new BizException(10004, "当前订单状态不允许退货");
        }

        SaleReturn saleReturn = new SaleReturn();
        saleReturn.setId(UUID.randomUUID());
        saleReturn.setReturnNo(generateReturnNo("SR"));
        saleReturn.setSaleOrderId(order.getId());
        saleReturn.setSaleOrderNo(order.getOrderNo());
        saleReturn.setCustomerId(order.getCustomerId());
        saleReturn.setCustomerName(order.getCustomerName());
        saleReturn.setStatus("PENDING_REVIEW");
        saleReturn.setRemark(request.getRemark());
        saleReturn.setCreatedBy(SecurityUtils.getUserId());
        saleReturn.setCreatedAt(OffsetDateTime.now());
        saleReturn.setUpdatedBy(SecurityUtils.getUserId());
        saleReturn.setUpdatedAt(OffsetDateTime.now());

        Map<UUID, SaleOrderItem> orderItemMap = saleOrderItemMapper.selectBySaleOrderId(order.getId()).stream()
                .collect(Collectors.toMap(SaleOrderItem::getId, item -> item));

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SaleReturnRequest.Item itemReq : request.getItems()) {
            SaleOrderItem orderItem = orderItemMap.get(itemReq.getSaleOrderItemId());
            if (orderItem == null) {
                throw new BizException(10004, "订单明细不存在: " + itemReq.getSaleOrderItemId());
            }
            SaleReturnItem returnItem = new SaleReturnItem();
            returnItem.setId(UUID.randomUUID());
            returnItem.setSaleReturnId(saleReturn.getId());
            returnItem.setSaleOrderItemId(orderItem.getId());
            returnItem.setSkuId(orderItem.getSkuId());
            returnItem.setSkuCode(orderItem.getSkuCode());
            returnItem.setProductName(orderItem.getProductName());
            returnItem.setQuantity(itemReq.getQuantity());
            returnItem.setUnitPrice(orderItem.getUnitPrice());
            BigDecimal returnAmount = itemReq.getQuantity() != null && orderItem.getUnitPrice() != null
                    ? itemReq.getQuantity().multiply(orderItem.getUnitPrice()) : BigDecimal.ZERO;
            returnItem.setReturnAmount(returnAmount);
            returnItem.setReason(itemReq.getReason());
            returnItem.setCreatedAt(OffsetDateTime.now());
            saleReturnItemMapper.insert(returnItem);
            totalAmount = totalAmount.add(returnAmount);
        }

        saleReturn.setTotalAmount(totalAmount);
        saleReturnMapper.insert(saleReturn);

        // Update order status to RETURNING
        order.setStatus("RETURNING");
        order.setUpdatedBy(SecurityUtils.getUserId());
        order.setUpdatedAt(OffsetDateTime.now());
        saleOrderMapper.updateById(order);

        return toVO(saleReturn);
    }

    @Override
    @Transactional
    public SaleReturnVO changeStatus(UUID id, String action, String remark) {
        SaleReturn saleReturn = getReturn(id);
        switch (action) {
            case "approve" -> {
                ensureStatus(saleReturn, "PENDING_REVIEW");
                saleReturn.setStatus("APPROVED");
            }
            case "reject" -> {
                if (!"PENDING_REVIEW".equals(saleReturn.getStatus()) && !"APPROVED".equals(saleReturn.getStatus())) {
                    throw new BizException(10004, "当前退货单状态不允许驳回");
                }
                saleReturn.setStatus("REJECTED");
                // Restore order status
                SaleOrder order = saleOrderMapper.selectById(saleReturn.getSaleOrderId());
                if (order != null) {
                    order.setStatus("COMPLETED");
                    order.setUpdatedBy(SecurityUtils.getUserId());
                    order.setUpdatedAt(OffsetDateTime.now());
                    saleOrderMapper.updateById(order);
                }
            }
            case "inspect" -> {
                ensureStatus(saleReturn, "APPROVED");
                saleReturn.setStatus("INSPECTED");
                // Inventory return-in: create SALE_RETURN_IN transactions
                createReturnInTransactions(saleReturn);
            }
            case "refund" -> {
                ensureStatus(saleReturn, "INSPECTED");
                saleReturn.setStatus("REFUNDED");
            }
            case "complete" -> {
                ensureStatus(saleReturn, "REFUNDED");
                saleReturn.setStatus("COMPLETED");
                // Update order status to RETURNED
                SaleOrder order = saleOrderMapper.selectById(saleReturn.getSaleOrderId());
                if (order != null) {
                    order.setStatus("RETURNED");
                    order.setUpdatedBy(SecurityUtils.getUserId());
                    order.setUpdatedAt(OffsetDateTime.now());
                    saleOrderMapper.updateById(order);
                }
            }
            default -> throw new BizException(10004, "不支持的退货操作: " + action);
        }
        if (remark != null && !remark.isBlank()) {
            saleReturn.setRemark(remark);
        }
        saleReturn.setUpdatedBy(SecurityUtils.getUserId());
        saleReturn.setUpdatedAt(OffsetDateTime.now());
        saleReturnMapper.updateById(saleReturn);
        return toVO(saleReturn);
    }

    private void createReturnInTransactions(SaleReturn saleReturn) {
        List<SaleReturnItem> items = saleReturnItemMapper.selectBySaleReturnId(saleReturn.getId());
        for (SaleReturnItem item : items) {
            // Find corresponding material
            Material material = null;
            if (item.getSkuCode() != null) {
                List<Material> candidates = materialMapper.selectList(
                        new LambdaQueryWrapper<Material>().eq(Material::getCode, item.getSkuCode()));
                if (!candidates.isEmpty()) {
                    material = candidates.get(0);
                }
            }

            InventoryTransaction txn = new InventoryTransaction();
            txn.setId(UUID.randomUUID());
            txn.setTransactionType("SALE_RETURN_IN");
            txn.setQuantity(item.getQuantity()); // positive for return-in
            txn.setSourceType("SALE_RETURN");
            txn.setSourceOrderId(saleReturn.getId());
            txn.setSourceOrderNo(saleReturn.getReturnNo());
            txn.setSourceItemId(item.getId());
            txn.setRemark("销售退货入库: " + saleReturn.getReturnNo());
            txn.setCreatedBy(SecurityUtils.getUserId());
            txn.setCreatedAt(OffsetDateTime.now());

            if (material != null) {
                txn.setMaterialId(material.getId());
                txn.setMaterialCode(material.getCode());
                txn.setMaterialName(material.getName());
                BigDecimal newStock = safe(material.getCurrentStock()).add(item.getQuantity());
                txn.setBalanceAfter(newStock);
                material.setCurrentStock(newStock);
                materialMapper.updateById(material);
            } else {
                txn.setMaterialCode(item.getSkuCode());
                txn.setMaterialName(item.getProductName());
                txn.setBalanceAfter(BigDecimal.ZERO);
                log.info("Return SKU {} has no corresponding material, return-in recorded without stock update", item.getSkuCode());
            }
            inventoryTransactionMapper.insert(txn);
        }
    }

    private SaleReturn getReturn(UUID id) {
        SaleReturn saleReturn = saleReturnMapper.selectById(id);
        if (saleReturn == null) {
            throw new BizException(10006, "退货单不存在");
        }
        return saleReturn;
    }

    private void ensureStatus(SaleReturn saleReturn, String expectedStatus) {
        if (!expectedStatus.equals(saleReturn.getStatus())) {
            throw new BizException(10004, "当前退货单状态不允许该操作（当前: " + saleReturn.getStatus() + "）");
        }
    }

    private SaleReturnVO toVO(SaleReturn saleReturn) {
        SaleReturnVO vo = new SaleReturnVO();
        vo.setId(saleReturn.getId());
        vo.setReturnNo(saleReturn.getReturnNo());
        vo.setSaleOrderId(saleReturn.getSaleOrderId());
        vo.setSaleOrderNo(saleReturn.getSaleOrderNo());
        vo.setCustomerId(saleReturn.getCustomerId());
        vo.setCustomerName(saleReturn.getCustomerName());
        vo.setStatus(saleReturn.getStatus());
        vo.setTotalAmount(saleReturn.getTotalAmount());
        vo.setReason(saleReturn.getReason());
        vo.setRemark(saleReturn.getRemark());
        vo.setItems(saleReturnItemMapper.selectBySaleReturnId(saleReturn.getId()).stream().map(this::toItemVO).toList());
        return vo;
    }

    private SaleReturnItemVO toItemVO(SaleReturnItem item) {
        SaleReturnItemVO vo = new SaleReturnItemVO();
        vo.setId(item.getId());
        vo.setSaleReturnId(item.getSaleReturnId());
        vo.setSaleOrderItemId(item.getSaleOrderItemId());
        vo.setSkuId(item.getSkuId());
        vo.setSkuCode(item.getSkuCode());
        vo.setProductName(item.getProductName());
        vo.setQuantity(item.getQuantity());
        vo.setUnitPrice(item.getUnitPrice());
        vo.setReturnAmount(item.getReturnAmount());
        vo.setReason(item.getReason());
        return vo;
    }

    private String generateReturnNo(String prefix) {
        return prefix + "-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
