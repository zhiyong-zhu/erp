package com.erp.purchase.domain.vo;

import java.time.OffsetDateTime;
import java.util.UUID;

public class PurchaseExceptionVO {
    private UUID id;
    private String exceptionNo;
    private UUID purchaseOrderId;
    private UUID purchaseOrderItemId;
    private String supplierName;
    private String materialCode;
    private String materialName;
    private String exceptionType;
    private String status;
    private String description;
    private String resolution;
    private OffsetDateTime createdAt;
    private OffsetDateTime handledAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getExceptionNo() { return exceptionNo; }
    public void setExceptionNo(String exceptionNo) { this.exceptionNo = exceptionNo; }
    public UUID getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(UUID purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public UUID getPurchaseOrderItemId() { return purchaseOrderItemId; }
    public void setPurchaseOrderItemId(UUID purchaseOrderItemId) { this.purchaseOrderItemId = purchaseOrderItemId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getExceptionType() { return exceptionType; }
    public void setExceptionType(String exceptionType) { this.exceptionType = exceptionType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(OffsetDateTime handledAt) { this.handledAt = handledAt; }
}
