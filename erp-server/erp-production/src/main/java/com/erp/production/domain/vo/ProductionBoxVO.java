package com.erp.production.domain.vo;

import com.erp.common.core.domain.BaseVO;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class ProductionBoxVO extends BaseVO {
    private UUID id;
    private String boxCode;
    private UUID batchId;
    private String batchNo;
    private UUID productId;
    private String productCode;
    private String productName;
    private UUID packageId;
    private String packageName;
    private Integer packageLevel;
    private BigDecimal quantity;
    private List<String> serialNos;
    private String labelHtml;
    private String status;
    private String remark;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getBoxCode() { return boxCode; }
    public void setBoxCode(String boxCode) { this.boxCode = boxCode; }
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
    public UUID getPackageId() { return packageId; }
    public void setPackageId(UUID packageId) { this.packageId = packageId; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public Integer getPackageLevel() { return packageLevel; }
    public void setPackageLevel(Integer packageLevel) { this.packageLevel = packageLevel; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public List<String> getSerialNos() { return serialNos; }
    public void setSerialNos(List<String> serialNos) { this.serialNos = serialNos; }
    public String getLabelHtml() { return labelHtml; }
    public void setLabelHtml(String labelHtml) { this.labelHtml = labelHtml; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
