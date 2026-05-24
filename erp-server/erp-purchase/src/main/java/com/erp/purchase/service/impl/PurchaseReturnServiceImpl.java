package com.erp.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.inventory.domain.entity.InventoryTransaction;
import com.erp.inventory.mapper.InventoryTransactionMapper;
import com.erp.material.domain.entity.Material;
import com.erp.material.mapper.MaterialMapper;
import com.erp.purchase.domain.dto.PurchaseReturnRequest;
import com.erp.purchase.domain.entity.PurchaseOrder;
import com.erp.purchase.domain.entity.PurchaseOrderItem;
import com.erp.purchase.domain.entity.PurchaseReturn;
import com.erp.purchase.domain.entity.PurchaseReturnItem;
import com.erp.purchase.domain.vo.PurchaseReturnItemVO;
import com.erp.purchase.domain.vo.PurchaseReturnVO;
import com.erp.purchase.mapper.PurchaseOrderItemMapper;
import com.erp.purchase.mapper.PurchaseOrderMapper;
import com.erp.purchase.mapper.PurchaseReturnItemMapper;
import com.erp.purchase.mapper.PurchaseReturnMapper;
import com.erp.purchase.service.PurchaseReturnService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PurchaseReturnServiceImpl implements PurchaseReturnService {
    private final PurchaseReturnMapper purchaseReturnMapper;
    private final PurchaseReturnItemMapper purchaseReturnItemMapper;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final PurchaseOrderItemMapper purchaseOrderItemMapper;
    private final MaterialMapper materialMapper;
    private final InventoryTransactionMapper inventoryTransactionMapper;

    public PurchaseReturnServiceImpl(
            PurchaseReturnMapper purchaseReturnMapper,
            PurchaseReturnItemMapper purchaseReturnItemMapper,
            PurchaseOrderMapper purchaseOrderMapper,
            PurchaseOrderItemMapper purchaseOrderItemMapper,
            MaterialMapper materialMapper,
            InventoryTransactionMapper inventoryTransactionMapper
    ) {
        this.purchaseReturnMapper = purchaseReturnMapper;
        this.purchaseReturnItemMapper = purchaseReturnItemMapper;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.purchaseOrderItemMapper = purchaseOrderItemMapper;
        this.materialMapper = materialMapper;
        this.inventoryTransactionMapper = inventoryTransactionMapper;
    }

    @Override
    public PageVO<PurchaseReturnVO> list(long pageNum, long pageSize) {
        Page<PurchaseReturn> page = purchaseReturnMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<PurchaseReturn>().orderByDesc(PurchaseReturn::getCreatedAt)
        );
        List<PurchaseReturnVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional
    public PurchaseReturnVO create(UUID purchaseOrderId, PurchaseReturnRequest request) {
        PurchaseOrder order = purchaseOrderMapper.selectById(purchaseOrderId);
        if (order == null) {
            throw new BizException(10006, "采购单不存在");
        }
        PurchaseReturn purchaseReturn = new PurchaseReturn();
        purchaseReturn.setId(UUID.randomUUID());
        purchaseReturn.setReturnNo("PR-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        purchaseReturn.setPurchaseOrderId(order.getId());
        purchaseReturn.setPurchaseOrderNo(order.getOrderNo());
        purchaseReturn.setSupplierId(order.getSupplierId());
        purchaseReturn.setSupplierName(order.getSupplierName());
        purchaseReturn.setStatus("COMPLETED");
        purchaseReturn.setRemark(request.getRemark());
        purchaseReturn.setCreatedBy(SecurityUtils.getUserId());
        purchaseReturn.setCreatedAt(OffsetDateTime.now());
        purchaseReturnMapper.insert(purchaseReturn);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseReturnRequest.ReturnItem requestItem : request.getItems()) {
            PurchaseOrderItem orderItem = purchaseOrderItemMapper.selectById(requestItem.getItemId());
            if (orderItem == null || !purchaseOrderId.equals(orderItem.getPurchaseOrderId())) {
                throw new BizException(10004, "采购明细不存在");
            }

            BigDecimal available = safe(orderItem.getAcceptedQuantity()).subtract(safe(orderItem.getReturnedQuantity()));
            if (requestItem.getReturnQuantity() == null || requestItem.getReturnQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException(10004, "退货数量必须大于0");
            }
            if (requestItem.getReturnQuantity().compareTo(available) > 0) {
                throw new BizException(10004, "退货数量不能超过可退数量");
            }

            PurchaseReturnItem returnItem = new PurchaseReturnItem();
            returnItem.setId(UUID.randomUUID());
            returnItem.setPurchaseReturnId(purchaseReturn.getId());
            returnItem.setPurchaseOrderItemId(orderItem.getId());
            returnItem.setMaterialId(orderItem.getMaterialId());
            returnItem.setMaterialCode(orderItem.getMaterialCode());
            returnItem.setMaterialName(orderItem.getMaterialName());
            returnItem.setUnit(orderItem.getUnit());
            returnItem.setReturnQuantity(requestItem.getReturnQuantity());
            returnItem.setQuotePrice(orderItem.getQuotePrice());
            BigDecimal returnAmount = orderItem.getQuotePrice() == null
                    ? BigDecimal.ZERO
                    : orderItem.getQuotePrice().multiply(requestItem.getReturnQuantity());
            returnItem.setReturnAmount(returnAmount);
            returnItem.setReason(requestItem.getReason());
            returnItem.setCreatedAt(OffsetDateTime.now());
            purchaseReturnItemMapper.insert(returnItem);
            totalAmount = totalAmount.add(returnAmount);

            orderItem.setReturnedQuantity(safe(orderItem.getReturnedQuantity()).add(requestItem.getReturnQuantity()));
            purchaseOrderItemMapper.updateById(orderItem);

            Material material = materialMapper.selectById(orderItem.getMaterialId());
            if (material != null) {
                material.setCurrentStock(safe(material.getCurrentStock()).subtract(requestItem.getReturnQuantity()));
                materialMapper.updateById(material);

                InventoryTransaction txn = new InventoryTransaction();
                txn.setId(UUID.randomUUID());
                txn.setMaterialId(orderItem.getMaterialId());
                txn.setMaterialCode(orderItem.getMaterialCode());
                txn.setMaterialName(orderItem.getMaterialName());
                txn.setTransactionType("PURCHASE_RETURN");
                txn.setQuantity(requestItem.getReturnQuantity().negate());
                txn.setBalanceAfter(material.getCurrentStock());
                txn.setSourceType("PURCHASE_RETURN");
                txn.setSourceOrderId(order.getId());
                txn.setSourceOrderNo(order.getOrderNo());
                txn.setSourceItemId(orderItem.getId());
                txn.setRemark(requestItem.getReason());
                txn.setCreatedBy(SecurityUtils.getUserId());
                txn.setCreatedAt(OffsetDateTime.now());
                inventoryTransactionMapper.insert(txn);
            }
        }
        purchaseReturn.setTotalAmount(totalAmount);
        purchaseReturnMapper.updateById(purchaseReturn);
        return toVO(purchaseReturn);
    }

    private PurchaseReturnVO toVO(PurchaseReturn purchaseReturn) {
        PurchaseReturnVO vo = new PurchaseReturnVO();
        vo.setId(purchaseReturn.getId());
        vo.setReturnNo(purchaseReturn.getReturnNo());
        vo.setPurchaseOrderId(purchaseReturn.getPurchaseOrderId());
        vo.setPurchaseOrderNo(purchaseReturn.getPurchaseOrderNo());
        vo.setSupplierId(purchaseReturn.getSupplierId());
        vo.setSupplierName(purchaseReturn.getSupplierName());
        vo.setStatus(purchaseReturn.getStatus());
        vo.setTotalAmount(purchaseReturn.getTotalAmount());
        vo.setRemark(purchaseReturn.getRemark());
        vo.setCreatedAt(purchaseReturn.getCreatedAt());
        vo.setItems(purchaseReturnItemMapper.selectByPurchaseReturnId(purchaseReturn.getId()).stream().map(this::toItemVO).toList());
        return vo;
    }

    private PurchaseReturnItemVO toItemVO(PurchaseReturnItem item) {
        PurchaseReturnItemVO vo = new PurchaseReturnItemVO();
        vo.setId(item.getId());
        vo.setPurchaseOrderItemId(item.getPurchaseOrderItemId());
        vo.setMaterialId(item.getMaterialId());
        vo.setMaterialCode(item.getMaterialCode());
        vo.setMaterialName(item.getMaterialName());
        vo.setUnit(item.getUnit());
        vo.setReturnQuantity(item.getReturnQuantity());
        vo.setQuotePrice(item.getQuotePrice());
        vo.setReturnAmount(item.getReturnAmount());
        vo.setReason(item.getReason());
        return vo;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
