package com.erp.material.domain.vo;

import com.erp.common.core.domain.BaseVO;
import java.math.BigDecimal;
import java.util.UUID;

public class MaterialVO extends BaseVO {
    private UUID id;
    private String code;
    private String name;
    private UUID categoryId;
    private String categoryName;
    private String unit;
    private String specifications;
    private UUID defaultSupplierId;
    private String defaultSupplierName;
    private BigDecimal safetyStock;
    private BigDecimal currentStock;
    private Integer leadTimeDays;
    private Integer status;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getCategoryId() { return categoryId; }
    public void setCategoryId(UUID categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }
    public UUID getDefaultSupplierId() { return defaultSupplierId; }
    public void setDefaultSupplierId(UUID defaultSupplierId) { this.defaultSupplierId = defaultSupplierId; }
    public String getDefaultSupplierName() { return defaultSupplierName; }
    public void setDefaultSupplierName(String defaultSupplierName) { this.defaultSupplierName = defaultSupplierName; }
    public BigDecimal getSafetyStock() { return safetyStock; }
    public void setSafetyStock(BigDecimal safetyStock) { this.safetyStock = safetyStock; }
    public BigDecimal getCurrentStock() { return currentStock; }
    public void setCurrentStock(BigDecimal currentStock) { this.currentStock = currentStock; }
    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
