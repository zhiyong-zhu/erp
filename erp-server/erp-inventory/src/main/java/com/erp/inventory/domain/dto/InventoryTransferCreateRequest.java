package com.erp.inventory.domain.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class InventoryTransferCreateRequest {
    private String fromWarehouseCode;
    private String fromWarehouseName;
    private String fromLocation;
    private String fromLocationCode;
    private String fromLocationName;
    private String fromBatchNo;
    private String toWarehouseCode;
    private String toWarehouseName;
    private String toLocation;
    private String toLocationCode;
    private String toLocationName;
    private String toBatchNo;
    private String remark;
    private List<Item> items;

    public String getFromWarehouseCode() { return fromWarehouseCode; }
    public void setFromWarehouseCode(String fromWarehouseCode) { this.fromWarehouseCode = fromWarehouseCode; }
    public String getFromWarehouseName() { return fromWarehouseName; }
    public void setFromWarehouseName(String fromWarehouseName) { this.fromWarehouseName = fromWarehouseName; }
    public String getFromLocation() { return fromLocation; }
    public void setFromLocation(String fromLocation) { this.fromLocation = fromLocation; }
    public String getFromLocationCode() { return fromLocationCode; }
    public void setFromLocationCode(String fromLocationCode) { this.fromLocationCode = fromLocationCode; }
    public String getFromLocationName() { return fromLocationName; }
    public void setFromLocationName(String fromLocationName) { this.fromLocationName = fromLocationName; }
    public String getFromBatchNo() { return fromBatchNo; }
    public void setFromBatchNo(String fromBatchNo) { this.fromBatchNo = fromBatchNo; }
    public String getToWarehouseCode() { return toWarehouseCode; }
    public void setToWarehouseCode(String toWarehouseCode) { this.toWarehouseCode = toWarehouseCode; }
    public String getToWarehouseName() { return toWarehouseName; }
    public void setToWarehouseName(String toWarehouseName) { this.toWarehouseName = toWarehouseName; }
    public String getToLocation() { return toLocation; }
    public void setToLocation(String toLocation) { this.toLocation = toLocation; }
    public String getToLocationCode() { return toLocationCode; }
    public void setToLocationCode(String toLocationCode) { this.toLocationCode = toLocationCode; }
    public String getToLocationName() { return toLocationName; }
    public void setToLocationName(String toLocationName) { this.toLocationName = toLocationName; }
    public String getToBatchNo() { return toBatchNo; }
    public void setToBatchNo(String toBatchNo) { this.toBatchNo = toBatchNo; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public static class Item {
        private UUID materialId;
        private BigDecimal quantity;
        private String fromWarehouseCode;
        private String fromWarehouseName;
        private String fromLocationCode;
        private String fromLocationName;
        private String fromBatchNo;
        private String toWarehouseCode;
        private String toWarehouseName;
        private String toLocationCode;
        private String toLocationName;
        private String toBatchNo;
        private String remark;

        public UUID getMaterialId() { return materialId; }
        public void setMaterialId(UUID materialId) { this.materialId = materialId; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public String getFromWarehouseCode() { return fromWarehouseCode; }
        public void setFromWarehouseCode(String fromWarehouseCode) { this.fromWarehouseCode = fromWarehouseCode; }
        public String getFromWarehouseName() { return fromWarehouseName; }
        public void setFromWarehouseName(String fromWarehouseName) { this.fromWarehouseName = fromWarehouseName; }
        public String getFromLocationCode() { return fromLocationCode; }
        public void setFromLocationCode(String fromLocationCode) { this.fromLocationCode = fromLocationCode; }
        public String getFromLocationName() { return fromLocationName; }
        public void setFromLocationName(String fromLocationName) { this.fromLocationName = fromLocationName; }
        public String getFromBatchNo() { return fromBatchNo; }
        public void setFromBatchNo(String fromBatchNo) { this.fromBatchNo = fromBatchNo; }
        public String getToWarehouseCode() { return toWarehouseCode; }
        public void setToWarehouseCode(String toWarehouseCode) { this.toWarehouseCode = toWarehouseCode; }
        public String getToWarehouseName() { return toWarehouseName; }
        public void setToWarehouseName(String toWarehouseName) { this.toWarehouseName = toWarehouseName; }
        public String getToLocationCode() { return toLocationCode; }
        public void setToLocationCode(String toLocationCode) { this.toLocationCode = toLocationCode; }
        public String getToLocationName() { return toLocationName; }
        public void setToLocationName(String toLocationName) { this.toLocationName = toLocationName; }
        public String getToBatchNo() { return toBatchNo; }
        public void setToBatchNo(String toBatchNo) { this.toBatchNo = toBatchNo; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }
}
