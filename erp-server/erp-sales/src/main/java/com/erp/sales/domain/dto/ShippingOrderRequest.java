package com.erp.sales.domain.dto;

import com.erp.common.core.domain.BaseDTO;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ShippingOrderRequest extends BaseDTO {
    private String carrierCode;
    private String carrierName;
    private String trackingNumber;
    private String remark;
    private List<Item> items;

    public String getCarrierCode() { return carrierCode; }
    public void setCarrierCode(String carrierCode) { this.carrierCode = carrierCode; }
    public String getCarrierName() { return carrierName; }
    public void setCarrierName(String carrierName) { this.carrierName = carrierName; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public static class Item {
        private UUID saleOrderItemId;
        private BigDecimal quantity;
        private List<String> serialNos;

        public UUID getSaleOrderItemId() { return saleOrderItemId; }
        public void setSaleOrderItemId(UUID saleOrderItemId) { this.saleOrderItemId = saleOrderItemId; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public List<String> getSerialNos() { return serialNos; }
        public void setSerialNos(List<String> serialNos) { this.serialNos = serialNos; }
    }
}
