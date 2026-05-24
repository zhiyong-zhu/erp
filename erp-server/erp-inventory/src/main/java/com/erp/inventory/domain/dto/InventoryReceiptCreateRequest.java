package com.erp.inventory.domain.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class InventoryReceiptCreateRequest {
    private String sourceType;
    private UUID sourceOrderId;
    private String sourceOrderNo;
    private UUID supplierId;
    private String supplierName;
    private String remark;
    private List<Item> items;

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public UUID getSourceOrderId() { return sourceOrderId; }
    public void setSourceOrderId(UUID sourceOrderId) { this.sourceOrderId = sourceOrderId; }
    public String getSourceOrderNo() { return sourceOrderNo; }
    public void setSourceOrderNo(String sourceOrderNo) { this.sourceOrderNo = sourceOrderNo; }
    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public static class Item {
        private UUID materialId;
        private String materialCode;
        private String materialName;
        private UUID sourceItemId;
        private BigDecimal quantity;

        public UUID getMaterialId() { return materialId; }
        public void setMaterialId(UUID materialId) { this.materialId = materialId; }
        public String getMaterialCode() { return materialCode; }
        public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
        public String getMaterialName() { return materialName; }
        public void setMaterialName(String materialName) { this.materialName = materialName; }
        public UUID getSourceItemId() { return sourceItemId; }
        public void setSourceItemId(UUID sourceItemId) { this.sourceItemId = sourceItemId; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    }
}
