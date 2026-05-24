package com.erp.purchase.domain.vo;

import java.math.BigDecimal;
import java.util.UUID;

public class PurchaseOrderItemVO {
    private UUID id;
    private UUID materialId;
    private String materialCode;
    private String materialName;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal quotePrice;
    private BigDecimal estimatedAmount;
    private Integer leadTimeDays;
    private String sourceType;
    private UUID sourceRefId;
    private BigDecimal receivedQuantity;
    private BigDecimal acceptedQuantity;
    private BigDecimal rejectedQuantity;
    private BigDecimal returnedQuantity;
    private String inspectionResult;
    private String exceptionReason;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getMaterialId() { return materialId; }
    public void setMaterialId(UUID materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getQuotePrice() { return quotePrice; }
    public void setQuotePrice(BigDecimal quotePrice) { this.quotePrice = quotePrice; }
    public BigDecimal getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(BigDecimal estimatedAmount) { this.estimatedAmount = estimatedAmount; }
    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public UUID getSourceRefId() { return sourceRefId; }
    public void setSourceRefId(UUID sourceRefId) { this.sourceRefId = sourceRefId; }
    public BigDecimal getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(BigDecimal receivedQuantity) { this.receivedQuantity = receivedQuantity; }
    public BigDecimal getAcceptedQuantity() { return acceptedQuantity; }
    public void setAcceptedQuantity(BigDecimal acceptedQuantity) { this.acceptedQuantity = acceptedQuantity; }
    public BigDecimal getRejectedQuantity() { return rejectedQuantity; }
    public void setRejectedQuantity(BigDecimal rejectedQuantity) { this.rejectedQuantity = rejectedQuantity; }
    public BigDecimal getReturnedQuantity() { return returnedQuantity; }
    public void setReturnedQuantity(BigDecimal returnedQuantity) { this.returnedQuantity = returnedQuantity; }
    public String getInspectionResult() { return inspectionResult; }
    public void setInspectionResult(String inspectionResult) { this.inspectionResult = inspectionResult; }
    public String getExceptionReason() { return exceptionReason; }
    public void setExceptionReason(String exceptionReason) { this.exceptionReason = exceptionReason; }
}
