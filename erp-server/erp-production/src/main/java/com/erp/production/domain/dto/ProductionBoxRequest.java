package com.erp.production.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ProductionBoxRequest {
    @NotNull
    private UUID batchId;
    @NotNull
    private UUID packageId;
    @DecimalMin("0.0001")
    private BigDecimal quantity;
    private List<String> serialNos;
    private String remark;

    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }
    public UUID getPackageId() { return packageId; }
    public void setPackageId(UUID packageId) { this.packageId = packageId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public List<String> getSerialNos() { return serialNos; }
    public void setSerialNos(List<String> serialNos) { this.serialNos = serialNos; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
