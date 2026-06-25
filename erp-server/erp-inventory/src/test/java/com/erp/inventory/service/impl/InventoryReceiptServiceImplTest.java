package com.erp.inventory.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.erp.inventory.domain.dto.InventoryCheckCreateRequest;
import com.erp.inventory.domain.dto.InventoryIssueCreateRequest;
import com.erp.inventory.domain.dto.InventoryReceiptCreateRequest;
import com.erp.inventory.domain.entity.InventoryBalance;
import com.erp.inventory.domain.entity.InventoryCheck;
import com.erp.inventory.domain.entity.InventoryCheckItem;
import com.erp.inventory.domain.entity.InventoryIssue;
import com.erp.inventory.domain.entity.InventoryReceipt;
import com.erp.inventory.domain.entity.InventoryTransaction;
import com.erp.inventory.mapper.InventoryCheckItemMapper;
import com.erp.inventory.mapper.InventoryCheckMapper;
import com.erp.inventory.mapper.InventoryBalanceMapper;
import com.erp.inventory.mapper.InventoryIssueMapper;
import com.erp.inventory.mapper.InventoryReceiptMapper;
import com.erp.inventory.mapper.InventoryTransactionMapper;
import com.erp.inventory.mapper.InventoryTransferItemMapper;
import com.erp.inventory.mapper.InventoryTransferMapper;
import com.erp.inventory.service.InventoryBalanceService;
import com.erp.material.domain.entity.Material;
import com.erp.material.mapper.MaterialMapper;
import java.math.BigDecimal;
import java.util.UUID;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryReceiptServiceImplTest {
    @Mock private InventoryCheckMapper inventoryCheckMapper;
    @Mock private InventoryCheckItemMapper inventoryCheckItemMapper;
    @Mock private InventoryIssueMapper inventoryIssueMapper;
    @Mock private InventoryReceiptMapper inventoryReceiptMapper;
    @Mock private InventoryTransactionMapper inventoryTransactionMapper;
    @Mock private InventoryTransferMapper inventoryTransferMapper;
    @Mock private InventoryTransferItemMapper inventoryTransferItemMapper;
    @Mock private InventoryBalanceMapper inventoryBalanceMapper;
    @Mock private MaterialMapper materialMapper;
    @Mock private InventoryBalanceService inventoryBalanceService;

    private InventoryReceiptServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InventoryReceiptServiceImpl(
                inventoryCheckMapper,
                inventoryCheckItemMapper,
                inventoryIssueMapper,
                inventoryReceiptMapper,
                inventoryTransactionMapper,
                inventoryTransferMapper,
                inventoryTransferItemMapper,
                inventoryBalanceMapper,
                materialMapper,
                inventoryBalanceService
        );
    }

    @Test
    void createReceiptReturnsExistingIdForDuplicateIdempotencyKey() {
        UUID receiptId = UUID.randomUUID();
        InventoryReceipt existing = new InventoryReceipt();
        existing.setId(receiptId);
        when(inventoryReceiptMapper.selectByIdempotencyKey("receive-1")).thenReturn(existing);

        InventoryReceiptCreateRequest request = new InventoryReceiptCreateRequest();
        request.setIdempotencyKey(" receive-1 ");

        UUID result = service.createReceipt(request);

        assertEquals(receiptId, result);
        verify(inventoryReceiptMapper, never()).insert(org.mockito.ArgumentMatchers.any(InventoryReceipt.class));
    }

    @Test
    void createIssueReturnsExistingIssueForDuplicateIdempotencyKey() {
        UUID issueId = UUID.randomUUID();
        InventoryIssue existing = new InventoryIssue();
        existing.setId(issueId);
        existing.setIssueNo("ISS-001");
        when(inventoryIssueMapper.selectByIdempotencyKey("issue-1")).thenReturn(existing);

        InventoryIssueCreateRequest request = new InventoryIssueCreateRequest();
        request.setIdempotencyKey(" issue-1 ");

        assertEquals(issueId, service.createIssue(request).getId());
        verify(inventoryIssueMapper, never()).insert(org.mockito.ArgumentMatchers.any(InventoryIssue.class));
    }

    @Test
    void createReceiptUsesManualInTransactionTypeForManualSource() {
        UUID materialId = UUID.randomUUID();
        Material material = new Material();
        material.setId(materialId);
        material.setCode("MAT-001");
        material.setName("测试原料");
        material.setCurrentStock(BigDecimal.ZERO);

        InventoryBalance balance = new InventoryBalance();
        balance.setWarehouseCode("WH");
        balance.setWarehouseName("仓库");
        balance.setLocationCode("LOC");
        balance.setLocationName("库位");
        balance.setBatchNo("BATCH");
        balance.setAvailableQuantity(new BigDecimal("10.00"));

        when(materialMapper.selectById(materialId)).thenReturn(material);
        when(inventoryBalanceService.increase(any(Material.class), any(BigDecimal.class), any())).thenReturn(balance);

        InventoryReceiptCreateRequest.Item item = new InventoryReceiptCreateRequest.Item();
        item.setMaterialId(materialId);
        item.setMaterialCode("MAT-001");
        item.setMaterialName("测试原料");
        item.setQuantity(new BigDecimal("10.00"));

        InventoryReceiptCreateRequest request = new InventoryReceiptCreateRequest();
        request.setSourceType("MANUAL");
        request.setItems(java.util.List.of(item));

        service.createReceipt(request);

        ArgumentCaptor<InventoryTransaction> captor = ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(inventoryTransactionMapper).insert(captor.capture());
        assertEquals("MANUAL_IN", captor.getValue().getTransactionType());
    }

    @Test
    void createReceiptKeepsPurchaseInTransactionTypeForPurchaseSource() {
        UUID materialId = UUID.randomUUID();
        Material material = new Material();
        material.setId(materialId);
        material.setCode("MAT-001");
        material.setName("测试原料");
        material.setCurrentStock(BigDecimal.ZERO);

        InventoryBalance balance = new InventoryBalance();
        balance.setWarehouseCode("WH");
        balance.setWarehouseName("仓库");
        balance.setLocationCode("LOC");
        balance.setLocationName("库位");
        balance.setBatchNo("BATCH");
        balance.setAvailableQuantity(new BigDecimal("10.00"));

        when(materialMapper.selectById(materialId)).thenReturn(material);
        when(inventoryBalanceService.increase(any(Material.class), any(BigDecimal.class), any())).thenReturn(balance);

        InventoryReceiptCreateRequest.Item item = new InventoryReceiptCreateRequest.Item();
        item.setMaterialId(materialId);
        item.setMaterialCode("MAT-001");
        item.setMaterialName("测试原料");
        item.setQuantity(new BigDecimal("10.00"));

        InventoryReceiptCreateRequest request = new InventoryReceiptCreateRequest();
        request.setSourceType("PURCHASE");
        request.setItems(java.util.List.of(item));

        service.createReceipt(request);

        ArgumentCaptor<InventoryTransaction> captor = ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(inventoryTransactionMapper).insert(captor.capture());
        assertEquals("PURCHASE_IN", captor.getValue().getTransactionType());
    }

    @Test
    void createCheckFreezesInventoryAndDoesNotCreateAdjustmentTransaction() {
        UUID materialId = UUID.randomUUID();
        Material material = material(materialId, new BigDecimal("10.00"));
        InventoryBalance balance = balance(new BigDecimal("10.00"));

        when(materialMapper.selectById(materialId)).thenReturn(material);
        when(inventoryBalanceMapper.selectOne(any(Wrapper.class))).thenReturn(balance);
        when(inventoryBalanceService.freezeForCheck(any(Material.class), eq(new BigDecimal("10.00")), any())).thenReturn(balance);

        InventoryCheckCreateRequest.Item item = new InventoryCheckCreateRequest.Item();
        item.setMaterialId(materialId);
        item.setActualQuantity(new BigDecimal("12.00"));

        InventoryCheckCreateRequest request = new InventoryCheckCreateRequest();
        request.setItems(java.util.List.of(item));

        assertEquals("FROZEN", service.createCheck(request).getStatus());
        verify(inventoryBalanceService).freezeForCheck(any(Material.class), eq(new BigDecimal("10.00")), any());
        verify(inventoryBalanceService, never()).adjustTo(any(), any(), any());
        verify(inventoryTransactionMapper, never()).insert(any(InventoryTransaction.class));
    }

    @Test
    void approveCheckCreatesAdjustmentTransactionAfterReview() {
        UUID checkId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();
        InventoryCheck check = new InventoryCheck();
        check.setId(checkId);
        check.setCheckNo("CHK-001");
        check.setStatus("REVIEWED");

        InventoryCheckItem item = new InventoryCheckItem();
        item.setId(UUID.randomUUID());
        item.setCheckId(checkId);
        item.setMaterialId(materialId);
        item.setMaterialCode("MAT-001");
        item.setMaterialName("测试原料");
        item.setWarehouseCode("MAIN");
        item.setWarehouseName("主仓");
        item.setLocationCode("DEFAULT");
        item.setLocationName("默认库位");
        item.setBatchNo("DEFAULT");
        item.setSystemQuantity(new BigDecimal("10.00"));
        item.setActualQuantity(new BigDecimal("12.00"));
        item.setDifferenceQuantity(new BigDecimal("2.00"));

        Material material = material(materialId, new BigDecimal("10.00"));
        InventoryBalance balance = balance(new BigDecimal("12.00"));

        when(inventoryCheckMapper.selectById(checkId)).thenReturn(check);
        when(inventoryCheckItemMapper.selectList(any(Wrapper.class))).thenReturn(java.util.List.of(item));
        when(materialMapper.selectById(materialId)).thenReturn(material);
        when(inventoryBalanceService.approveCheckAdjustment(any(Material.class), eq(new BigDecimal("10.00")), eq(new BigDecimal("12.00")), any())).thenReturn(balance);

        assertEquals("APPROVED", service.approveCheck(checkId, "同意").getStatus());

        ArgumentCaptor<InventoryTransaction> captor = ArgumentCaptor.forClass(InventoryTransaction.class);
        verify(inventoryTransactionMapper).insert(captor.capture());
        assertEquals("CHECK_PROFIT", captor.getValue().getTransactionType());
        assertEquals(new BigDecimal("2.00"), captor.getValue().getQuantity());
        assertEquals(checkId, captor.getValue().getCheckId());
    }

    private Material material(UUID materialId, BigDecimal currentStock) {
        Material material = new Material();
        material.setId(materialId);
        material.setCode("MAT-001");
        material.setName("测试原料");
        material.setCurrentStock(currentStock);
        return material;
    }

    private InventoryBalance balance(BigDecimal availableQuantity) {
        InventoryBalance balance = new InventoryBalance();
        balance.setWarehouseCode("MAIN");
        balance.setWarehouseName("主仓");
        balance.setLocationCode("DEFAULT");
        balance.setLocationName("默认库位");
        balance.setBatchNo("DEFAULT");
        balance.setAvailableQuantity(availableQuantity);
        return balance;
    }
}
