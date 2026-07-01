package com.erp.sales.domain.vo;

import com.erp.common.core.domain.BaseVO;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class SaleOrderVO extends BaseVO {
    private UUID id;
    private String orderNo;
    private UUID customerId;
    private String customerName;
    private String orderSource;
    private String platformOrderNo;
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
    /** 是否存在未处理的销售异常（OPEN），用于前端确认门禁 */
    private boolean hasOpenException;
    private List<SaleOrderItemVO> items;
    private List<ShippingVO> shippingOrders;

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
    public boolean isHasOpenException() { return hasOpenException; }
    public void setHasOpenException(boolean hasOpenException) { this.hasOpenException = hasOpenException; }
    public List<SaleOrderItemVO> getItems() { return items; }
    public void setItems(List<SaleOrderItemVO> items) { this.items = items; }
    public List<ShippingVO> getShippingOrders() { return shippingOrders; }
    public void setShippingOrders(List<ShippingVO> shippingOrders) { this.shippingOrders = shippingOrders; }
}
