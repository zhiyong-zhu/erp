package com.erp.material.domain.dto;

import com.erp.common.core.domain.BaseDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class SupplierQuoteRequest extends BaseDTO {
    @NotNull(message = "供应商不能为空")
    private UUID supplierId;
    @NotNull(message = "原料不能为空")
    private UUID materialId;
    @NotNull(message = "报价不能为空")
    @DecimalMin(value = "0.01", message = "报价必须大于0")
    private BigDecimal quotePrice;
    @NotBlank(message = "币种不能为空")
    private String currency;
    private BigDecimal minOrderQuantity;
    private Integer leadTimeDays;
    private String remark;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;

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
