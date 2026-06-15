package com.erp.production.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ProductionBomRequest {
    @NotBlank
    private String code;
    @NotNull
    private UUID productId;
    @NotBlank
    private String version;
    private Integer status;
    private LocalDate effectiveDate;
    private String remark;
    @Valid
    private List<ProductionBomItemRequest> items;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<ProductionBomItemRequest> getItems() { return items; }
    public void setItems(List<ProductionBomItemRequest> items) { this.items = items; }
}
