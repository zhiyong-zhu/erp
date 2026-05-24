package com.erp.purchase.domain.vo;

import java.math.BigDecimal;
import java.util.UUID;

public class PurchaseReturnItemVO {
    private UUID id;
    private UUID purchaseOrderItemId;
    private UUID materialId;
    private String materialCode;
    private String materialName;
    private String unit;
    private BigDecimal returnQuantity;
    private BigDecimal quotePrice;
    private BigDecimal returnAmount;
    private String reason;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPurchaseOrderItemId() { return purchaseOrderItemId; }
    public void setPurchaseOrderItemId(UUID purchaseOrderItemId) { this.purchaseOrderItemId = purchaseOrderItemId; }
    public UUID getMaterialId() { return materialId; }
    public void setMaterialId(UUID materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getReturnQuantity() { return returnQuantity; }
    public void setReturnQuantity(BigDecimal returnQuantity) { this.returnQuantity = returnQuantity; }
    public BigDecimal getQuotePrice() { return quotePrice; }
    public void setQuotePrice(BigDecimal quotePrice) { this.quotePrice = quotePrice; }
    public BigDecimal getReturnAmount() { return returnAmount; }
    public void setReturnAmount(BigDecimal returnAmount) { this.returnAmount = returnAmount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
