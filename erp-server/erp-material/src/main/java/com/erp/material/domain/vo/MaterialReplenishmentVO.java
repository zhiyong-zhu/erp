package com.erp.material.domain.vo;

import java.math.BigDecimal;
import java.util.UUID;

public class MaterialReplenishmentVO {
    private UUID materialId;
    private String materialCode;
    private String materialName;
    private String unit;
    private BigDecimal currentStock;
    private BigDecimal safetyStock;
    private BigDecimal shortageAmount;
    private BigDecimal suggestedQuantity;
    private UUID supplierId;
    private String supplierName;
    private BigDecimal quotePrice;
    private String currency;
    private BigDecimal estimatedAmount;
    private Integer leadTimeDays;
    private String recommendationReason;

    public UUID getMaterialId() { return materialId; }
    public void setMaterialId(UUID materialId) { this.materialId = materialId; }
    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getCurrentStock() { return currentStock; }
    public void setCurrentStock(BigDecimal currentStock) { this.currentStock = currentStock; }
    public BigDecimal getSafetyStock() { return safetyStock; }
    public void setSafetyStock(BigDecimal safetyStock) { this.safetyStock = safetyStock; }
    public BigDecimal getShortageAmount() { return shortageAmount; }
    public void setShortageAmount(BigDecimal shortageAmount) { this.shortageAmount = shortageAmount; }
    public BigDecimal getSuggestedQuantity() { return suggestedQuantity; }
    public void setSuggestedQuantity(BigDecimal suggestedQuantity) { this.suggestedQuantity = suggestedQuantity; }
    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public BigDecimal getQuotePrice() { return quotePrice; }
    public void setQuotePrice(BigDecimal quotePrice) { this.quotePrice = quotePrice; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(BigDecimal estimatedAmount) { this.estimatedAmount = estimatedAmount; }
    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }
    public String getRecommendationReason() { return recommendationReason; }
    public void setRecommendationReason(String recommendationReason) { this.recommendationReason = recommendationReason; }
}
