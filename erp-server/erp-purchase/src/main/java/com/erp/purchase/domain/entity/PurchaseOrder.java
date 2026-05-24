package com.erp.purchase.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.erp.common.core.domain.BaseEntity;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@TableName("purchase_order")
public class PurchaseOrder extends BaseEntity {
    @TableId
    private UUID id;
    private String orderNo;
    private UUID supplierId;
    private String supplierName;
    private String orderType;
    private String status;
    private BigDecimal totalAmount;
    private String sourceType;
    private String remark;
    private OffsetDateTime receivedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getOrderType() { return orderType; }
    public void setOrderType(String orderType) { this.orderType = orderType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public OffsetDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(OffsetDateTime receivedAt) { this.receivedAt = receivedAt; }
}
