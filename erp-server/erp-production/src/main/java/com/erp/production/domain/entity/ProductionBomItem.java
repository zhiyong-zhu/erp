package com.erp.production.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.UUID;

@TableName("production_bom_item")
public class ProductionBomItem {
    @TableId
    private UUID id;
    private UUID bomId;
    private UUID materialId;
    private Integer itemType;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal lossRate;
    private Integer processStepNo;
    private String remark;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getBomId() { return bomId; }
    public void setBomId(UUID bomId) { this.bomId = bomId; }
    public UUID getMaterialId() { return materialId; }
    public void setMaterialId(UUID materialId) { this.materialId = materialId; }
    public Integer getItemType() { return itemType; }
    public void setItemType(Integer itemType) { this.itemType = itemType; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public BigDecimal getLossRate() { return lossRate; }
    public void setLossRate(BigDecimal lossRate) { this.lossRate = lossRate; }
    public Integer getProcessStepNo() { return processStepNo; }
    public void setProcessStepNo(Integer processStepNo) { this.processStepNo = processStepNo; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
