package com.erp.inventory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.report.excel.ExcelExportUtils;
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
import com.erp.inventory.domain.entity.InventoryBalance;
import com.erp.inventory.domain.entity.InventoryTransfer;
import com.erp.inventory.domain.entity.InventoryTransferItem;
import com.erp.inventory.domain.vo.InventoryBalanceVO;
import com.erp.inventory.domain.vo.InventoryCheckVO;
import com.erp.inventory.domain.vo.InventoryIssueVO;
import com.erp.inventory.domain.vo.InventoryReceiptVO;
import com.erp.inventory.domain.vo.InventoryTransactionVO;
import com.erp.inventory.domain.vo.InventoryTransferVO;
import com.erp.inventory.mapper.InventoryCheckItemMapper;
import com.erp.inventory.mapper.InventoryCheckMapper;
import com.erp.inventory.mapper.InventoryBalanceMapper;
import com.erp.inventory.mapper.InventoryIssueMapper;
import com.erp.inventory.mapper.InventoryReceiptMapper;
import com.erp.inventory.mapper.InventoryTransactionMapper;
import com.erp.inventory.mapper.InventoryTransferItemMapper;
import com.erp.inventory.mapper.InventoryTransferMapper;
import com.erp.inventory.service.InventoryBalanceService;
import com.erp.inventory.service.InventoryBalanceService.InventoryPosition;
import com.erp.inventory.service.InventoryReceiptService;
import com.erp.material.domain.entity.Material;
import com.erp.material.mapper.MaterialMapper;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryReceiptServiceImpl implements InventoryReceiptService {
    private static final String CHECK_STATUS_FROZEN = "FROZEN";
    private static final String CHECK_STATUS_REVIEWED = "REVIEWED";
    private static final String CHECK_STATUS_APPROVED = "APPROVED";
    private static final String CHECK_STATUS_REJECTED = "REJECTED";

    private final InventoryCheckMapper inventoryCheckMapper;
    private final InventoryCheckItemMapper inventoryCheckItemMapper;
    private final InventoryIssueMapper inventoryIssueMapper;
    private final InventoryReceiptMapper inventoryReceiptMapper;
    private final InventoryTransactionMapper inventoryTransactionMapper;
    private final InventoryTransferMapper inventoryTransferMapper;
    private final InventoryTransferItemMapper inventoryTransferItemMapper;
    private final InventoryBalanceMapper inventoryBalanceMapper;
    private final MaterialMapper materialMapper;
    private final InventoryBalanceService inventoryBalanceService;

    public InventoryReceiptServiceImpl(
            InventoryCheckMapper inventoryCheckMapper,
            InventoryCheckItemMapper inventoryCheckItemMapper,
            InventoryIssueMapper inventoryIssueMapper,
            InventoryReceiptMapper inventoryReceiptMapper,
            InventoryTransactionMapper inventoryTransactionMapper,
            InventoryTransferMapper inventoryTransferMapper,
            InventoryTransferItemMapper inventoryTransferItemMapper,
            InventoryBalanceMapper inventoryBalanceMapper,
            MaterialMapper materialMapper,
            InventoryBalanceService inventoryBalanceService
    ) {
        this.inventoryCheckMapper = inventoryCheckMapper;
        this.inventoryCheckItemMapper = inventoryCheckItemMapper;
        this.inventoryIssueMapper = inventoryIssueMapper;
        this.inventoryReceiptMapper = inventoryReceiptMapper;
        this.inventoryTransactionMapper = inventoryTransactionMapper;
        this.inventoryTransferMapper = inventoryTransferMapper;
        this.inventoryTransferItemMapper = inventoryTransferItemMapper;
        this.inventoryBalanceMapper = inventoryBalanceMapper;
        this.materialMapper = materialMapper;
        this.inventoryBalanceService = inventoryBalanceService;
    }

    @Override
    @Transactional
    public UUID createReceipt(InventoryReceiptCreateRequest request) {
        String idempotencyKey = normalizeIdempotencyKey(request.getIdempotencyKey());
        if (idempotencyKey != null) {
            InventoryReceipt existing = inventoryReceiptMapper.selectByIdempotencyKey(idempotencyKey);
            if (existing != null) {
                return existing.getId();
            }
        }

        InventoryReceipt receipt = new InventoryReceipt();
        receipt.setId(UUID.randomUUID());
        receipt.setReceiptNo(generateDocumentNo("GRN"));
        receipt.setSourceType(request.getSourceType());
        receipt.setSourceOrderId(request.getSourceOrderId());
        receipt.setSourceOrderNo(request.getSourceOrderNo());
        receipt.setSupplierId(request.getSupplierId());
        receipt.setSupplierName(request.getSupplierName());
        receipt.setIdempotencyKey(idempotencyKey);
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
                if (material == null) {
                    throw new BizException(10006, "原料不存在");
                }
                InventoryPosition position = position(
                        firstText(item.getWarehouseCode(), request.getWarehouseCode()),
                        firstText(item.getWarehouseName(), request.getWarehouseName()),
                        firstText(item.getLocationCode(), request.getLocationCode()),
                        firstText(item.getLocationName(), request.getLocationName()),
                        firstText(item.getBatchNo(), request.getBatchNo()));
                BigDecimal balanceBefore = safe(material.getCurrentStock());
                InventoryBalance balance = inventoryBalanceService.increase(material, quantity, position);

                InventoryTransaction transaction = new InventoryTransaction();
                transaction.setId(UUID.randomUUID());
                transaction.setMaterialId(item.getMaterialId());
                transaction.setMaterialCode(item.getMaterialCode());
                transaction.setMaterialName(item.getMaterialName());
                transaction.setTransactionType(resolveReceiptTransactionType(request.getSourceType()));
                transaction.setQuantity(quantity);
                transaction.setBalanceBefore(balanceBefore);
                transaction.setBalanceAfter(balance.getAvailableQuantity());
                applyPosition(transaction, balance);
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
    public boolean receiptExistsByIdempotencyKey(String idempotencyKey) {
        String normalizedKey = normalizeIdempotencyKey(idempotencyKey);
        return normalizedKey != null && inventoryReceiptMapper.selectByIdempotencyKey(normalizedKey) != null;
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
        check.setCheckNo(generateDocumentNo("CHK", now));
        check.setCheckType(checkType);
        check.setStatus(CHECK_STATUS_FROZEN);
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
            InventoryPosition position = position(
                    firstText(item.getWarehouseCode(), request.getWarehouseCode()),
                    firstText(item.getWarehouseName(), request.getWarehouseName()),
                    firstText(item.getLocationCode(), request.getLocationCode()),
                    firstText(item.getLocationName(), request.getLocationName()),
                    firstText(item.getBatchNo(), request.getBatchNo()));
            BigDecimal systemQuantity = currentBalanceQuantity(material, position);
            BigDecimal difference = actualQuantity.subtract(systemQuantity);

            InventoryCheckItem checkItem = new InventoryCheckItem();
            checkItem.setId(UUID.randomUUID());
            checkItem.setCheckId(check.getId());
            checkItem.setMaterialId(material.getId());
            checkItem.setMaterialCode(material.getCode());
            checkItem.setMaterialName(material.getName());
            checkItem.setWarehouseCode(position.warehouseCode());
            checkItem.setWarehouseName(position.warehouseName());
            checkItem.setLocationCode(position.locationCode());
            checkItem.setLocationName(position.locationName());
            checkItem.setBatchNo(position.batchNo());
            checkItem.setSystemQuantity(systemQuantity);
            checkItem.setActualQuantity(actualQuantity);
            checkItem.setDifferenceQuantity(difference);
            checkItem.setRemark(item.getRemark());
            inventoryCheckItemMapper.insert(checkItem);
            inventoryBalanceService.freezeForCheck(material, systemQuantity, position);
            totalDifference = totalDifference.add(difference);
        }

        check.setTotalDifference(totalDifference);
        inventoryCheckMapper.updateById(check);
        return toCheckVO(check);
    }

    @Override
    @Transactional
    public InventoryCheckVO reviewCheck(UUID id, String remark) {
        InventoryCheck check = requireCheck(id);
        requireCheckStatus(check, CHECK_STATUS_FROZEN, "只有已冻结的盘点单可以复核");
        check.setStatus(CHECK_STATUS_REVIEWED);
        check.setReviewedBy(SecurityUtils.getUserId());
        check.setReviewedAt(OffsetDateTime.now());
        check.setReviewRemark(remark);
        inventoryCheckMapper.updateById(check);
        return toCheckVO(check);
    }

    @Override
    @Transactional
    public InventoryCheckVO approveCheck(UUID id, String remark) {
        InventoryCheck check = requireCheck(id);
        requireCheckStatus(check, CHECK_STATUS_REVIEWED, "只有已复核的盘点单可以审批通过");
        OffsetDateTime now = OffsetDateTime.now();
        List<InventoryCheckItem> items = inventoryCheckItemMapper.selectList(new LambdaQueryWrapper<InventoryCheckItem>()
                .eq(InventoryCheckItem::getCheckId, check.getId()));
        for (InventoryCheckItem item : items) {
            Material material = materialMapper.selectById(item.getMaterialId());
            if (material == null) {
                throw new BizException(10006, "原料不存在");
            }
            InventoryPosition position = position(
                    item.getWarehouseCode(),
                    item.getWarehouseName(),
                    item.getLocationCode(),
                    item.getLocationName(),
                    item.getBatchNo());
            BigDecimal systemQuantity = safe(item.getSystemQuantity());
            BigDecimal actualQuantity = safe(item.getActualQuantity());
            BigDecimal difference = safe(item.getDifferenceQuantity());
            InventoryBalance balance = inventoryBalanceService.approveCheckAdjustment(material, systemQuantity, actualQuantity, position);
            if (difference.compareTo(BigDecimal.ZERO) != 0) {
                InventoryTransaction transaction = new InventoryTransaction();
                transaction.setId(UUID.randomUUID());
                transaction.setMaterialId(material.getId());
                transaction.setMaterialCode(material.getCode());
                transaction.setMaterialName(material.getName());
                transaction.setTransactionType(difference.compareTo(BigDecimal.ZERO) > 0 ? "CHECK_PROFIT" : "CHECK_LOSS");
                transaction.setQuantity(difference);
                transaction.setBalanceBefore(systemQuantity);
                transaction.setBalanceAfter(balance.getAvailableQuantity());
                applyPosition(transaction, balance);
                transaction.setSourceType("INVENTORY_CHECK");
                transaction.setSourceOrderId(check.getId());
                transaction.setSourceOrderNo(check.getCheckNo());
                transaction.setSourceItemId(item.getId());
                transaction.setCheckId(check.getId());
                transaction.setRemark(firstText(remark, item.getRemark()));
                transaction.setCreatedBy(SecurityUtils.getUserId());
                transaction.setCreatedAt(now);
                inventoryTransactionMapper.insert(transaction);
            }
        }
        check.setStatus(CHECK_STATUS_APPROVED);
        check.setApprovedBy(SecurityUtils.getUserId());
        check.setApprovedAt(now);
        check.setApprovalRemark(remark);
        inventoryCheckMapper.updateById(check);
        return toCheckVO(check);
    }

    @Override
    @Transactional
    public InventoryCheckVO rejectCheck(UUID id, String remark) {
        InventoryCheck check = requireCheck(id);
        if (!CHECK_STATUS_FROZEN.equals(check.getStatus()) && !CHECK_STATUS_REVIEWED.equals(check.getStatus())) {
            throw new BizException(10004, "只有已冻结或已复核的盘点单可以驳回");
        }
        List<InventoryCheckItem> items = inventoryCheckItemMapper.selectList(new LambdaQueryWrapper<InventoryCheckItem>()
                .eq(InventoryCheckItem::getCheckId, check.getId()));
        for (InventoryCheckItem item : items) {
            Material material = materialMapper.selectById(item.getMaterialId());
            if (material == null) {
                throw new BizException(10006, "原料不存在");
            }
            inventoryBalanceService.releaseCheckFreeze(material, safe(item.getSystemQuantity()), position(
                    item.getWarehouseCode(),
                    item.getWarehouseName(),
                    item.getLocationCode(),
                    item.getLocationName(),
                    item.getBatchNo()));
        }
        check.setStatus(CHECK_STATUS_REJECTED);
        check.setRejectedBy(SecurityUtils.getUserId());
        check.setRejectedAt(OffsetDateTime.now());
        check.setRejectRemark(remark);
        inventoryCheckMapper.updateById(check);
        return toCheckVO(check);
    }

    @Override
    @Transactional
    public InventoryIssueVO createIssue(InventoryIssueCreateRequest request) {
        String idempotencyKey = normalizeIdempotencyKey(request.getIdempotencyKey());
        if (idempotencyKey != null) {
            InventoryIssue existing = inventoryIssueMapper.selectByIdempotencyKey(idempotencyKey);
            if (existing != null) {
                return toIssueVO(existing);
            }
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BizException(10004, "出库明细不能为空");
        }
        String issueType = request.getIssueType() == null || request.getIssueType().isBlank()
                ? "MANUAL_OUT"
                : request.getIssueType();
        OffsetDateTime now = OffsetDateTime.now();
        InventoryIssue issue = new InventoryIssue();
        issue.setId(UUID.randomUUID());
        issue.setIssueNo(generateDocumentNo("ISS", now));
        issue.setIssueType(issueType);
        issue.setSourceOrderId(request.getSourceOrderId());
        issue.setSourceOrderNo(request.getSourceOrderNo());
        issue.setIdempotencyKey(idempotencyKey);
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
            InventoryPosition position = position(
                    firstText(item.getWarehouseCode(), request.getWarehouseCode()),
                    firstText(item.getWarehouseName(), request.getWarehouseName()),
                    firstText(item.getLocationCode(), request.getLocationCode()),
                    firstText(item.getLocationName(), request.getLocationName()),
                    firstText(item.getBatchNo(), request.getBatchNo()));
            BigDecimal balanceBefore = safe(material.getCurrentStock());
            InventoryBalance balance = inventoryBalanceService.decrease(material, quantity, position);

            InventoryTransaction transaction = new InventoryTransaction();
            transaction.setId(UUID.randomUUID());
            transaction.setMaterialId(material.getId());
            transaction.setMaterialCode(material.getCode());
            transaction.setMaterialName(material.getName());
            transaction.setTransactionType(issueType);
            transaction.setQuantity(quantity.negate());
            transaction.setBalanceBefore(balanceBefore);
            transaction.setBalanceAfter(balance.getAvailableQuantity());
            applyPosition(transaction, balance);
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
        transfer.setTransferNo(generateDocumentNo("TRF", now));
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
            InventoryPosition fromPosition = position(
                    firstText(item.getFromWarehouseCode(), request.getFromWarehouseCode()),
                    firstText(item.getFromWarehouseName(), request.getFromWarehouseName()),
                    firstText(item.getFromLocationCode(), firstText(request.getFromLocationCode(), request.getFromLocation())),
                    firstText(item.getFromLocationName(), firstText(request.getFromLocationName(), request.getFromLocation())),
                    firstText(item.getFromBatchNo(), request.getFromBatchNo()));
            InventoryPosition toPosition = position(
                    firstText(item.getToWarehouseCode(), request.getToWarehouseCode()),
                    firstText(item.getToWarehouseName(), request.getToWarehouseName()),
                    firstText(item.getToLocationCode(), firstText(request.getToLocationCode(), request.getToLocation())),
                    firstText(item.getToLocationName(), firstText(request.getToLocationName(), request.getToLocation())),
                    firstText(item.getToBatchNo(), request.getToBatchNo()));
            BigDecimal balanceBefore = currentBalanceQuantity(material, fromPosition);

            InventoryTransferItem transferItem = new InventoryTransferItem();
            transferItem.setId(UUID.randomUUID());
            transferItem.setTransferId(transfer.getId());
            transferItem.setMaterialId(material.getId());
            transferItem.setMaterialCode(material.getCode());
            transferItem.setMaterialName(material.getName());
            transferItem.setQuantity(quantity);
            transferItem.setRemark(item.getRemark());
            inventoryTransferItemMapper.insert(transferItem);

            InventoryBalance outboundBalance = inventoryBalanceService.decrease(material, quantity, fromPosition);
            InventoryTransaction outbound = new InventoryTransaction();
            outbound.setId(UUID.randomUUID());
            outbound.setMaterialId(material.getId());
            outbound.setMaterialCode(material.getCode());
            outbound.setMaterialName(material.getName());
            outbound.setTransactionType("TRANSFER_OUT");
            outbound.setQuantity(quantity.negate());
            outbound.setBalanceBefore(balanceBefore);
            outbound.setBalanceAfter(outboundBalance.getAvailableQuantity());
            applyPosition(outbound, outboundBalance);
            outbound.setSourceType("TRANSFER");
            outbound.setSourceOrderId(transfer.getId());
            outbound.setSourceOrderNo(transfer.getTransferNo());
            outbound.setSourceItemId(transferItem.getId());
            outbound.setTransferId(transfer.getId());
            outbound.setRemark(request.getFromLocation() + " -> " + request.getToLocation());
            outbound.setCreatedBy(SecurityUtils.getUserId());
            outbound.setCreatedAt(now);
            inventoryTransactionMapper.insert(outbound);

            InventoryBalance inboundBalance = inventoryBalanceService.increase(material, quantity, toPosition);
            InventoryTransaction inbound = new InventoryTransaction();
            inbound.setId(UUID.randomUUID());
            inbound.setMaterialId(material.getId());
            inbound.setMaterialCode(material.getCode());
            inbound.setMaterialName(material.getName());
            inbound.setTransactionType("TRANSFER_IN");
            inbound.setQuantity(quantity);
            inbound.setBalanceBefore(safe(inboundBalance.getAvailableQuantity()).subtract(quantity));
            inbound.setBalanceAfter(inboundBalance.getAvailableQuantity());
            applyPosition(inbound, inboundBalance);
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
    public PageVO<InventoryTransactionVO> listTransactions(
            long pageNum,
            long pageSize,
            UUID receiptId,
            UUID issueId,
            UUID transferId,
            UUID checkId,
            UUID sourceOrderId
    ) {
        Page<InventoryTransaction> page = inventoryTransactionMapper.selectPage(
                new Page<>(pageNum, pageSize),
                transactionWrapper(receiptId, issueId, transferId, checkId, sourceOrderId)
        );
        List<InventoryTransactionVO> records = page.getRecords().stream().map(this::toTransactionVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public ByteArrayInputStream exportTransactions(UUID receiptId, UUID issueId, UUID transferId, UUID checkId, UUID sourceOrderId) {
        List<InventoryTransactionVO> records = inventoryTransactionMapper.selectList(transactionWrapper(receiptId, issueId, transferId, checkId, sourceOrderId))
                .stream()
                .map(this::toTransactionVO)
                .collect(Collectors.toList());
        return ExcelExportUtils.export("inventory-transactions", List.of(
                ExcelExportUtils.column("原料编码", InventoryTransactionVO::getMaterialCode),
                ExcelExportUtils.column("原料名称", InventoryTransactionVO::getMaterialName),
                ExcelExportUtils.column("流水类型", InventoryTransactionVO::getTransactionType),
                ExcelExportUtils.column("变化数量", InventoryTransactionVO::getQuantity),
                ExcelExportUtils.column("变更前", InventoryTransactionVO::getBalanceBefore),
                ExcelExportUtils.column("结存数量", InventoryTransactionVO::getBalanceAfter),
                ExcelExportUtils.column("仓库编码", InventoryTransactionVO::getWarehouseCode),
                ExcelExportUtils.column("仓库名称", InventoryTransactionVO::getWarehouseName),
                ExcelExportUtils.column("库位编码", InventoryTransactionVO::getLocationCode),
                ExcelExportUtils.column("库位名称", InventoryTransactionVO::getLocationName),
                ExcelExportUtils.column("批次", InventoryTransactionVO::getBatchNo),
                ExcelExportUtils.column("来源类型", InventoryTransactionVO::getSourceType),
                ExcelExportUtils.column("来源单号", InventoryTransactionVO::getSourceOrderNo),
                ExcelExportUtils.column("幂等键", InventoryTransactionVO::getIdempotencyKey),
                ExcelExportUtils.column("备注", InventoryTransactionVO::getRemark),
                ExcelExportUtils.column("创建时间", InventoryTransactionVO::getCreatedAt)
        ), records, "库存流水导出失败");
    }

    @Override
    public PageVO<InventoryBalanceVO> listBalances(
            long pageNum,
            long pageSize,
            String materialName,
            String warehouseCode,
            String locationCode,
            String batchNo
    ) {
        LambdaQueryWrapper<InventoryBalance> wrapper = balanceWrapper(materialName, warehouseCode, locationCode, batchNo);
        Page<InventoryBalance> page = inventoryBalanceMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<InventoryBalanceVO> records = page.getRecords().stream().map(this::toBalanceVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public ByteArrayInputStream exportBalances(String materialName, String warehouseCode, String locationCode, String batchNo) {
        List<InventoryBalanceVO> records = inventoryBalanceMapper.selectList(balanceWrapper(materialName, warehouseCode, locationCode, batchNo))
                .stream()
                .map(this::toBalanceVO)
                .collect(Collectors.toList());
        return ExcelExportUtils.export("inventory-balances", List.of(
                ExcelExportUtils.column("原料编码", InventoryBalanceVO::getMaterialCode),
                ExcelExportUtils.column("原料名称", InventoryBalanceVO::getMaterialName),
                ExcelExportUtils.column("仓库编码", InventoryBalanceVO::getWarehouseCode),
                ExcelExportUtils.column("仓库名称", InventoryBalanceVO::getWarehouseName),
                ExcelExportUtils.column("库位编码", InventoryBalanceVO::getLocationCode),
                ExcelExportUtils.column("库位名称", InventoryBalanceVO::getLocationName),
                ExcelExportUtils.column("批次", InventoryBalanceVO::getBatchNo),
                ExcelExportUtils.column("可用库存", InventoryBalanceVO::getAvailableQuantity),
                ExcelExportUtils.column("冻结库存", InventoryBalanceVO::getFrozenQuantity),
                ExcelExportUtils.column("总库存", InventoryBalanceVO::getTotalQuantity),
                ExcelExportUtils.column("更新时间", InventoryBalanceVO::getUpdatedAt)
        ), records, "库存余额导出失败");
    }

    private LambdaQueryWrapper<InventoryTransaction> transactionWrapper(
            UUID receiptId,
            UUID issueId,
            UUID transferId,
            UUID checkId,
            UUID sourceOrderId
    ) {
        return new LambdaQueryWrapper<InventoryTransaction>()
                .eq(receiptId != null, InventoryTransaction::getReceiptId, receiptId)
                .eq(issueId != null, InventoryTransaction::getIssueId, issueId)
                .eq(transferId != null, InventoryTransaction::getTransferId, transferId)
                .eq(checkId != null, InventoryTransaction::getCheckId, checkId)
                .eq(sourceOrderId != null, InventoryTransaction::getSourceOrderId, sourceOrderId)
                .orderByDesc(InventoryTransaction::getCreatedAt);
    }

    private LambdaQueryWrapper<InventoryBalance> balanceWrapper(
            String materialName,
            String warehouseCode,
            String locationCode,
            String batchNo
    ) {
        return new LambdaQueryWrapper<InventoryBalance>()
                .like(hasText(materialName), InventoryBalance::getMaterialName, materialName)
                .eq(hasText(warehouseCode), InventoryBalance::getWarehouseCode, warehouseCode)
                .eq(hasText(locationCode), InventoryBalance::getLocationCode, locationCode)
                .eq(hasText(batchNo), InventoryBalance::getBatchNo, batchNo)
                .orderByAsc(InventoryBalance::getMaterialCode)
                .orderByAsc(InventoryBalance::getWarehouseCode)
                .orderByAsc(InventoryBalance::getLocationCode)
                .orderByAsc(InventoryBalance::getBatchNo);
    }

    private InventoryIssueVO toIssueVO(InventoryIssue issue) {
        InventoryIssueVO vo = new InventoryIssueVO();
        vo.setId(issue.getId());
        vo.setIssueNo(issue.getIssueNo());
        vo.setIssueType(issue.getIssueType());
        vo.setSourceOrderId(issue.getSourceOrderId());
        vo.setSourceOrderNo(issue.getSourceOrderNo());
        vo.setIdempotencyKey(issue.getIdempotencyKey());
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
        vo.setReviewedAt(check.getReviewedAt());
        vo.setReviewRemark(check.getReviewRemark());
        vo.setApprovedAt(check.getApprovedAt());
        vo.setApprovalRemark(check.getApprovalRemark());
        vo.setRejectedAt(check.getRejectedAt());
        vo.setRejectRemark(check.getRejectRemark());
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
        vo.setIdempotencyKey(receipt.getIdempotencyKey());
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
        vo.setBalanceBefore(transaction.getBalanceBefore());
        vo.setBalanceAfter(transaction.getBalanceAfter());
        vo.setWarehouseCode(transaction.getWarehouseCode());
        vo.setWarehouseName(transaction.getWarehouseName());
        vo.setLocationCode(transaction.getLocationCode());
        vo.setLocationName(transaction.getLocationName());
        vo.setBatchNo(transaction.getBatchNo());
        vo.setSourceType(transaction.getSourceType());
        vo.setSourceOrderId(transaction.getSourceOrderId());
        vo.setSourceOrderNo(transaction.getSourceOrderNo());
        vo.setSourceItemId(transaction.getSourceItemId());
        vo.setReceiptId(transaction.getReceiptId());
        vo.setIssueId(transaction.getIssueId());
        vo.setTransferId(transaction.getTransferId());
        vo.setCheckId(transaction.getCheckId());
        vo.setIdempotencyKey(resolveTransactionIdempotencyKey(transaction));
        vo.setRemark(transaction.getRemark());
        vo.setCreatedAt(transaction.getCreatedAt());
        return vo;
    }

    private InventoryBalanceVO toBalanceVO(InventoryBalance balance) {
        InventoryBalanceVO vo = new InventoryBalanceVO();
        vo.setId(balance.getId());
        vo.setMaterialId(balance.getMaterialId());
        vo.setMaterialCode(balance.getMaterialCode());
        vo.setMaterialName(balance.getMaterialName());
        vo.setWarehouseCode(balance.getWarehouseCode());
        vo.setWarehouseName(balance.getWarehouseName());
        vo.setLocationCode(balance.getLocationCode());
        vo.setLocationName(balance.getLocationName());
        vo.setBatchNo(balance.getBatchNo());
        vo.setAvailableQuantity(balance.getAvailableQuantity());
        vo.setFrozenQuantity(balance.getFrozenQuantity());
        vo.setTotalQuantity(balance.getTotalQuantity());
        vo.setUpdatedAt(balance.getUpdatedAt());
        return vo;
    }

    private InventoryPosition position(String warehouseCode, String warehouseName, String locationCode, String locationName, String batchNo) {
        return new InventoryPosition(warehouseCode, warehouseName, locationCode, locationName, batchNo).normalized();
    }

    private InventoryCheck requireCheck(UUID id) {
        InventoryCheck check = inventoryCheckMapper.selectById(id);
        if (check == null) {
            throw new BizException(10006, "盘点单不存在");
        }
        return check;
    }

    private void requireCheckStatus(InventoryCheck check, String expectedStatus, String message) {
        if (!expectedStatus.equals(check.getStatus())) {
            throw new BizException(10004, message);
        }
    }

    private BigDecimal currentBalanceQuantity(Material material, InventoryPosition rawPosition) {
        InventoryPosition position = rawPosition.normalized();
        InventoryBalance balance = inventoryBalanceMapper.selectOne(new LambdaQueryWrapper<InventoryBalance>()
                .eq(InventoryBalance::getMaterialId, material.getId())
                .eq(InventoryBalance::getWarehouseCode, position.warehouseCode())
                .eq(InventoryBalance::getLocationCode, position.locationCode())
                .eq(InventoryBalance::getBatchNo, position.batchNo()));
        return balance == null ? BigDecimal.ZERO : safe(balance.getAvailableQuantity());
    }

    private String resolveTransactionIdempotencyKey(InventoryTransaction transaction) {
        if (transaction.getReceiptId() != null) {
            InventoryReceipt receipt = inventoryReceiptMapper.selectById(transaction.getReceiptId());
            return receipt == null ? null : receipt.getIdempotencyKey();
        }
        if (transaction.getIssueId() != null) {
            InventoryIssue issue = inventoryIssueMapper.selectById(transaction.getIssueId());
            return issue == null ? null : issue.getIdempotencyKey();
        }
        return null;
    }

    private void applyPosition(InventoryTransaction transaction, InventoryBalance balance) {
        transaction.setWarehouseCode(balance.getWarehouseCode());
        transaction.setWarehouseName(balance.getWarehouseName());
        transaction.setLocationCode(balance.getLocationCode());
        transaction.setLocationName(balance.getLocationName());
        transaction.setBatchNo(balance.getBatchNo());
    }

    private String firstText(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return null;
        }
        return idempotencyKey.trim();
    }

    private String resolveReceiptTransactionType(String sourceType) {
        return "PURCHASE".equalsIgnoreCase(firstText(sourceType, ""))
                ? "PURCHASE_IN"
                : "MANUAL_IN";
    }

    private String generateDocumentNo(String prefix) {
        return generateDocumentNo(prefix, OffsetDateTime.now());
    }

    private String generateDocumentNo(String prefix, OffsetDateTime now) {
        return prefix + "-" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + "-" + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
