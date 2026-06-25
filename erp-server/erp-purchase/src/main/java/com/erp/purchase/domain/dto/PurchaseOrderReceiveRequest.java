package com.erp.purchase.domain.dto;

import com.erp.common.core.domain.BaseDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class PurchaseOrderReceiveRequest extends BaseDTO {
    @Valid
    @NotEmpty(message = "收货明细不能为空")
    private List<ReceiveItem> items;
    private String idempotencyKey;

    public List<ReceiveItem> getItems() { return items; }
    public void setItems(List<ReceiveItem> items) { this.items = items; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }

    public static class ReceiveItem {
        @NotNull(message = "采购明细不能为空")
        private UUID itemId;
        @NotNull(message = "收货数量不能为空")
        private BigDecimal receivedQuantity;
        @NotNull(message = "合格数量不能为空")
        private BigDecimal acceptedQuantity;
        private BigDecimal rejectedQuantity;
        private String inspectionResult;
        private String exceptionReason;

        public UUID getItemId() { return itemId; }
        public void setItemId(UUID itemId) { this.itemId = itemId; }
        public BigDecimal getReceivedQuantity() { return receivedQuantity; }
        public void setReceivedQuantity(BigDecimal receivedQuantity) { this.receivedQuantity = receivedQuantity; }
        public BigDecimal getAcceptedQuantity() { return acceptedQuantity; }
        public void setAcceptedQuantity(BigDecimal acceptedQuantity) { this.acceptedQuantity = acceptedQuantity; }
        public BigDecimal getRejectedQuantity() { return rejectedQuantity; }
        public void setRejectedQuantity(BigDecimal rejectedQuantity) { this.rejectedQuantity = rejectedQuantity; }
        public String getInspectionResult() { return inspectionResult; }
        public void setInspectionResult(String inspectionResult) { this.inspectionResult = inspectionResult; }
        public String getExceptionReason() { return exceptionReason; }
        public void setExceptionReason(String exceptionReason) { this.exceptionReason = exceptionReason; }
    }
}
