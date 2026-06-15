package com.erp.production.domain.vo;

import com.erp.common.core.domain.BaseVO;
import java.time.OffsetDateTime;
import java.util.UUID;

public class SerialNumberVO extends BaseVO {
    private UUID id;
    private String serialNo;
    private UUID batchId;
    private String batchNo;
    private UUID productId;
    private String productCode;
    private String productName;
    private String status;
    private OffsetDateTime producedAt;
    private OffsetDateTime shippedAt;
    private String remark;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getSerialNo() { return serialNo; }
    public void setSerialNo(String serialNo) { this.serialNo = serialNo; }
    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }
    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getProducedAt() { return producedAt; }
    public void setProducedAt(OffsetDateTime producedAt) { this.producedAt = producedAt; }
    public OffsetDateTime getShippedAt() { return shippedAt; }
    public void setShippedAt(OffsetDateTime shippedAt) { this.shippedAt = shippedAt; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
