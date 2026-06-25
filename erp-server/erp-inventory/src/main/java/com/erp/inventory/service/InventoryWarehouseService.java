package com.erp.inventory.service;

import com.erp.common.core.domain.PageVO;
import com.erp.inventory.domain.dto.InventoryLocationRequest;
import com.erp.inventory.domain.dto.InventoryWarehouseRequest;
import com.erp.inventory.domain.vo.InventoryLocationVO;
import com.erp.inventory.domain.vo.InventoryWarehouseVO;
import java.util.UUID;

public interface InventoryWarehouseService {
    PageVO<InventoryWarehouseVO> listWarehouses(long pageNum, long pageSize, String keyword, Integer status);

    InventoryWarehouseVO saveWarehouse(UUID id, InventoryWarehouseRequest request);

    PageVO<InventoryLocationVO> listLocations(long pageNum, long pageSize, UUID warehouseId, String keyword, Integer status);

    InventoryLocationVO saveLocation(UUID id, InventoryLocationRequest request);
}
