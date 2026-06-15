package com.erp.production.domain.vo;

import com.erp.common.core.domain.BaseVO;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public class ProductionReportVO extends BaseVO {
    private UUID id;
    private String reportNo;
    private UUID batchId;
    private String batchNo;
    private UUID productId;
    private String productCode;
    private String productName;
    private BigDecimal reportQuantity;
    private BigDecimal goodQuantity;
    private BigDecimal defectQuantity;
    private OffsetDateTime reportAt;
    private String operatorName;
    private String status;
    private String remark;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getReportNo() { return reportNo; }
    public void setReportNo(String reportNo) { this.reportNo = reportNo; }
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
    public BigDecimal getReportQuantity() { return reportQuantity; }
    public void setReportQuantity(BigDecimal reportQuantity) { this.reportQuantity = reportQuantity; }
    public BigDecimal getGoodQuantity() { return goodQuantity; }
    public void setGoodQuantity(BigDecimal goodQuantity) { this.goodQuantity = goodQuantity; }
    public BigDecimal getDefectQuantity() { return defectQuantity; }
    public void setDefectQuantity(BigDecimal defectQuantity) { this.defectQuantity = defectQuantity; }
    public OffsetDateTime getReportAt() { return reportAt; }
    public void setReportAt(OffsetDateTime reportAt) { this.reportAt = reportAt; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
