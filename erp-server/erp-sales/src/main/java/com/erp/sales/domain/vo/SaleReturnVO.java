package com.erp.sales.domain.vo;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class SaleReturnVO {
    private UUID id;
    private String returnNo;
    private UUID saleOrderId;
    private String saleOrderNo;
    private UUID customerId;
    private String customerName;
    private String status;
    private BigDecimal totalAmount;
    private String reason;
    private String remark;
    private List<SaleReturnItemVO> items;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getReturnNo() { return returnNo; }
    public void setReturnNo(String returnNo) { this.returnNo = returnNo; }
    public UUID getSaleOrderId() { return saleOrderId; }
    public void setSaleOrderId(UUID saleOrderId) { this.saleOrderId = saleOrderId; }
    public String getSaleOrderNo() { return saleOrderNo; }
    public void setSaleOrderNo(String saleOrderNo) { this.saleOrderNo = saleOrderNo; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<SaleReturnItemVO> getItems() { return items; }
    public void setItems(List<SaleReturnItemVO> items) { this.items = items; }
}
