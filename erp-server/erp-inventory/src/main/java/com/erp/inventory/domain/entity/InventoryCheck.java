package com.erp.inventory.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@TableName("inventory_check")
public class InventoryCheck {
    @TableId
    private UUID id;
    private String checkNo;
    private String checkType;
    private String status;
    private BigDecimal totalDifference;
    private String remark;
    private UUID createdBy;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCheckNo() { return checkNo; }
    public void setCheckNo(String checkNo) { this.checkNo = checkNo; }
    public String getCheckType() { return checkType; }
    public void setCheckType(String checkType) { this.checkType = checkType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalDifference() { return totalDifference; }
    public void setTotalDifference(BigDecimal totalDifference) { this.totalDifference = totalDifference; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
