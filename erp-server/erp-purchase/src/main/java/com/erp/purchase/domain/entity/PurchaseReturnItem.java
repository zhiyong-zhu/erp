package com.erp.purchase.domain.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("purchase_return_item")
public class PurchaseReturnItem {
    @TableId
    private UUID id;
    private UUID purchaseReturnId;
    private UUID purchaseOrderItemId;
    private UUID materialId;
    private String materialCode;
    private String materialName;
    private String unit;
    private BigDecimal returnQuantity;
    private BigDecimal quotePrice;
    private BigDecimal returnAmount;
    private String reason;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getPurchaseReturnId() { return purchaseReturnId; }
    public void setPurchaseReturnId(UUID purchaseReturnId) { this.purchaseReturnId = purchaseReturnId; }
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
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
