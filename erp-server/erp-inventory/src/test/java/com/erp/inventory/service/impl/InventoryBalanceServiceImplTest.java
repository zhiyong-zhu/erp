package com.erp.inventory.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.erp.common.core.exception.BizException;
import com.erp.inventory.domain.entity.InventoryBalance;
import com.erp.inventory.mapper.InventoryBalanceMapper;
import com.erp.inventory.service.InventoryBalanceService.InventoryPosition;
import com.erp.material.domain.entity.Material;
import com.erp.material.mapper.MaterialMapper;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryBalanceServiceImplTest {
    @Mock
    private InventoryBalanceMapper inventoryBalanceMapper;
    @Mock
    private MaterialMapper materialMapper;

    private InventoryBalanceServiceImpl service;
    private Material material;

    @BeforeEach
    void setUp() {
        service = new InventoryBalanceServiceImpl(inventoryBalanceMapper, materialMapper);
        material = new Material();
        material.setId(UUID.randomUUID());
        material.setCode("MAT-001");
        material.setName("测试原料");
        material.setCurrentStock(BigDecimal.ZERO);
    }

    @Test
    void increaseCreatesBalanceAndSyncsMaterialStock() {
        when(inventoryBalanceMapper.selectOne(any(Wrapper.class))).thenReturn(null);

        InventoryBalance balance = service.increase(material, new BigDecimal("12.50"), InventoryPosition.defaults());

        assertEquals(new BigDecimal("12.50"), balance.getAvailableQuantity());
        assertEquals(new BigDecimal("12.50"), balance.getTotalQuantity());
        assertEquals(new BigDecimal("12.50"), material.getCurrentStock());
        verify(inventoryBalanceMapper).insert(any(InventoryBalance.class));
        verify(inventoryBalanceMapper).updateById(balance);
        verify(materialMapper).updateById(material);
    }

    @Test
    void decreaseRejectsInsufficientAvailableQuantity() {
        InventoryBalance balance = existingBalance(new BigDecimal("3.00"));
        when(inventoryBalanceMapper.selectOne(any(Wrapper.class))).thenReturn(balance);

        assertThrows(BizException.class, () -> service.decrease(material, new BigDecimal("4.00"), InventoryPosition.defaults()));

        assertEquals(new BigDecimal("3.00"), balance.getAvailableQuantity());
        assertEquals(BigDecimal.ZERO, material.getCurrentStock());
    }

    @Test
    void adjustToUpdatesBalanceAndMaterialByDifference() {
        material.setCurrentStock(new BigDecimal("5.00"));
        InventoryBalance balance = existingBalance(new BigDecimal("5.00"));
        when(inventoryBalanceMapper.selectOne(any(Wrapper.class))).thenReturn(balance);

        InventoryBalance adjusted = service.adjustTo(material, new BigDecimal("8.00"), InventoryPosition.defaults());

        assertEquals(new BigDecimal("8.00"), adjusted.getAvailableQuantity());
        assertEquals(new BigDecimal("8.00"), adjusted.getTotalQuantity());
        assertEquals(new BigDecimal("8.00"), material.getCurrentStock());
        verify(inventoryBalanceMapper).updateById(balance);
        verify(materialMapper).updateById(material);
    }

    @Test
    void decreaseUsesAtomicConditionUpdate() {
        material.setCurrentStock(new BigDecimal("5.00"));
        InventoryBalance balance = existingBalance(new BigDecimal("5.00"));
        when(inventoryBalanceMapper.selectOne(any(Wrapper.class))).thenReturn(balance);
        when(inventoryBalanceMapper.decreaseAvailableIfEnough(eq(balance.getId()), eq(new BigDecimal("2.00")), any(), any())).thenReturn(1);

        InventoryBalance decreased = service.decrease(material, new BigDecimal("2.00"), InventoryPosition.defaults());

        assertEquals(new BigDecimal("3.00"), decreased.getAvailableQuantity());
        assertEquals(new BigDecimal("3.00"), material.getCurrentStock());
        verify(inventoryBalanceMapper).decreaseAvailableIfEnough(eq(balance.getId()), eq(new BigDecimal("2.00")), any(), any());
        verify(materialMapper).updateById(material);
    }

    @Test
    void decreaseRejectsConcurrentConditionUpdateMiss() {
        material.setCurrentStock(new BigDecimal("5.00"));
        InventoryBalance balance = existingBalance(new BigDecimal("5.00"));
        when(inventoryBalanceMapper.selectOne(any(Wrapper.class))).thenReturn(balance);
        when(inventoryBalanceMapper.decreaseAvailableIfEnough(eq(balance.getId()), eq(new BigDecimal("2.00")), any(), any())).thenReturn(0);

        assertThrows(BizException.class, () -> service.decrease(material, new BigDecimal("2.00"), InventoryPosition.defaults()));

        assertEquals(new BigDecimal("5.00"), material.getCurrentStock());
    }

    @Test
    void freezeForCheckMovesAvailableToFrozenWithoutChangingMaterialStock() {
        material.setCurrentStock(new BigDecimal("5.00"));
        InventoryBalance balance = existingBalance(new BigDecimal("5.00"));
        when(inventoryBalanceMapper.selectOne(any(Wrapper.class))).thenReturn(balance);
        when(inventoryBalanceMapper.freezeQuantity(eq(balance.getId()), eq(new BigDecimal("5.00")), any(), any())).thenReturn(1);

        InventoryBalance frozen = service.freezeForCheck(material, new BigDecimal("5.00"), InventoryPosition.defaults());

        assertEquals(BigDecimal.ZERO.setScale(2), frozen.getAvailableQuantity());
        assertEquals(new BigDecimal("5.00"), frozen.getFrozenQuantity());
        assertEquals(new BigDecimal("5.00"), frozen.getTotalQuantity());
        assertEquals(new BigDecimal("5.00"), material.getCurrentStock());
    }

    @Test
    void releaseCheckFreezeMovesFrozenBackToAvailable() {
        material.setCurrentStock(new BigDecimal("5.00"));
        InventoryBalance balance = existingBalance(BigDecimal.ZERO.setScale(2));
        balance.setFrozenQuantity(new BigDecimal("5.00"));
        balance.setTotalQuantity(new BigDecimal("5.00"));
        when(inventoryBalanceMapper.selectOne(any(Wrapper.class))).thenReturn(balance);
        when(inventoryBalanceMapper.releaseFrozenQuantity(eq(balance.getId()), eq(new BigDecimal("5.00")), any(), any())).thenReturn(1);

        InventoryBalance released = service.releaseCheckFreeze(material, new BigDecimal("5.00"), InventoryPosition.defaults());

        assertEquals(new BigDecimal("5.00"), released.getAvailableQuantity());
        assertEquals(BigDecimal.ZERO.setScale(2), released.getFrozenQuantity());
        assertEquals(new BigDecimal("5.00"), released.getTotalQuantity());
        assertEquals(new BigDecimal("5.00"), material.getCurrentStock());
    }

    private InventoryBalance existingBalance(BigDecimal availableQuantity) {
        InventoryBalance balance = new InventoryBalance();
        balance.setId(UUID.randomUUID());
        balance.setMaterialId(material.getId());
        balance.setMaterialCode(material.getCode());
        balance.setMaterialName(material.getName());
        balance.setWarehouseCode("MAIN");
        balance.setWarehouseName("主仓");
        balance.setLocationCode("DEFAULT");
        balance.setLocationName("默认库位");
        balance.setBatchNo("DEFAULT");
        balance.setAvailableQuantity(availableQuantity);
        balance.setFrozenQuantity(BigDecimal.ZERO);
        balance.setTotalQuantity(availableQuantity);
        return balance;
    }
}
