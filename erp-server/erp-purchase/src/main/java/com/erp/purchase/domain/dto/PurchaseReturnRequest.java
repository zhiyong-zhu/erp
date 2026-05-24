package com.erp.purchase.domain.dto;

import com.erp.common.core.domain.BaseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PurchaseReturnRequest extends BaseDTO {
    @Valid
    @NotEmpty(message = "退货明细不能为空")
    private List<ReturnItem> items;
    private String remark;

    public List<ReturnItem> getItems() { return items; }
    public void setItems(List<ReturnItem> items) { this.items = items; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public static class ReturnItem {
        @NotNull(message = "采购明细不能为空")
        private UUID itemId;
        @NotNull(message = "退货数量不能为空")
        private BigDecimal returnQuantity;
        private String reason;

        public UUID getItemId() { return itemId; }
        public void setItemId(UUID itemId) { this.itemId = itemId; }
        public BigDecimal getReturnQuantity() { return returnQuantity; }
        public void setReturnQuantity(BigDecimal returnQuantity) { this.returnQuantity = returnQuantity; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
