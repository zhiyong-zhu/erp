package com.erp.purchase.domain.vo;

import java.math.BigDecimal;
import java.util.UUID;

public class PurchasePayableStatVO {
    private UUID supplierId;
    private String supplierName;
    private BigDecimal orderAmount;
    private BigDecimal returnAmount;
    private BigDecimal netPayableAmount;
    private Long orderCount;
    private Long returnCount;

    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public BigDecimal getOrderAmount() { return orderAmount; }
    public void setOrderAmount(BigDecimal orderAmount) { this.orderAmount = orderAmount; }
    public BigDecimal getReturnAmount() { return returnAmount; }
    public void setReturnAmount(BigDecimal returnAmount) { this.returnAmount = returnAmount; }
    public BigDecimal getNetPayableAmount() { return netPayableAmount; }
    public void setNetPayableAmount(BigDecimal netPayableAmount) { this.netPayableAmount = netPayableAmount; }
    public Long getOrderCount() { return orderCount; }
    public void setOrderCount(Long orderCount) { this.orderCount = orderCount; }
    public Long getReturnCount() { return returnCount; }
    public void setReturnCount(Long returnCount) { this.returnCount = returnCount; }
}
