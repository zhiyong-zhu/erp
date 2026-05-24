package com.erp.inventory.domain.vo;

import java.time.OffsetDateTime;
import java.util.UUID;

public class InventoryReceiptVO {
    private UUID id;
    private String receiptNo;
    private String sourceType;
    private UUID sourceOrderId;
    private String sourceOrderNo;
    private UUID supplierId;
    private String supplierName;
    private String status;
    private String remark;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getReceiptNo() { return receiptNo; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }
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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
