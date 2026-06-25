package com.erp.production.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.erp.inventory.domain.entity.InventoryTransaction;
import com.erp.inventory.domain.vo.InventoryIssueVO;
import com.erp.inventory.mapper.InventoryTransactionMapper;
import com.erp.inventory.service.InventoryReceiptService;
import com.erp.material.domain.entity.Material;
import com.erp.material.mapper.MaterialMapper;
import com.erp.product.domain.entity.Product;
import com.erp.product.domain.entity.ProductPackage;
import com.erp.product.mapper.ProductMapper;
import com.erp.product.mapper.ProductPackageMapper;
import com.erp.production.domain.ProductionBatchStatusMachine;
import com.erp.production.domain.dto.ProductionBoxRequest;
import com.erp.production.domain.dto.ProductionMaterialMovementRequest;
import com.erp.production.domain.dto.ProductionReportRequest;
import com.erp.production.domain.entity.ProductionBatch;
import com.erp.production.domain.entity.ProductionBomItem;
import com.erp.production.domain.entity.ProductionBox;
import com.erp.production.domain.entity.ProductionMaterialMovement;
import com.erp.production.domain.entity.ProductionMaterialMovementItem;
import com.erp.production.domain.entity.ProductionProductStock;
import com.erp.production.domain.entity.ProductionReport;
import com.erp.production.mapper.ProductionBatchMapper;
import com.erp.production.mapper.ProductionBomItemMapper;
import com.erp.production.mapper.ProductionBomMapper;
import com.erp.production.mapper.ProductionBoxMapper;
import com.erp.production.mapper.ProductionMaterialMovementItemMapper;
import com.erp.production.mapper.ProductionMaterialMovementMapper;
import com.erp.production.mapper.ProductionProcessMapper;
import com.erp.production.mapper.ProductionProcessStepMapper;
import com.erp.production.mapper.ProductionProductStockMapper;
import com.erp.production.mapper.ProductionReportMapper;
import com.erp.production.mapper.SerialNumberMapper;
import com.erp.production.service.ProductionSerialNumberService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductionWorkflowClosureTest {
    @Mock private ProductMapper productMapper;
    @Mock private ProductPackageMapper productPackageMapper;
    @Mock private MaterialMapper materialMapper;
    @Mock private ProductionProcessMapper processMapper;
    @Mock private ProductionProcessStepMapper processStepMapper;
    @Mock private ProductionBomMapper bomMapper;
    @Mock private ProductionBomItemMapper bomItemMapper;
    @Mock private ProductionBatchMapper batchMapper;
    @Mock private ProductionBoxMapper boxMapper;
    @Mock private ProductionProductStockMapper productStockMapper;
    @Mock private ProductionReportMapper reportMapper;
    @Mock private SerialNumberMapper serialNumberMapper;
    @Mock private InventoryTransactionMapper inventoryTransactionMapper;
    @Mock private ProductionSerialNumberService serialNumberService;
    @Mock private ProductionMaterialMovementMapper movementMapper;
    @Mock private ProductionMaterialMovementItemMapper movementItemMapper;
    @Mock private InventoryReceiptService inventoryReceiptService;

    private ProductionServiceImpl productionService;
    private ProductionMaterialMovementServiceImpl movementService;

    @BeforeEach
    void setUp() {
        productionService = new ProductionServiceImpl(
                productMapper,
                productPackageMapper,
                materialMapper,
                processMapper,
                processStepMapper,
                bomMapper,
                bomItemMapper,
                batchMapper,
                boxMapper,
                productStockMapper,
                reportMapper,
                serialNumberMapper,
                inventoryTransactionMapper,
                serialNumberService
        );
        movementService = new ProductionMaterialMovementServiceImpl(
                batchMapper,
                bomItemMapper,
                movementMapper,
                movementItemMapper,
                materialMapper,
                inventoryReceiptService
        );
    }

    @Test
    void productionMainWorkflowClosesFromStartToReceipt() {
        ProductionBatch batch = batch();
        Product product = product(batch.getProductId());
        Material material = material();
        ProductPackage productPackage = productPackage(batch.getProductId());
        ProductionBomItem bomItem = bomItem(material.getId(), "2.00");
        InventoryIssueVO issue = new InventoryIssueVO();
        issue.setId(UUID.randomUUID());
        issue.setIssueNo("ISS-001");

        when(batchMapper.selectById(batch.getId())).thenReturn(batch);
        when(bomItemMapper.selectByBomId(batch.getBomId())).thenReturn(List.of(bomItem));
        when(materialMapper.selectById(material.getId())).thenReturn(material);
        when(movementMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(inventoryReceiptService.createIssue(any())).thenReturn(issue);
        when(productMapper.selectById(batch.getProductId())).thenReturn(product);
        when(productPackageMapper.selectById(productPackage.getId())).thenReturn(productPackage);
        when(boxMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        productionService.startBatch(batch.getId());
        movementService.pickMaterials(pickRequest(batch.getId(), material.getId(), "20.00"));
        productionService.createReport(reportRequest(batch.getId(), "10.00", "10.00", "0.00"));
        productionService.packBox(boxRequest(batch.getId(), productPackage.getId(), "10.00"));
        productionService.receiveBatch(batch.getId());

        assertEquals(ProductionBatchStatusMachine.CLOSED, batch.getStatus());
        assertEquals(new BigDecimal("10.00"), batch.getCompletedQuantity());
        verify(batchMapper, times(3)).updateById(batch);
        verify(inventoryReceiptService).createIssue(any());
        verify(movementMapper).insert(any(ProductionMaterialMovement.class));
        verify(movementItemMapper).insert(any(ProductionMaterialMovementItem.class));
        verify(reportMapper).insert(any(ProductionReport.class));
        verify(boxMapper).insert(any(ProductionBox.class));
        verify(productStockMapper).insert(any(ProductionProductStock.class));
        verify(inventoryTransactionMapper).insert(any(InventoryTransaction.class));
        verify(serialNumberService).markBatchStocked(any(), any(), any());
    }

    private ProductionMaterialMovementRequest pickRequest(UUID batchId, UUID materialId, String quantity) {
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

    private ProductionReportRequest reportRequest(UUID batchId, String reportQuantity, String goodQuantity, String defectQuantity) {
        ProductionReportRequest request = new ProductionReportRequest();
        request.setBatchId(batchId);
        request.setReportQuantity(new BigDecimal(reportQuantity));
        request.setGoodQuantity(new BigDecimal(goodQuantity));
        request.setDefectQuantity(new BigDecimal(defectQuantity));
        return request;
    }

    private ProductionBoxRequest boxRequest(UUID batchId, UUID packageId, String quantity) {
        ProductionBoxRequest request = new ProductionBoxRequest();
        request.setBatchId(batchId);
        request.setPackageId(packageId);
        request.setQuantity(new BigDecimal(quantity));
        return request;
    }

    private ProductionBatch batch() {
        ProductionBatch batch = new ProductionBatch();
        batch.setId(UUID.randomUUID());
        batch.setBatchNo("WO-001");
        batch.setProductId(UUID.randomUUID());
        batch.setBomId(UUID.randomUUID());
        batch.setPlannedQuantity(new BigDecimal("10.00"));
        batch.setCompletedQuantity(BigDecimal.ZERO);
        batch.setStatus(ProductionBatchStatusMachine.RELEASED);
        return batch;
    }

    private Product product(UUID productId) {
        Product product = new Product();
        product.setId(productId);
        product.setCode("P-001");
        product.setName("测试产品");
        product.setDeleted(false);
        return product;
    }

    private ProductPackage productPackage(UUID productId) {
        ProductPackage productPackage = new ProductPackage();
        productPackage.setId(UUID.randomUUID());
        productPackage.setProductId(productId);
        productPackage.setName("标准箱");
        productPackage.setLevel(1);
        productPackage.setQuantity(1);
        return productPackage;
    }

    private Material material() {
        Material material = new Material();
        material.setId(UUID.randomUUID());
        material.setCode("M-001");
        material.setName("测试原料");
        material.setCurrentStock(new BigDecimal("20.00"));
        return material;
    }

    private ProductionBomItem bomItem(UUID materialId, String quantity) {
        ProductionBomItem bomItem = new ProductionBomItem();
        bomItem.setId(UUID.randomUUID());
        bomItem.setMaterialId(materialId);
        bomItem.setQuantity(new BigDecimal(quantity));
        bomItem.setLossRate(BigDecimal.ZERO);
        return bomItem;
    }
}
