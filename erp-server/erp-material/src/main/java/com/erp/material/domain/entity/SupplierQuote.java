package com.erp.material.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.erp.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@TableName("supplier_quote")
public class SupplierQuote extends BaseEntity {
    @TableId
    private UUID id;
    private UUID supplierId;
    private UUID materialId;
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
    public UUID getMaterialId() { return materialId; }
    public void setMaterialId(UUID materialId) { this.materialId = materialId; }
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
