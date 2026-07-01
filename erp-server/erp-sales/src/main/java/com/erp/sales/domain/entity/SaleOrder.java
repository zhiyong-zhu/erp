package com.erp.sales.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.erp.common.core.domain.BaseEntity;
import com.erp.common.mybatis.type.JsonbStringTypeHandler;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@TableName(value = "sale_order", autoResultMap = true)
public class SaleOrder extends BaseEntity {
    @TableId
    private UUID id;
    private String orderNo;
    private UUID customerId;
    private String customerName;
    private String orderSource;
    private String platformOrderNo;
    @TableField(typeHandler = JsonbStringTypeHandler.class)
    private String platformData;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal freightAmount;
    private BigDecimal payableAmount;
    private BigDecimal paidAmount;
    private String paymentStatus;
    private String shippingAddress;
    private String remark;
    private OffsetDateTime orderedAt;
    private OffsetDateTime paidAt;
    private OffsetDateTime shippedAt;
    private OffsetDateTime completedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getOrderSource() { return orderSource; }
    public void setOrderSource(String orderSource) { this.orderSource = orderSource; }
    public String getPlatformOrderNo() { return platformOrderNo; }
    public void setPlatformOrderNo(String platformOrderNo) { this.platformOrderNo = platformOrderNo; }
    public String getPlatformData() { return platformData; }
    public void setPlatformData(String platformData) { this.platformData = platformData; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getFreightAmount() { return freightAmount; }
    public void setFreightAmount(BigDecimal freightAmount) { this.freightAmount = freightAmount; }
    public BigDecimal getPayableAmount() { return payableAmount; }
    public void setPayableAmount(BigDecimal payableAmount) { this.payableAmount = payableAmount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public void setPaidAmount(BigDecimal paidAmount) { this.paidAmount = paidAmount; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public OffsetDateTime getOrderedAt() { return orderedAt; }
    public void setOrderedAt(OffsetDateTime orderedAt) { this.orderedAt = orderedAt; }
    public OffsetDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(OffsetDateTime paidAt) { this.paidAt = paidAt; }
    public OffsetDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(OffsetDateTime shippedAt) { this.shippedAt = shippedAt; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; }
}
