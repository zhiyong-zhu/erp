package com.erp.material.domain.dto;

import com.erp.common.core.domain.BaseDTO;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.UUID;

public class MaterialRequest extends BaseDTO {
    @NotBlank(message = "原料编码不能为空")
    private String code;
    @NotBlank(message = "原料名称不能为空")
    private String name;
    private UUID categoryId;
    @NotBlank(message = "单位不能为空")
    private String unit;
    private String specifications;
    private UUID defaultSupplierId;
    private BigDecimal safetyStock;
    private BigDecimal currentStock;
    private Integer leadTimeDays;
    private Integer status;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    public UUID getDefaultSupplierId() { return defaultSupplierId; }
    public void setDefaultSupplierId(UUID defaultSupplierId) { this.defaultSupplierId = defaultSupplierId; }
    public BigDecimal getSafetyStock() { return safetyStock; }
    public void setSafetyStock(BigDecimal safetyStock) { this.safetyStock = safetyStock; }
    public BigDecimal getCurrentStock() { return currentStock; }
    public void setCurrentStock(BigDecimal currentStock) { this.currentStock = currentStock; }
    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
