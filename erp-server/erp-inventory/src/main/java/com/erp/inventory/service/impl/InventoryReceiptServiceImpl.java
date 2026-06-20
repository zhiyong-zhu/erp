package com.erp.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.inventory.domain.dto.InventoryCheckCreateRequest;
import com.erp.inventory.domain.dto.InventoryIssueCreateRequest;
import com.erp.inventory.domain.dto.InventoryReceiptCreateRequest;
import com.erp.inventory.domain.dto.InventoryTransferCreateRequest;
import com.erp.inventory.domain.entity.InventoryCheck;
import com.erp.inventory.domain.entity.InventoryCheckItem;
import com.erp.inventory.domain.entity.InventoryIssue;
import com.erp.inventory.domain.entity.InventoryReceipt;
import com.erp.inventory.domain.entity.InventoryTransaction;
import com.erp.inventory.domain.entity.InventoryTransfer;
import com.erp.inventory.domain.entity.InventoryTransferItem;
import com.erp.inventory.domain.vo.InventoryCheckVO;
import com.erp.inventory.domain.vo.InventoryIssueVO;
import com.erp.inventory.domain.vo.InventoryReceiptVO;
import com.erp.inventory.domain.vo.InventoryTransactionVO;
import com.erp.inventory.domain.vo.InventoryTransferVO;
import com.erp.inventory.mapper.InventoryCheckItemMapper;
import com.erp.inventory.mapper.InventoryCheckMapper;
import com.erp.inventory.mapper.InventoryIssueMapper;
import com.erp.inventory.mapper.InventoryReceiptMapper;
import com.erp.inventory.mapper.InventoryTransactionMapper;
import com.erp.inventory.mapper.InventoryTransferItemMapper;
import com.erp.inventory.mapper.InventoryTransferMapper;
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
    private final InventoryCheckMapper inventoryCheckMapper;
    private final InventoryCheckItemMapper inventoryCheckItemMapper;
    private final InventoryIssueMapper inventoryIssueMapper;
    private final InventoryReceiptMapper inventoryReceiptMapper;
    private final InventoryTransactionMapper inventoryTransactionMapper;
    private final InventoryTransferMapper inventoryTransferMapper;
    private final InventoryTransferItemMapper inventoryTransferItemMapper;
    private final MaterialMapper materialMapper;

    public InventoryReceiptServiceImpl(
            InventoryCheckMapper inventoryCheckMapper,
            InventoryCheckItemMapper inventoryCheckItemMapper,
            InventoryIssueMapper inventoryIssueMapper,
            InventoryReceiptMapper inventoryReceiptMapper,
            InventoryTransactionMapper inventoryTransactionMapper,
            InventoryTransferMapper inventoryTransferMapper,
            InventoryTransferItemMapper inventoryTransferItemMapper,
            MaterialMapper materialMapper
    ) {
        this.inventoryCheckMapper = inventoryCheckMapper;
        this.inventoryCheckItemMapper = inventoryCheckItemMapper;
        this.inventoryIssueMapper = inventoryIssueMapper;
        this.inventoryReceiptMapper = inventoryReceiptMapper;
        this.inventoryTransactionMapper = inventoryTransactionMapper;
        this.inventoryTransferMapper = inventoryTransferMapper;
        this.inventoryTransferItemMapper = inventoryTransferItemMapper;
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
    @Transactional
    public InventoryCheckVO createCheck(InventoryCheckCreateRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BizException(10004, "盘点明细不能为空");
        }
        String checkType = request.getCheckType() == null || request.getCheckType().isBlank()
                ? "FULL"
                : request.getCheckType();
        OffsetDateTime now = OffsetDateTime.now();
        InventoryCheck check = new InventoryCheck();
        check.setId(UUID.randomUUID());
        check.setCheckNo("CHK-" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        check.setCheckType(checkType);
        check.setStatus("COMPLETED");
        check.setTotalDifference(BigDecimal.ZERO);
        check.setRemark(request.getRemark());
        check.setCreatedBy(SecurityUtils.getUserId());
        check.setCreatedAt(now);
        inventoryCheckMapper.insert(check);

        BigDecimal totalDifference = BigDecimal.ZERO;
        for (InventoryCheckCreateRequest.Item item : request.getItems()) {
            BigDecimal actualQuantity = item.getActualQuantity() == null ? BigDecimal.ZERO : item.getActualQuantity();
            if (actualQuantity.compareTo(BigDecimal.ZERO) < 0) {
                throw new BizException(10004, "实盘数量不能小于0");
            }
            Material material = materialMapper.selectById(item.getMaterialId());
            if (material == null) {
                throw new BizException(10006, "原料不存在");
            }
            BigDecimal systemQuantity = safe(material.getCurrentStock());
            BigDecimal difference = actualQuantity.subtract(systemQuantity);

            InventoryCheckItem checkItem = new InventoryCheckItem();
            checkItem.setId(UUID.randomUUID());
            checkItem.setCheckId(check.getId());
            checkItem.setMaterialId(material.getId());
            checkItem.setMaterialCode(material.getCode());
            checkItem.setMaterialName(material.getName());
            checkItem.setSystemQuantity(systemQuantity);
            checkItem.setActualQuantity(actualQuantity);
            checkItem.setDifferenceQuantity(difference);
            checkItem.setRemark(item.getRemark());
            inventoryCheckItemMapper.insert(checkItem);

            if (difference.compareTo(BigDecimal.ZERO) != 0) {
                material.setCurrentStock(actualQuantity);
                material.setUpdatedBy(SecurityUtils.getUserId());
                material.setUpdatedAt(now);
                materialMapper.updateById(material);

                InventoryTransaction transaction = new InventoryTransaction();
                transaction.setId(UUID.randomUUID());
                transaction.setMaterialId(material.getId());
                transaction.setMaterialCode(material.getCode());
                transaction.setMaterialName(material.getName());
                transaction.setTransactionType(difference.compareTo(BigDecimal.ZERO) > 0 ? "CHECK_PROFIT" : "CHECK_LOSS");
                transaction.setQuantity(difference);
                transaction.setBalanceAfter(actualQuantity);
                transaction.setSourceType("INVENTORY_CHECK");
                transaction.setSourceOrderId(check.getId());
                transaction.setSourceOrderNo(check.getCheckNo());
                transaction.setSourceItemId(checkItem.getId());
                transaction.setCheckId(check.getId());
                transaction.setRemark(item.getRemark() == null || item.getRemark().isBlank() ? request.getRemark() : item.getRemark());
                transaction.setCreatedBy(SecurityUtils.getUserId());
                transaction.setCreatedAt(now);
                inventoryTransactionMapper.insert(transaction);
            }
            totalDifference = totalDifference.add(difference);
        }

        check.setTotalDifference(totalDifference);
        inventoryCheckMapper.updateById(check);
        return toCheckVO(check);
    }

    @Override
    @Transactional
    public InventoryIssueVO createIssue(InventoryIssueCreateRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BizException(10004, "出库明细不能为空");
        }
        String issueType = request.getIssueType() == null || request.getIssueType().isBlank()
                ? "MANUAL_OUT"
                : request.getIssueType();
        OffsetDateTime now = OffsetDateTime.now();
        InventoryIssue issue = new InventoryIssue();
        issue.setId(UUID.randomUUID());
        issue.setIssueNo("ISS-" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        issue.setIssueType(issueType);
        issue.setSourceOrderId(request.getSourceOrderId());
        issue.setSourceOrderNo(request.getSourceOrderNo());
        issue.setStatus("COMPLETED");
        issue.setTotalQuantity(BigDecimal.ZERO);
        issue.setRemark(request.getRemark());
        issue.setCreatedBy(SecurityUtils.getUserId());
        issue.setCreatedAt(now);
        inventoryIssueMapper.insert(issue);

        BigDecimal totalQuantity = BigDecimal.ZERO;
        for (InventoryIssueCreateRequest.Item item : request.getItems()) {
            BigDecimal quantity = item.getQuantity() == null ? BigDecimal.ZERO : item.getQuantity();
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException(10004, "出库数量必须大于0");
            }
            Material material = materialMapper.selectById(item.getMaterialId());
            if (material == null) {
                throw new BizException(10006, "原料不存在");
            }
            BigDecimal currentStock = safe(material.getCurrentStock());
            if (currentStock.compareTo(quantity) < 0) {
                throw new BizException(10004, "原料库存不足: " + material.getName());
            }
            BigDecimal balanceAfter = currentStock.subtract(quantity);
            material.setCurrentStock(balanceAfter);
            material.setUpdatedBy(SecurityUtils.getUserId());
            material.setUpdatedAt(now);
            materialMapper.updateById(material);

            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setId(UUID.randomUUID());
            transaction.setMaterialId(material.getId());
            transaction.setMaterialCode(material.getCode());
            transaction.setMaterialName(material.getName());
            transaction.setTransactionType(issueType);
            transaction.setQuantity(quantity.negate());
            transaction.setBalanceAfter(balanceAfter);
            transaction.setSourceType(issueType);
            transaction.setSourceOrderId(request.getSourceOrderId());
            transaction.setSourceOrderNo(request.getSourceOrderNo());
            transaction.setIssueId(issue.getId());
            transaction.setRemark(item.getRemark() == null || item.getRemark().isBlank() ? request.getRemark() : item.getRemark());
            transaction.setCreatedBy(SecurityUtils.getUserId());
            transaction.setCreatedAt(now);
            inventoryTransactionMapper.insert(transaction);
            totalQuantity = totalQuantity.add(quantity);
        }

        issue.setTotalQuantity(totalQuantity);
        inventoryIssueMapper.updateById(issue);
        return toIssueVO(issue);
    }

    @Override
    @Transactional
    public InventoryTransferVO createTransfer(InventoryTransferCreateRequest request) {
        if (request.getFromLocation() == null || request.getFromLocation().isBlank()) {
            throw new BizException(10004, "调出位置不能为空");
        }
        if (request.getToLocation() == null || request.getToLocation().isBlank()) {
            throw new BizException(10004, "调入位置不能为空");
        }
        if (request.getFromLocation().equals(request.getToLocation())) {
            throw new BizException(10004, "调出位置和调入位置不能相同");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BizException(10004, "调拨明细不能为空");
        }
        OffsetDateTime now = OffsetDateTime.now();
        InventoryTransfer transfer = new InventoryTransfer();
        transfer.setId(UUID.randomUUID());
        transfer.setTransferNo("TRF-" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
        transfer.setFromLocation(request.getFromLocation());
        transfer.setToLocation(request.getToLocation());
        transfer.setStatus("COMPLETED");
        transfer.setTotalQuantity(BigDecimal.ZERO);
        transfer.setRemark(request.getRemark());
        transfer.setCreatedBy(SecurityUtils.getUserId());
        transfer.setCreatedAt(now);
        inventoryTransferMapper.insert(transfer);

        BigDecimal totalQuantity = BigDecimal.ZERO;
        for (InventoryTransferCreateRequest.Item item : request.getItems()) {
            BigDecimal quantity = item.getQuantity() == null ? BigDecimal.ZERO : item.getQuantity();
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException(10004, "调拨数量必须大于0");
            }
            Material material = materialMapper.selectById(item.getMaterialId());
            if (material == null) {
                throw new BizException(10006, "原料不存在");
            }
            BigDecimal currentStock = safe(material.getCurrentStock());
            if (currentStock.compareTo(quantity) < 0) {
                throw new BizException(10004, "原料库存不足: " + material.getName());
            }

            InventoryTransferItem transferItem = new InventoryTransferItem();
            transferItem.setId(UUID.randomUUID());
            transferItem.setTransferId(transfer.getId());
            transferItem.setMaterialId(material.getId());
            transferItem.setMaterialCode(material.getCode());
            transferItem.setMaterialName(material.getName());
            transferItem.setQuantity(quantity);
            transferItem.setRemark(item.getRemark());
            inventoryTransferItemMapper.insert(transferItem);

            InventoryTransaction outbound = new InventoryTransaction();
            outbound.setId(UUID.randomUUID());
            outbound.setMaterialId(material.getId());
            outbound.setMaterialCode(material.getCode());
            outbound.setMaterialName(material.getName());
            outbound.setTransactionType("TRANSFER_OUT");
            outbound.setQuantity(quantity.negate());
            outbound.setBalanceAfter(currentStock);
            outbound.setSourceType("TRANSFER");
            outbound.setSourceOrderId(transfer.getId());
            outbound.setSourceOrderNo(transfer.getTransferNo());
            outbound.setSourceItemId(transferItem.getId());
            outbound.setTransferId(transfer.getId());
            outbound.setRemark(request.getFromLocation() + " -> " + request.getToLocation());
            outbound.setCreatedBy(SecurityUtils.getUserId());
            outbound.setCreatedAt(now);
            inventoryTransactionMapper.insert(outbound);

            InventoryTransaction inbound = new InventoryTransaction();
            inbound.setId(UUID.randomUUID());
            inbound.setMaterialId(material.getId());
            inbound.setMaterialCode(material.getCode());
            inbound.setMaterialName(material.getName());
            inbound.setTransactionType("TRANSFER_IN");
            inbound.setQuantity(quantity);
            inbound.setBalanceAfter(currentStock);
            inbound.setSourceType("TRANSFER");
            inbound.setSourceOrderId(transfer.getId());
            inbound.setSourceOrderNo(transfer.getTransferNo());
            inbound.setSourceItemId(transferItem.getId());
            inbound.setTransferId(transfer.getId());
            inbound.setRemark(request.getFromLocation() + " -> " + request.getToLocation());
            inbound.setCreatedBy(SecurityUtils.getUserId());
            inbound.setCreatedAt(now);
            inventoryTransactionMapper.insert(inbound);
            totalQuantity = totalQuantity.add(quantity);
        }

        transfer.setTotalQuantity(totalQuantity);
        inventoryTransferMapper.updateById(transfer);
        return toTransferVO(transfer);
    }

    @Override
    public PageVO<InventoryIssueVO> listIssues(long pageNum, long pageSize) {
        Page<InventoryIssue> page = inventoryIssueMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<InventoryIssue>().orderByDesc(InventoryIssue::getCreatedAt)
        );
        List<InventoryIssueVO> records = page.getRecords().stream().map(this::toIssueVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public PageVO<InventoryCheckVO> listChecks(long pageNum, long pageSize) {
        Page<InventoryCheck> page = inventoryCheckMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<InventoryCheck>().orderByDesc(InventoryCheck::getCreatedAt)
        );
        List<InventoryCheckVO> records = page.getRecords().stream().map(this::toCheckVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public PageVO<InventoryTransferVO> listTransfers(long pageNum, long pageSize) {
        Page<InventoryTransfer> page = inventoryTransferMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<InventoryTransfer>().orderByDesc(InventoryTransfer::getCreatedAt)
        );
        List<InventoryTransferVO> records = page.getRecords().stream().map(this::toTransferVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
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

    private InventoryIssueVO toIssueVO(InventoryIssue issue) {
        InventoryIssueVO vo = new InventoryIssueVO();
        vo.setId(issue.getId());
        vo.setIssueNo(issue.getIssueNo());
        vo.setIssueType(issue.getIssueType());
        vo.setSourceOrderId(issue.getSourceOrderId());
        vo.setSourceOrderNo(issue.getSourceOrderNo());
        vo.setStatus(issue.getStatus());
        vo.setTotalQuantity(issue.getTotalQuantity());
        vo.setRemark(issue.getRemark());
        vo.setCreatedAt(issue.getCreatedAt());
        return vo;
    }

    private InventoryCheckVO toCheckVO(InventoryCheck check) {
        InventoryCheckVO vo = new InventoryCheckVO();
        vo.setId(check.getId());
        vo.setCheckNo(check.getCheckNo());
        vo.setCheckType(check.getCheckType());
        vo.setStatus(check.getStatus());
        vo.setTotalDifference(check.getTotalDifference());
        vo.setRemark(check.getRemark());
        vo.setCreatedAt(check.getCreatedAt());
        return vo;
    }

    private InventoryTransferVO toTransferVO(InventoryTransfer transfer) {
        InventoryTransferVO vo = new InventoryTransferVO();
        vo.setId(transfer.getId());
        vo.setTransferNo(transfer.getTransferNo());
        vo.setFromLocation(transfer.getFromLocation());
        vo.setToLocation(transfer.getToLocation());
        vo.setStatus(transfer.getStatus());
        vo.setTotalQuantity(transfer.getTotalQuantity());
        vo.setRemark(transfer.getRemark());
        vo.setCreatedAt(transfer.getCreatedAt());
        return vo;
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
        vo.setIssueId(transaction.getIssueId());
        vo.setTransferId(transaction.getTransferId());
        vo.setCheckId(transaction.getCheckId());
        vo.setRemark(transaction.getRemark());
        vo.setCreatedAt(transaction.getCreatedAt());
        return vo;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
