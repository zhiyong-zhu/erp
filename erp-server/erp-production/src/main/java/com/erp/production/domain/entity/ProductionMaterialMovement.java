package com.erp.production.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.erp.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.util.UUID;

@TableName("production_material_movement")
public class ProductionMaterialMovement extends BaseEntity {
    @TableId
    private UUID id;
    private String movementNo;
    private String movementType;
    private UUID batchId;
    private String batchNo;
    private UUID inventoryDocumentId;
    private String inventoryDocumentNo;
    private String status;
    private BigDecimal totalQuantity;
    private String warehouseCode;
    private String warehouseName;
    private String locationCode;
    private String locationName;
    private String batchNoInventory;
    private String idempotencyKey;
    private String remark;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getMovementNo() { return movementNo; }
    public void setMovementNo(String movementNo) { this.movementNo = movementNo; }
    public String getMovementType() { return movementType; }
    public void setMovementType(String movementType) { this.movementType = movementType; }
    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public UUID getInventoryDocumentId() { return inventoryDocumentId; }
    public void setInventoryDocumentId(UUID inventoryDocumentId) { this.inventoryDocumentId = inventoryDocumentId; }
    public String getInventoryDocumentNo() { return inventoryDocumentNo; }
    public void setInventoryDocumentNo(String inventoryDocumentNo) { this.inventoryDocumentNo = inventoryDocumentNo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(BigDecimal totalQuantity) { this.totalQuantity = totalQuantity; }
    public String getWarehouseCode() { return warehouseCode; }
    public void setWarehouseCode(String warehouseCode) { this.warehouseCode = warehouseCode; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public String getLocationCode() { return locationCode; }
    public void setLocationCode(String locationCode) { this.locationCode = locationCode; }
    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }
    public String getBatchNoInventory() { return batchNoInventory; }
    public void setBatchNoInventory(String batchNoInventory) { this.batchNoInventory = batchNoInventory; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
