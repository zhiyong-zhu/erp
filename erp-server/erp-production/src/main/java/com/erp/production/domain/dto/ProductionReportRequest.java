package com.erp.production.domain.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class ProductionReportRequest {
    @NotNull
    private UUID batchId;
    private String reportNo;
    @NotNull
    @DecimalMin("0.0001")
    private BigDecimal reportQuantity;
    private BigDecimal goodQuantity;
    private BigDecimal defectQuantity;
    private String reportAt;
    private String operatorName;
    private String remark;

    public UUID getBatchId() { return batchId; }
    public void setBatchId(UUID batchId) { this.batchId = batchId; }
    public String getReportNo() { return reportNo; }
    public void setReportNo(String reportNo) { this.reportNo = reportNo; }
    public BigDecimal getReportQuantity() { return reportQuantity; }
    public void setReportQuantity(BigDecimal reportQuantity) { this.reportQuantity = reportQuantity; }
    public BigDecimal getGoodQuantity() { return goodQuantity; }
    public void setGoodQuantity(BigDecimal goodQuantity) { this.goodQuantity = goodQuantity; }
    public BigDecimal getDefectQuantity() { return defectQuantity; }
    public void setDefectQuantity(BigDecimal defectQuantity) { this.defectQuantity = defectQuantity; }
    public String getReportAt() { return reportAt; }
    public void setReportAt(String reportAt) { this.reportAt = reportAt; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
