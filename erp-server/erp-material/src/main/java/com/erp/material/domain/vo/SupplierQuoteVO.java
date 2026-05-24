package com.erp.material.domain.vo;

import com.erp.common.core.domain.BaseVO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class SupplierQuoteVO extends BaseVO {
    private UUID id;
    private UUID supplierId;
    private String supplierName;
    private UUID materialId;
    private String materialName;
    private BigDecimal quotePrice;
    private String currency;
    private BigDecimal minOrderQuantity;
    private Integer leadTimeDays;
    private String remark;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public UUID getMaterialId() { return materialId; }
    public void setMaterialId(UUID materialId) { this.materialId = materialId; }
    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }
    public BigDecimal getQuotePrice() { return quotePrice; }
    public void setQuotePrice(BigDecimal quotePrice) { this.quotePrice = quotePrice; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getMinOrderQuantity() { return minOrderQuantity; }
    public void setMinOrderQuantity(BigDecimal minOrderQuantity) { this.minOrderQuantity = minOrderQuantity; }
    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
}
