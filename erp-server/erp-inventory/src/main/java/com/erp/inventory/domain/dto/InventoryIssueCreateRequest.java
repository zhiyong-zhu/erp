package com.erp.inventory.domain.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class InventoryIssueCreateRequest {
    private String issueType;
    private UUID sourceOrderId;
    private String sourceOrderNo;
    private String remark;
    private List<Item> items;

    public String getIssueType() { return issueType; }
    public void setIssueType(String issueType) { this.issueType = issueType; }
    public UUID getSourceOrderId() { return sourceOrderId; }
    public void setSourceOrderId(UUID sourceOrderId) { this.sourceOrderId = sourceOrderId; }
    public String getSourceOrderNo() { return sourceOrderNo; }
    public void setSourceOrderNo(String sourceOrderNo) { this.sourceOrderNo = sourceOrderNo; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items; }

    public static class Item {
        private UUID materialId;
        private BigDecimal quantity;
        private String remark;

        public UUID getMaterialId() { return materialId; }
        public void setMaterialId(UUID materialId) { this.materialId = materialId; }
        public BigDecimal getQuantity() { return quantity; }
        public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }
}
