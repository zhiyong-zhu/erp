package com.erp.production.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.erp.common.core.exception.BizException;
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
import com.erp.production.mapper.ProductionBatchMapper;
import com.erp.production.mapper.ProductionBomItemMapper;
import com.erp.production.mapper.ProductionMaterialMovementItemMapper;
import com.erp.production.mapper.ProductionMaterialMovementMapper;
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
class ProductionMaterialMovementServiceImplTest {
    @Mock private ProductionBatchMapper batchMapper;
    @Mock private ProductionBomItemMapper bomItemMapper;
    @Mock private ProductionMaterialMovementMapper movementMapper;
    @Mock private ProductionMaterialMovementItemMapper movementItemMapper;
    @Mock private MaterialMapper materialMapper;
    @Mock private InventoryReceiptService inventoryReceiptService;

    private ProductionMaterialMovementServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductionMaterialMovementServiceImpl(
                batchMapper,
                bomItemMapper,
                movementMapper,
                movementItemMapper,
                materialMapper,
                inventoryReceiptService
        );
    }

    @Test
    void pickRejectsWhenQuantityExceedsBomPlan() {
        ProductionBatch batch = batch();
        Material material = material();
        ProductionBomItem bomItem = bomItem(material.getId(), "2.00");
        ProductionMaterialMovementRequest request = request(batch.getId(), material.getId(), "21.00");

        when(batchMapper.selectById(batch.getId())).thenReturn(batch);
        when(materialMapper.selectById(material.getId())).thenReturn(material);
        when(movementMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(bomItemMapper.selectByBomId(batch.getBomId())).thenReturn(List.of(bomItem));

        assertThrows(BizException.class, () -> service.pickMaterials(request));

        verify(movementMapper, never()).insert(any(ProductionMaterialMovement.class));
        verify(inventoryReceiptService, never()).createIssue(any());
    }

    @Test
    void returnRejectsWhenQuantityExceedsNetPicked() {
        ProductionBatch batch = batch();
        Material material = material();
        ProductionMaterialMovement existingPick = movement("PICK", batch.getId());
        ProductionMaterialMovementItem existingItem = movementItem(existingPick.getId(), material.getId(), "2.00");
        ProductionMaterialMovementRequest request = request(batch.getId(), material.getId(), "3.00");

        when(batchMapper.selectById(batch.getId())).thenReturn(batch);
        when(materialMapper.selectById(material.getId())).thenReturn(material);
        when(movementMapper.selectList(any(Wrapper.class))).thenReturn(List.of(existingPick));
        when(movementItemMapper.selectByMovementId(existingPick.getId())).thenReturn(List.of(existingItem));

        assertThrows(BizException.class, () -> service.returnMaterials(request));

        verify(movementMapper, never()).insert(any(ProductionMaterialMovement.class));
        verify(inventoryReceiptService, never()).createReceipt(any());
    }

    @Test
    void pickCreatesProductionMovementAndInventoryIssue() {
        ProductionBatch batch = batch();
        Material material = material();
        ProductionBomItem bomItem = bomItem(material.getId(), "2.00");
        ProductionMaterialMovementRequest request = request(batch.getId(), material.getId(), "5.00");
        InventoryIssueVO issue = new InventoryIssueVO();
        issue.setId(UUID.randomUUID());
        issue.setIssueNo("ISS-001");

        when(batchMapper.selectById(batch.getId())).thenReturn(batch);
        when(materialMapper.selectById(material.getId())).thenReturn(material);
        when(movementMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(bomItemMapper.selectByBomId(batch.getBomId())).thenReturn(List.of(bomItem));
        when(inventoryReceiptService.createIssue(any())).thenReturn(issue);

        service.pickMaterials(request);

        ArgumentCaptor<ProductionMaterialMovement> movementCaptor = ArgumentCaptor.forClass(ProductionMaterialMovement.class);
        verify(movementMapper).insert(movementCaptor.capture());
        verify(movementItemMapper).insert(any(ProductionMaterialMovementItem.class));
        verify(inventoryReceiptService).createIssue(any());
        verify(movementMapper).updateById(movementCaptor.getValue());
        assertEquals("PICK", movementCaptor.getValue().getMovementType());
        assertEquals(new BigDecimal("5.00"), movementCaptor.getValue().getTotalQuantity());
        assertEquals(issue.getId(), movementCaptor.getValue().getInventoryDocumentId());
        assertEquals("ISS-001", movementCaptor.getValue().getInventoryDocumentNo());
    }

    private ProductionMaterialMovementRequest request(UUID batchId, UUID materialId, String quantity) {
        ProductionMaterialMovementRequest request = new ProductionMaterialMovementRequest();
        request.setBatchId(batchId);
        request.setWarehouseCode("MAIN");
        request.setLocationCode("DEFAULT");
        ProductionMaterialMovementRequest.Item item = new ProductionMaterialMovementRequest.Item();
        item.setMaterialId(materialId);
        item.setQuantity(new BigDecimal(quantity));
        request.setItems(List.of(item));
        return request;
    }

    private ProductionBatch batch() {
        ProductionBatch batch = new ProductionBatch();
        batch.setId(UUID.randomUUID());
        batch.setBatchNo("WO-001");
        batch.setBomId(UUID.randomUUID());
        batch.setPlannedQuantity(new BigDecimal("10.00"));
        batch.setStatus(ProductionBatchStatusMachine.IN_PROGRESS);
        return batch;
    }

    private Material material() {
        Material material = new Material();
        material.setId(UUID.randomUUID());
        material.setCode("M-001");
        material.setName("测试原料");
        return material;
    }

    private ProductionBomItem bomItem(UUID materialId, String quantity) {
        ProductionBomItem item = new ProductionBomItem();
        item.setId(UUID.randomUUID());
        item.setMaterialId(materialId);
        item.setQuantity(new BigDecimal(quantity));
        item.setLossRate(BigDecimal.ZERO);
        return item;
    }

    private ProductionMaterialMovement movement(String movementType, UUID batchId) {
        ProductionMaterialMovement movement = new ProductionMaterialMovement();
        movement.setId(UUID.randomUUID());
        movement.setMovementType(movementType);
        movement.setBatchId(batchId);
        movement.setStatus("COMPLETED");
        return movement;
    }

    private ProductionMaterialMovementItem movementItem(UUID movementId, UUID materialId, String quantity) {
        ProductionMaterialMovementItem item = new ProductionMaterialMovementItem();
        item.setId(UUID.randomUUID());
        item.setMovementId(movementId);
        item.setMaterialId(materialId);
        item.setQuantity(new BigDecimal(quantity));
        return item;
    }
}
