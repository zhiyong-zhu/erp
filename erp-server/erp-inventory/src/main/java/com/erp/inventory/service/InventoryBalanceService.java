package com.erp.inventory.service;

import com.erp.inventory.domain.entity.InventoryBalance;
import com.erp.material.domain.entity.Material;
import java.math.BigDecimal;

public interface InventoryBalanceService {
    String DEFAULT_WAREHOUSE_CODE = "MAIN";
    String DEFAULT_WAREHOUSE_NAME = "主仓";
    String DEFAULT_LOCATION_CODE = "DEFAULT";
    String DEFAULT_LOCATION_NAME = "默认库位";
    String DEFAULT_BATCH_NO = "DEFAULT";

    InventoryBalance increase(Material material, BigDecimal quantity, InventoryPosition position);

    InventoryBalance decrease(Material material, BigDecimal quantity, InventoryPosition position);

    InventoryBalance adjustTo(Material material, BigDecimal actualQuantity, InventoryPosition position);

    InventoryBalance freezeForCheck(Material material, BigDecimal quantity, InventoryPosition position);

    InventoryBalance approveCheckAdjustment(Material material, BigDecimal systemQuantity, BigDecimal actualQuantity, InventoryPosition position);

    InventoryBalance releaseCheckFreeze(Material material, BigDecimal quantity, InventoryPosition position);

    record InventoryPosition(
            String warehouseCode,
            String warehouseName,
            String locationCode,
            String locationName,
            String batchNo
    ) {
        public static InventoryPosition defaults() {
            return new InventoryPosition(
                    DEFAULT_WAREHOUSE_CODE,
                    DEFAULT_WAREHOUSE_NAME,
                    DEFAULT_LOCATION_CODE,
                    DEFAULT_LOCATION_NAME,
                    DEFAULT_BATCH_NO
            );
        }

        public InventoryPosition normalized() {
            return new InventoryPosition(
                    hasText(warehouseCode) ? warehouseCode : DEFAULT_WAREHOUSE_CODE,
                    hasText(warehouseName) ? warehouseName : DEFAULT_WAREHOUSE_NAME,
                    hasText(locationCode) ? locationCode : DEFAULT_LOCATION_CODE,
                    hasText(locationName) ? locationName : DEFAULT_LOCATION_NAME,
                    hasText(batchNo) ? batchNo : DEFAULT_BATCH_NO
            );
        }

        private static boolean hasText(String value) {
            return value != null && !value.isBlank();
        }
    }
}
