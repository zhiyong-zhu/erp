package com.erp.sales.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.OffsetDateTime;
import java.util.UUID;

@TableName("sale_exception")
public class SaleException {
    @TableId
    private UUID id;
    private String exceptionNo;
    private UUID saleOrderId;
    private UUID saleOrderItemId;
    private UUID saleReturnId;
    private UUID saleReturnItemId;
    private UUID customerId;
    private String customerName;
    private UUID skuId;
    private String skuCode;
    private String productName;
    private String exceptionType;
    private String status;
    private String description;
    private String resolution;
    private UUID createdBy;
    private OffsetDateTime createdAt;
    private UUID handledBy;
    private OffsetDateTime handledAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getExceptionNo() { return exceptionNo; }
    public void setExceptionNo(String exceptionNo) { this.exceptionNo = exceptionNo; }
    public UUID getSaleOrderId() { return saleOrderId; }
    public void setSaleOrderId(UUID saleOrderId) { this.saleOrderId = saleOrderId; }
    public UUID getSaleOrderItemId() { return saleOrderItemId; }
    public void setSaleOrderItemId(UUID saleOrderItemId) { this.saleOrderItemId = saleOrderItemId; }
    public UUID getSaleReturnId() { return saleReturnId; }
    public void setSaleReturnId(UUID saleReturnId) { this.saleReturnId = saleReturnId; }
    public UUID getSaleReturnItemId() { return saleReturnItemId; }
    public void setSaleReturnItemId(UUID saleReturnItemId) { this.saleReturnItemId = saleReturnItemId; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public UUID getSkuId() { return skuId; }
    public void setSkuId(UUID skuId) { this.skuId = skuId; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getExceptionType() { return exceptionType; }
    public void setExceptionType(String exceptionType) { this.exceptionType = exceptionType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public UUID getHandledBy() { return handledBy; }
    public void setHandledBy(UUID handledBy) { this.handledBy = handledBy; }
    public OffsetDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(OffsetDateTime handledAt) { this.handledAt = handledAt; }
}
