package com.erp.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.security.util.SecurityUtils;
import com.erp.inventory.domain.dto.InventoryReceiptCreateRequest;
import com.erp.inventory.domain.entity.InventoryReceipt;
import com.erp.inventory.domain.entity.InventoryTransaction;
import com.erp.inventory.domain.vo.InventoryReceiptVO;
import com.erp.inventory.domain.vo.InventoryTransactionVO;
import com.erp.inventory.mapper.InventoryReceiptMapper;
import com.erp.inventory.mapper.InventoryTransactionMapper;
import com.erp.inventory.service.InventoryReceiptService;
import com.erp.material.domain.entity.Material;
import com.erp.material.mapper.MaterialMapper;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryReceiptServiceImpl implements InventoryReceiptService {
    private final InventoryReceiptMapper inventoryReceiptMapper;
    private final InventoryTransactionMapper inventoryTransactionMapper;
    private final MaterialMapper materialMapper;

    public InventoryReceiptServiceImpl(
            InventoryReceiptMapper inventoryReceiptMapper,
            InventoryTransactionMapper inventoryTransactionMapper,
            MaterialMapper materialMapper
    ) {
        this.inventoryReceiptMapper = inventoryReceiptMapper;
        this.inventoryTransactionMapper = inventoryTransactionMapper;
        this.materialMapper = materialMapper;
    }

    @Override
    @Transactional
    public UUID createReceipt(InventoryReceiptCreateRequest request) {
        InventoryReceipt receipt = new InventoryReceipt();
        receipt.setId(UUID.randomUUID());
        receipt.setReceiptNo("GRN-" + OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        receipt.setSourceType(request.getSourceType());
        receipt.setSourceOrderId(request.getSourceOrderId());
        receipt.setSourceOrderNo(request.getSourceOrderNo());
        receipt.setSupplierId(request.getSupplierId());
        receipt.setSupplierName(request.getSupplierName());
        receipt.setStatus("COMPLETED");
        receipt.setRemark(request.getRemark());
        receipt.setCreatedBy(SecurityUtils.getUserId());
        receipt.setCreatedAt(OffsetDateTime.now());
        inventoryReceiptMapper.insert(receipt);

        if (request.getItems() != null) {
            for (InventoryReceiptCreateRequest.Item item : request.getItems()) {
                BigDecimal quantity = item.getQuantity() == null ? BigDecimal.ZERO : item.getQuantity();
                if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                Material material = materialMapper.selectById(item.getMaterialId());
                BigDecimal balanceAfter = material == null || material.getCurrentStock() == null
                        ? BigDecimal.ZERO
                        : material.getCurrentStock();

                InventoryTransaction transaction = new InventoryTransaction();
                transaction.setId(UUID.randomUUID());
                transaction.setMaterialId(item.getMaterialId());
                transaction.setMaterialCode(item.getMaterialCode());
                transaction.setMaterialName(item.getMaterialName());
                transaction.setTransactionType("PURCHASE_IN");
                transaction.setQuantity(quantity);
                transaction.setBalanceAfter(balanceAfter);
                transaction.setSourceType(request.getSourceType());
                transaction.setSourceOrderId(request.getSourceOrderId());
                transaction.setSourceOrderNo(request.getSourceOrderNo());
                transaction.setSourceItemId(item.getSourceItemId());
                transaction.setReceiptId(receipt.getId());
                transaction.setRemark(request.getRemark());
                transaction.setCreatedBy(SecurityUtils.getUserId());
                transaction.setCreatedAt(OffsetDateTime.now());
                inventoryTransactionMapper.insert(transaction);
            }
        }

        return receipt.getId();
    }

    @Override
    public PageVO<InventoryReceiptVO> listReceipts(long pageNum, long pageSize) {
        Page<InventoryReceipt> page = inventoryReceiptMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<InventoryReceipt>().orderByDesc(InventoryReceipt::getCreatedAt)
        );
        List<InventoryReceiptVO> records = page.getRecords().stream().map(this::toReceiptVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public PageVO<InventoryTransactionVO> listTransactions(long pageNum, long pageSize) {
        Page<InventoryTransaction> page = inventoryTransactionMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<InventoryTransaction>().orderByDesc(InventoryTransaction::getCreatedAt)
        );
        List<InventoryTransactionVO> records = page.getRecords().stream().map(this::toTransactionVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private InventoryReceiptVO toReceiptVO(InventoryReceipt receipt) {
        InventoryReceiptVO vo = new InventoryReceiptVO();
        vo.setId(receipt.getId());
        vo.setReceiptNo(receipt.getReceiptNo());
        vo.setSourceType(receipt.getSourceType());
        vo.setSourceOrderId(receipt.getSourceOrderId());
        vo.setSourceOrderNo(receipt.getSourceOrderNo());
        vo.setSupplierId(receipt.getSupplierId());
        vo.setSupplierName(receipt.getSupplierName());
        vo.setStatus(receipt.getStatus());
        vo.setRemark(receipt.getRemark());
        vo.setCreatedAt(receipt.getCreatedAt());
        return vo;
    }

    private InventoryTransactionVO toTransactionVO(InventoryTransaction transaction) {
        InventoryTransactionVO vo = new InventoryTransactionVO();
        vo.setId(transaction.getId());
        vo.setMaterialId(transaction.getMaterialId());
        vo.setMaterialCode(transaction.getMaterialCode());
        vo.setMaterialName(transaction.getMaterialName());
        vo.setTransactionType(transaction.getTransactionType());
        vo.setQuantity(transaction.getQuantity());
        vo.setBalanceAfter(transaction.getBalanceAfter());
        vo.setSourceType(transaction.getSourceType());
        vo.setSourceOrderId(transaction.getSourceOrderId());
        vo.setSourceOrderNo(transaction.getSourceOrderNo());
        vo.setSourceItemId(transaction.getSourceItemId());
        vo.setReceiptId(transaction.getReceiptId());
        vo.setRemark(transaction.getRemark());
        vo.setCreatedAt(transaction.getCreatedAt());
        return vo;
    }
}
