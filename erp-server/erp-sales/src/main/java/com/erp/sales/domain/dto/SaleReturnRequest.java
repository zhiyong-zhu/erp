package com.erp.sales.domain.dto;

import com.erp.common.core.domain.BaseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class SaleReturnRequest extends BaseDTO {
    @NotNull(message = "销售订单不能为空")
    private UUID saleOrderId;
    @NotEmpty(message = "退货明细不能为空")
    @Valid
    private List<Item> items;
    private String remark;

    public UUID getSaleOrderId() { return saleOrderId; }
    public void setSaleOrderId(UUID saleOrderId) { this.saleOrderId = saleOrderId; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public static class Item {
        private UUID saleOrderItemId;
        private java.math.BigDecimal quantity;
        private String reason;

        public UUID getSaleOrderItemId() { return saleOrderItemId; }
        public void setSaleOrderItemId(UUID saleOrderItemId) { this.saleOrderItemId = saleOrderItemId; }
        public java.math.BigDecimal getQuantity() { return quantity; }
        public void setQuantity(java.math.BigDecimal quantity) { this.quantity = quantity; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
