package com.erp.production.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.erp.common.core.exception.BizException;
import com.erp.inventory.mapper.InventoryTransactionMapper;
import com.erp.material.domain.entity.Material;
import com.erp.material.mapper.MaterialMapper;
import com.erp.product.domain.entity.Product;
import com.erp.product.domain.entity.ProductPackage;
import com.erp.product.mapper.ProductMapper;
import com.erp.product.mapper.ProductPackageMapper;
import com.erp.production.domain.ProductionBatchStatusMachine;
import com.erp.production.domain.dto.ProductionBoxRequest;
import com.erp.production.domain.dto.ProductionReportRequest;
import com.erp.production.domain.entity.ProductionBatch;
import com.erp.production.domain.entity.ProductionBox;
import com.erp.production.domain.entity.ProductionBomItem;
import com.erp.production.domain.entity.ProductionReport;
import com.erp.production.mapper.ProductionBatchMapper;
import com.erp.production.mapper.ProductionBomItemMapper;
import com.erp.production.mapper.ProductionBomMapper;
import com.erp.production.mapper.ProductionBoxMapper;
import com.erp.production.mapper.ProductionProductStockMapper;
import com.erp.production.mapper.ProductionProcessMapper;
import com.erp.production.mapper.ProductionProcessStepMapper;
import com.erp.production.mapper.ProductionReportMapper;
import com.erp.production.mapper.SerialNumberMapper;
import com.erp.production.service.ProductionSerialNumberService;
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
class ProductionServiceImplTest {
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

    private ProductionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductionServiceImpl(
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
    }

    @Test
    void startBatchRejectsWhenBomMaterialStockIsNotEnough() {
        ProductionBatch batch = batch(new BigDecimal("10.00"), BigDecimal.ZERO);
        batch.setBomId(UUID.randomUUID());
        ProductionBomItem bomItem = bomItem(UUID.randomUUID(), "2.00");
        Material material = material(bomItem.getMaterialId(), "8.00");

        when(batchMapper.selectById(batch.getId())).thenReturn(batch);
        when(bomItemMapper.selectByBomId(batch.getBomId())).thenReturn(List.of(bomItem));
        when(materialMapper.selectById(bomItem.getMaterialId())).thenReturn(material);

        assertThrows(BizException.class, () -> service.startBatch(batch.getId()));

        verify(batchMapper, never()).updateById(any(ProductionBatch.class));
    }

    @Test
    void startBatchAcceptsWhenBomMaterialsAreAvailable() {
        ProductionBatch batch = batch(new BigDecimal("10.00"), BigDecimal.ZERO);
        batch.setStatus(ProductionBatchStatusMachine.RELEASED);
        batch.setBomId(UUID.randomUUID());
        ProductionBomItem bomItem = bomItem(UUID.randomUUID(), "2.00");
        Material material = material(bomItem.getMaterialId(), "20.00");

        when(batchMapper.selectById(batch.getId())).thenReturn(batch);
        when(bomItemMapper.selectByBomId(batch.getBomId())).thenReturn(List.of(bomItem));
        when(materialMapper.selectById(bomItem.getMaterialId())).thenReturn(material);

        service.startBatch(batch.getId());

        verify(batchMapper).updateById(batch);
        assertEquals(ProductionBatchStatusMachine.IN_PROGRESS, batch.getStatus());
    }

    @Test
    void createReportRejectsWhenGoodAndDefectDoNotEqualReportQuantity() {
        ProductionBatch batch = batch(new BigDecimal("10.00"), BigDecimal.ZERO);
        when(batchMapper.selectById(batch.getId())).thenReturn(batch);

        ProductionReportRequest request = reportRequest(batch.getId(), "5.00", "3.00", "1.00");

        assertThrows(BizException.class, () -> service.createReport(request));

        verify(reportMapper, never()).insert(any(ProductionReport.class));
        verify(batchMapper, never()).updateById(any(ProductionBatch.class));
    }

    @Test
    void createReportRejectsWhenCumulativeGoodQuantityExceedsPlan() {
        ProductionBatch batch = batch(new BigDecimal("10.00"), new BigDecimal("8.00"));
        when(batchMapper.selectById(batch.getId())).thenReturn(batch);

        ProductionReportRequest request = reportRequest(batch.getId(), "3.00", "3.00", "0.00");

        assertThrows(BizException.class, () -> service.createReport(request));

        verify(reportMapper, never()).insert(any(ProductionReport.class));
        verify(batchMapper, never()).updateById(any(ProductionBatch.class));
    }

    @Test
    void createReportAcceptsValidQuantityAndUpdatesBatchProgress() {
        ProductionBatch batch = batch(new BigDecimal("10.00"), new BigDecimal("7.00"));
        Product product = product(batch.getProductId());
        when(batchMapper.selectById(batch.getId())).thenReturn(batch);
        when(productMapper.selectById(batch.getProductId())).thenReturn(product);

        ProductionReportRequest request = reportRequest(batch.getId(), "3.00", "2.00", "1.00");
        service.createReport(request);

        ArgumentCaptor<ProductionReport> reportCaptor = ArgumentCaptor.forClass(ProductionReport.class);
        verify(reportMapper).insert(reportCaptor.capture());
        verify(batchMapper).updateById(batch);
        assertEquals(new BigDecimal("9.00"), batch.getCompletedQuantity());
        assertEquals(ProductionBatchStatusMachine.IN_PROGRESS, batch.getStatus());
        assertEquals(new BigDecimal("2.00"), reportCaptor.getValue().getGoodQuantity());
    }

    @Test
    void packBoxRejectsWhenCumulativePackedQuantityExceedsCompletedGoodQuantity() {
        ProductionBatch batch = batch(new BigDecimal("10.00"), new BigDecimal("5.00"));
        Product product = product(batch.getProductId());
        ProductPackage productPackage = productPackage(batch.getProductId());
        ProductionBox existingBox = new ProductionBox();
        existingBox.setQuantity(new BigDecimal("4.00"));

        when(batchMapper.selectById(batch.getId())).thenReturn(batch);
        when(productMapper.selectById(batch.getProductId())).thenReturn(product);
        when(productPackageMapper.selectById(productPackage.getId())).thenReturn(productPackage);
        when(boxMapper.selectList(any(Wrapper.class))).thenReturn(List.of(existingBox));

        ProductionBoxRequest request = boxRequest(batch.getId(), productPackage.getId(), "2.00");

        assertThrows(BizException.class, () -> service.packBox(request));

        verify(boxMapper, never()).insert(any(ProductionBox.class));
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

    private ProductionBatch batch(BigDecimal plannedQuantity, BigDecimal completedQuantity) {
        ProductionBatch batch = new ProductionBatch();
        batch.setId(UUID.randomUUID());
        batch.setBatchNo("WO-001");
        batch.setProductId(UUID.randomUUID());
        batch.setPlannedQuantity(plannedQuantity);
        batch.setCompletedQuantity(completedQuantity);
        batch.setStatus(ProductionBatchStatusMachine.IN_PROGRESS);
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

    private ProductionBomItem bomItem(UUID materialId, String quantity) {
        ProductionBomItem bomItem = new ProductionBomItem();
        bomItem.setId(UUID.randomUUID());
        bomItem.setMaterialId(materialId);
        bomItem.setQuantity(new BigDecimal(quantity));
        return bomItem;
    }

    private Material material(UUID materialId, String currentStock) {
        Material material = new Material();
        material.setId(materialId);
        material.setCode("M-001");
        material.setName("测试原料");
        material.setCurrentStock(new BigDecimal(currentStock));
        return material;
    }
}
