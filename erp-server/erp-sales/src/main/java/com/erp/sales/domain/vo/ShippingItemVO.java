package com.erp.sales.domain.vo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class ShippingItemVO {
    private UUID id;
    private UUID shippingOrderId;
    private UUID saleOrderItemId;
    private UUID skuId;
    private String skuCode;
    private String productName;
    private BigDecimal quantity;
    private List<String> serialNos;
    private OffsetDateTime createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getShippingOrderId() { return shippingOrderId; }
    public void setShippingOrderId(UUID shippingOrderId) { this.shippingOrderId = shippingOrderId; }
    public UUID getSaleOrderItemId() { return saleOrderItemId; }
    public void setSaleOrderItemId(UUID saleOrderItemId) { this.saleOrderItemId = saleOrderItemId; }
    public UUID getSkuId() { return skuId; }
    public void setSkuId(UUID skuId) { this.skuId = skuId; }
    public String getSkuCode() { return skuCode; }
    public void setSkuCode(String skuCode) { this.skuCode = skuCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public List<String> getSerialNos() { return serialNos; }
    public void setSerialNos(List<String> serialNos) { this.serialNos = serialNos; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
