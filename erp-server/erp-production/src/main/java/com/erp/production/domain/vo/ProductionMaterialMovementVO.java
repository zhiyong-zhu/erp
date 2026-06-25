package com.erp.production.domain.vo;

import com.erp.common.core.domain.BaseVO;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ProductionMaterialMovementVO extends BaseVO {
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
    private String remark;
    private List<ProductionMaterialMovementItemVO> items;

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
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<ProductionMaterialMovementItemVO> getItems() { return items; }
    public void setItems(List<ProductionMaterialMovementItemVO> items) { this.items = items; }
}
