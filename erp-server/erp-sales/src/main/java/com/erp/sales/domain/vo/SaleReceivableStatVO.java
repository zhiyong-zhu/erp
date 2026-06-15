package com.erp.sales.domain.vo;

import java.math.BigDecimal;
import java.util.UUID;

public class SaleReceivableStatVO {
    private UUID customerId;
    private String customerName;
    private BigDecimal orderAmount;
    private BigDecimal returnAmount;
    private BigDecimal netReceivableAmount;
    private Long orderCount;
    private Long returnCount;

    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public BigDecimal getOrderAmount() { return orderAmount; }
    public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }
    public BigDecimal getReturnAmount() { return returnAmount; }
    public void setReturnAmount(BigDecimal returnAmount) { this.returnAmount = returnAmount; }
    public BigDecimal getNetReceivableAmount() { return netReceivableAmount; }
    public void setNetReceivableAmount(BigDecimal netReceivableAmount) { this.netReceivableAmount = netReceivableAmount; }
    public Long getOrderCount() { return orderCount; }
    public void setOrderCount(Long orderCount) { this.orderCount = orderCount; }
    public Long getReturnCount() { return returnCount; }
    public void setReturnCount(Long returnCount) { this.returnCount = returnCount; }
}
