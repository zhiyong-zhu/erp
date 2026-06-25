package com.erp.inventory.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.erp.common.core.exception.BizException;
import com.erp.inventory.domain.dto.InventoryLocationRequest;
import com.erp.inventory.domain.dto.InventoryWarehouseRequest;
import com.erp.inventory.domain.entity.InventoryLocation;
import com.erp.inventory.domain.entity.InventoryWarehouse;
import com.erp.inventory.domain.vo.InventoryLocationVO;
import com.erp.inventory.domain.vo.InventoryWarehouseVO;
import com.erp.inventory.mapper.InventoryLocationMapper;
import com.erp.inventory.mapper.InventoryWarehouseMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryWarehouseServiceImplTest {
    @Mock private InventoryWarehouseMapper warehouseMapper;
    @Mock private InventoryLocationMapper locationMapper;

    private InventoryWarehouseServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new InventoryWarehouseServiceImpl(warehouseMapper, locationMapper);
    }

    @Test
    void saveWarehouseCreatesActiveWarehouse() {
        InventoryWarehouseRequest request = warehouseRequest("MAIN", "主仓");

        InventoryWarehouseVO result = service.saveWarehouse(null, request);

        assertEquals("MAIN", result.getCode());
        assertEquals("主仓", result.getName());
        assertEquals(1, result.getStatus());
        verify(warehouseMapper).insert(any(InventoryWarehouse.class));
    }

    @Test
    void saveWarehouseRejectsDuplicatedCode() {
        InventoryWarehouseRequest request = warehouseRequest("MAIN", "主仓");
        InventoryWarehouse existing = new InventoryWarehouse();
        existing.setId(UUID.randomUUID());
        existing.setCode("MAIN");
        when(warehouseMapper.selectOne(any(Wrapper.class))).thenReturn(existing);

        assertThrows(BizException.class, () -> service.saveWarehouse(null, request));
    }

    @Test
    void saveLocationCopiesWarehouseSnapshot() {
        InventoryWarehouse warehouse = new InventoryWarehouse();
        warehouse.setId(UUID.randomUUID());
        warehouse.setCode("MAIN");
        warehouse.setName("主仓");
        when(warehouseMapper.selectById(warehouse.getId())).thenReturn(warehouse);

        InventoryLocationRequest request = locationRequest(warehouse.getId(), "A-01", "A区一号位");
        InventoryLocationVO result = service.saveLocation(null, request);

        assertEquals(warehouse.getCode(), result.getWarehouseCode());
        assertEquals(warehouse.getName(), result.getWarehouseName());
        assertEquals("A-01", result.getCode());
        verify(locationMapper).insert(any(InventoryLocation.class));
    }

    @Test
    void saveLocationRejectsMissingWarehouse() {
        InventoryLocationRequest request = locationRequest(UUID.randomUUID(), "A-01", "A区一号位");

        assertThrows(BizException.class, () -> service.saveLocation(null, request));
    }

    private InventoryWarehouseRequest warehouseRequest(String code, String name) {
        InventoryWarehouseRequest request = new InventoryWarehouseRequest();
        request.setCode(code);
        request.setName(name);
        return request;
    }

    private InventoryLocationRequest locationRequest(UUID warehouseId, String code, String name) {
        InventoryLocationRequest request = new InventoryLocationRequest();
        request.setWarehouseId(warehouseId);
        request.setCode(code);
        request.setName(name);
        return request;
    }
}
