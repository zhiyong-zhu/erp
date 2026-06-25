package com.erp.production.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.common.core.domain.PageVO;
import com.erp.common.core.exception.BizException;
import com.erp.common.security.util.SecurityUtils;
import com.erp.inventory.domain.dto.InventoryIssueCreateRequest;
import com.erp.inventory.domain.dto.InventoryReceiptCreateRequest;
import com.erp.inventory.domain.vo.InventoryIssueVO;
import com.erp.inventory.service.InventoryReceiptService;
import com.erp.material.domain.entity.Material;
import com.erp.material.mapper.MaterialMapper;
import com.erp.production.domain.ProductionBatchStatusMachine;
import com.erp.production.domain.dto.ProductionMaterialMovementRequest;
import com.erp.production.domain.entity.ProductionBatch;
import com.erp.production.domain.entity.ProductionBomItem;
import com.erp.production.domain.entity.ProductionMaterialMovement;
import com.erp.production.domain.entity.ProductionMaterialMovementItem;
import com.erp.production.domain.vo.ProductionMaterialMovementItemVO;
import com.erp.production.domain.vo.ProductionMaterialMovementVO;
import com.erp.production.mapper.ProductionBatchMapper;
import com.erp.production.mapper.ProductionBomItemMapper;
import com.erp.production.mapper.ProductionMaterialMovementItemMapper;
import com.erp.production.mapper.ProductionMaterialMovementMapper;
import com.erp.production.service.ProductionMaterialMovementService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductionMaterialMovementServiceImpl implements ProductionMaterialMovementService {
    private static final String PICK = "PICK";
    private static final String RETURN = "RETURN";

    private final ProductionBatchMapper batchMapper;
    private final ProductionBomItemMapper bomItemMapper;
    private final ProductionMaterialMovementMapper movementMapper;
    private final ProductionMaterialMovementItemMapper movementItemMapper;
    private final MaterialMapper materialMapper;
    private final InventoryReceiptService inventoryReceiptService;

    public ProductionMaterialMovementServiceImpl(
            ProductionBatchMapper batchMapper,
            ProductionBomItemMapper bomItemMapper,
            ProductionMaterialMovementMapper movementMapper,
            ProductionMaterialMovementItemMapper movementItemMapper,
            MaterialMapper materialMapper,
            InventoryReceiptService inventoryReceiptService
    ) {
        this.batchMapper = batchMapper;
        this.bomItemMapper = bomItemMapper;
        this.movementMapper = movementMapper;
        this.movementItemMapper = movementItemMapper;
        this.materialMapper = materialMapper;
        this.inventoryReceiptService = inventoryReceiptService;
    }

    @Override
    @Transactional
    public ProductionMaterialMovementVO pickMaterials(ProductionMaterialMovementRequest request) {
        return createMovement(PICK, request);
    }

    @Override
    @Transactional
    public ProductionMaterialMovementVO returnMaterials(ProductionMaterialMovementRequest request) {
        return createMovement(RETURN, request);
    }

    @Override
    public PageVO<ProductionMaterialMovementVO> listMovements(long pageNum, long pageSize, UUID batchId, String movementType) {
        Page<ProductionMaterialMovement> page = movementMapper.selectPage(
                new Page<>(pageNum, pageSize),
                new LambdaQueryWrapper<ProductionMaterialMovement>()
                        .eq(batchId != null, ProductionMaterialMovement::getBatchId, batchId)
                        .eq(hasText(movementType), ProductionMaterialMovement::getMovementType, movementType)
                        .orderByDesc(ProductionMaterialMovement::getCreatedAt));
        List<ProductionMaterialMovementVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageVO<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    private ProductionMaterialMovementVO createMovement(String movementType, ProductionMaterialMovementRequest request) {
        String idempotencyKey = normalizeIdempotencyKey(request.getIdempotencyKey());
        if (idempotencyKey != null) {
            ProductionMaterialMovement existing = movementMapper.selectByIdempotencyKey(idempotencyKey);
            if (existing != null) {
                return toVO(existing);
            }
        }
        ProductionBatch batch = getBatch(request.getBatchId());
        ensureBatchAllowsMovement(batch);
        List<MovementLine> lines = resolveLines(request);
        validateMovementQuantity(batch, movementType, lines);

        OffsetDateTime now = OffsetDateTime.now();
        ProductionMaterialMovement movement = new ProductionMaterialMovement();
        movement.setId(UUID.randomUUID());
        movement.setMovementNo(generateMovementNo(movementType, now));
        movement.setMovementType(movementType);
        movement.setBatchId(batch.getId());
        movement.setBatchNo(batch.getBatchNo());
        movement.setStatus("COMPLETED");
        movement.setTotalQuantity(lines.stream().map(MovementLine::quantity).reduce(BigDecimal.ZERO, BigDecimal::add));
        movement.setWarehouseCode(request.getWarehouseCode());
        movement.setWarehouseName(request.getWarehouseName());
        movement.setLocationCode(request.getLocationCode());
        movement.setLocationName(request.getLocationName());
        movement.setBatchNoInventory(request.getBatchNo());
        movement.setIdempotencyKey(idempotencyKey);
        movement.setRemark(request.getRemark());
        movement.setCreatedBy(SecurityUtils.getUserId());
        movement.setCreatedAt(now);
        movement.setUpdatedBy(SecurityUtils.getUserId());
        movement.setUpdatedAt(now);
        movementMapper.insert(movement);

        for (MovementLine line : lines) {
            ProductionMaterialMovementItem item = new ProductionMaterialMovementItem();
            item.setId(UUID.randomUUID());
            item.setMovementId(movement.getId());
            item.setMaterialId(line.material().getId());
            item.setMaterialCode(line.material().getCode());
            item.setMaterialName(line.material().getName());
            item.setQuantity(line.quantity());
            item.setWarehouseCode(firstText(line.request().getWarehouseCode(), request.getWarehouseCode()));
            item.setWarehouseName(firstText(line.request().getWarehouseName(), request.getWarehouseName()));
            item.setLocationCode(firstText(line.request().getLocationCode(), request.getLocationCode()));
            item.setLocationName(firstText(line.request().getLocationName(), request.getLocationName()));
            item.setBatchNo(firstText(line.request().getBatchNo(), request.getBatchNo()));
            item.setRemark(line.request().getRemark());
            movementItemMapper.insert(item);
        }

        if (PICK.equals(movementType)) {
            InventoryIssueVO issue = inventoryReceiptService.createIssue(toIssueRequest(batch, movement, request, lines));
            movement.setInventoryDocumentId(issue.getId());
            movement.setInventoryDocumentNo(issue.getIssueNo());
        } else {
            UUID receiptId = inventoryReceiptService.createReceipt(toReceiptRequest(batch, movement, request, lines));
            movement.setInventoryDocumentId(receiptId);
        }
        movementMapper.updateById(movement);
        return toVO(movement);
    }

    private void validateMovementQuantity(ProductionBatch batch, String movementType, List<MovementLine> lines) {
        Map<UUID, BigDecimal> netPicked = netPickedQuantities(batch.getId());
        for (MovementLine line : lines) {
            BigDecimal currentNetPicked = netPicked.getOrDefault(line.material().getId(), BigDecimal.ZERO);
            if (PICK.equals(movementType)) {
                BigDecimal planned = plannedMaterialQuantity(batch, line.material().getId());
                if (planned != null && currentNetPicked.add(line.quantity()).compareTo(planned) > 0) {
                    throw new BizException(10004, "领料数量超过 BOM 计划需求: " + line.material().getCode());
                }
            } else if (currentNetPicked.compareTo(line.quantity()) < 0) {
                throw new BizException(10004, "退料数量不能超过已领未退数量: " + line.material().getCode());
            }
        }
    }

    private Map<UUID, BigDecimal> netPickedQuantities(UUID batchId) {
        List<ProductionMaterialMovement> movements = movementMapper.selectList(
                new LambdaQueryWrapper<ProductionMaterialMovement>()
                        .eq(ProductionMaterialMovement::getBatchId, batchId)
                        .eq(ProductionMaterialMovement::getStatus, "COMPLETED"));
        Map<UUID, BigDecimal> result = new HashMap<>();
        for (ProductionMaterialMovement movement : movements) {
            BigDecimal direction = PICK.equals(movement.getMovementType()) ? BigDecimal.ONE : BigDecimal.ONE.negate();
            List<ProductionMaterialMovementItem> items = movementItemMapper.selectByMovementId(movement.getId());
            for (ProductionMaterialMovementItem item : items) {
                result.merge(item.getMaterialId(), safe(item.getQuantity()).multiply(direction), BigDecimal::add);
            }
        }
        return result;
    }

    private BigDecimal plannedMaterialQuantity(ProductionBatch batch, UUID materialId) {
        if (batch.getBomId() == null) {
            return null;
        }
        List<ProductionBomItem> bomItems = bomItemMapper.selectByBomId(batch.getBomId());
        BigDecimal plannedQuantity = null;
        for (ProductionBomItem item : bomItems) {
            if (materialId.equals(item.getMaterialId())) {
                BigDecimal lossRate = safe(item.getLossRate());
                BigDecimal required = safe(item.getQuantity())
                        .multiply(safe(batch.getPlannedQuantity()))
                        .multiply(BigDecimal.ONE.add(lossRate));
                plannedQuantity = plannedQuantity == null ? required : plannedQuantity.add(required);
            }
        }
        return plannedQuantity;
    }

    private InventoryIssueCreateRequest toIssueRequest(
            ProductionBatch batch,
            ProductionMaterialMovement movement,
            ProductionMaterialMovementRequest request,
            List<MovementLine> lines
    ) {
        InventoryIssueCreateRequest issueRequest = new InventoryIssueCreateRequest();
        issueRequest.setIssueType("PRODUCTION_PICK");
        issueRequest.setSourceOrderId(movement.getId());
        issueRequest.setSourceOrderNo(movement.getMovementNo());
        issueRequest.setIdempotencyKey("production-movement-" + movement.getId());
        issueRequest.setWarehouseCode(request.getWarehouseCode());
        issueRequest.setWarehouseName(request.getWarehouseName());
        issueRequest.setLocationCode(request.getLocationCode());
        issueRequest.setLocationName(request.getLocationName());
        issueRequest.setBatchNo(firstText(request.getBatchNo(), batch.getBatchNo()));
        issueRequest.setRemark(firstText(request.getRemark(), "生产领料: " + batch.getBatchNo()));
        issueRequest.setItems(lines.stream().map(line -> {
            InventoryIssueCreateRequest.Item item = new InventoryIssueCreateRequest.Item();
            item.setMaterialId(line.material().getId());
            item.setQuantity(line.quantity());
            item.setWarehouseCode(line.request().getWarehouseCode());
            item.setWarehouseName(line.request().getWarehouseName());
            item.setLocationCode(line.request().getLocationCode());
            item.setLocationName(line.request().getLocationName());
            item.setBatchNo(line.request().getBatchNo());
            item.setRemark(line.request().getRemark());
            return item;
        }).toList());
        return issueRequest;
    }

    private InventoryReceiptCreateRequest toReceiptRequest(
            ProductionBatch batch,
            ProductionMaterialMovement movement,
            ProductionMaterialMovementRequest request,
            List<MovementLine> lines
    ) {
        InventoryReceiptCreateRequest receiptRequest = new InventoryReceiptCreateRequest();
        receiptRequest.setSourceType("PRODUCTION_RETURN");
        receiptRequest.setSourceOrderId(movement.getId());
        receiptRequest.setSourceOrderNo(movement.getMovementNo());
        receiptRequest.setIdempotencyKey("production-movement-" + movement.getId());
        receiptRequest.setWarehouseCode(request.getWarehouseCode());
        receiptRequest.setWarehouseName(request.getWarehouseName());
        receiptRequest.setLocationCode(request.getLocationCode());
        receiptRequest.setLocationName(request.getLocationName());
        receiptRequest.setBatchNo(firstText(request.getBatchNo(), batch.getBatchNo()));
        receiptRequest.setRemark(firstText(request.getRemark(), "生产退料: " + batch.getBatchNo()));
        receiptRequest.setItems(lines.stream().map(line -> {
            InventoryReceiptCreateRequest.Item item = new InventoryReceiptCreateRequest.Item();
            item.setMaterialId(line.material().getId());
            item.setMaterialCode(line.material().getCode());
            item.setMaterialName(line.material().getName());
            item.setQuantity(line.quantity());
            item.setWarehouseCode(line.request().getWarehouseCode());
            item.setWarehouseName(line.request().getWarehouseName());
            item.setLocationCode(line.request().getLocationCode());
            item.setLocationName(line.request().getLocationName());
            item.setBatchNo(line.request().getBatchNo());
            return item;
        }).toList());
        return receiptRequest;
    }

    private List<MovementLine> resolveLines(ProductionMaterialMovementRequest request) {
        return request.getItems().stream().map(item -> {
            BigDecimal quantity = safe(item.getQuantity());
            if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException(10004, "领退料数量必须大于0");
            }
            Material material = materialMapper.selectById(item.getMaterialId());
            if (material == null) {
                throw new BizException(10006, "原料不存在");
            }
            return new MovementLine(item, material, quantity);
        }).toList();
    }

    private void ensureBatchAllowsMovement(ProductionBatch batch) {
        if (!ProductionBatchStatusMachine.IN_PROGRESS.equals(batch.getStatus())
                && !ProductionBatchStatusMachine.COMPLETED.equals(batch.getStatus())) {
            throw new BizException(10004, "只有生产中或已完工批次允许领退料");
        }
    }

    private ProductionBatch getBatch(UUID id) {
        ProductionBatch batch = batchMapper.selectById(id);
        if (batch == null) {
            throw new BizException(10006, "生产批次不存在");
        }
        return batch;
    }

    private ProductionMaterialMovementVO toVO(ProductionMaterialMovement movement) {
        ProductionMaterialMovementVO vo = new ProductionMaterialMovementVO();
        vo.setId(movement.getId());
        vo.setMovementNo(movement.getMovementNo());
        vo.setMovementType(movement.getMovementType());
        vo.setBatchId(movement.getBatchId());
        vo.setBatchNo(movement.getBatchNo());
        vo.setInventoryDocumentId(movement.getInventoryDocumentId());
        vo.setInventoryDocumentNo(movement.getInventoryDocumentNo());
        vo.setStatus(movement.getStatus());
        vo.setTotalQuantity(movement.getTotalQuantity());
        vo.setWarehouseCode(movement.getWarehouseCode());
        vo.setWarehouseName(movement.getWarehouseName());
        vo.setLocationCode(movement.getLocationCode());
        vo.setLocationName(movement.getLocationName());
        vo.setBatchNoInventory(movement.getBatchNoInventory());
        vo.setRemark(movement.getRemark());
        vo.setCreatedAt(movement.getCreatedAt());
        vo.setUpdatedAt(movement.getUpdatedAt());
        vo.setItems(movementItemMapper.selectByMovementId(movement.getId()).stream().map(this::toItemVO).toList());
        return vo;
    }

    private ProductionMaterialMovementItemVO toItemVO(ProductionMaterialMovementItem item) {
        ProductionMaterialMovementItemVO vo = new ProductionMaterialMovementItemVO();
        vo.setId(item.getId());
        vo.setMovementId(item.getMovementId());
        vo.setMaterialId(item.getMaterialId());
        vo.setMaterialCode(item.getMaterialCode());
        vo.setMaterialName(item.getMaterialName());
        vo.setQuantity(item.getQuantity());
        vo.setWarehouseCode(item.getWarehouseCode());
        vo.setWarehouseName(item.getWarehouseName());
        vo.setLocationCode(item.getLocationCode());
        vo.setLocationName(item.getLocationName());
        vo.setBatchNo(item.getBatchNo());
        vo.setRemark(item.getRemark());
        return vo;
    }

    private String generateMovementNo(String movementType, OffsetDateTime now) {
        String prefix = PICK.equals(movementType) ? "PMR" : "PRR";
        return prefix + "-" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
    }

    private String normalizeIdempotencyKey(String idempotencyKey) {
        return idempotencyKey == null || idempotencyKey.isBlank() ? null : idempotencyKey.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String firstText(String first, String second) {
        return hasText(first) ? first : second;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record MovementLine(ProductionMaterialMovementRequest.Item request, Material material, BigDecimal quantity) {
    }
}
