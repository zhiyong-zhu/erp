package com.erp.purchase.domain.dto;

import com.erp.common.core.domain.BaseDTO;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class PurchaseOrderItemUpdateRequest extends BaseDTO {
    private UUID id;
    @NotNull(message = "原料不能为空")
    private UUID materialId;
    @NotBlank(message = "原料编码不能为空")
    private String materialCode;
    @NotBlank(message = "原料名称不能为空")
    private String materialName;
    private String unit;
    @NotNull(message = "采购数量不能为空")
    @DecimalMin(value = "0.01", message = "采购数量必须大于0")
    private BigDecimal quantity;
    private BigDecimal quotePrice;
    private BigDecimal estimatedAmount;
    private Integer leadTimeDays;
    private String sourceType;
    private UUID sourceRefId;
    private BigDecimal receivedQuantity;

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
}
