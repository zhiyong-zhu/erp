package com.erp.purchase.domain.vo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class PurchaseReturnVO {
    private UUID id;
    private String returnNo;
    private UUID purchaseOrderId;
    private String purchaseOrderNo;
    private UUID supplierId;
    private String supplierName;
    private String status;
    private BigDecimal totalAmount;
    private String remark;
    private OffsetDateTime createdAt;
    private List<PurchaseReturnItemVO> items;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getReturnNo() { return returnNo; }
    public void setReturnNo(String returnNo) { this.returnNo = returnNo; }
    public UUID getPurchaseOrderId() { return purchaseOrderId; }
    public void setPurchaseOrderId(UUID purchaseOrderId) { this.purchaseOrderId = purchaseOrderId; }
    public String getPurchaseOrderNo() { return purchaseOrderNo; }
    public void setPurchaseOrderNo(String purchaseOrderNo) { this.purchaseOrderNo = purchaseOrderNo; }
    public UUID getSupplierId() { return supplierId; }
    public void setSupplierId(UUID supplierId) { this.supplierId = supplierId; }
    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public List<PurchaseReturnItemVO> getItems() { return items; }
    public void setItems(List<PurchaseReturnItemVO> items) { this.items = items; }
}
