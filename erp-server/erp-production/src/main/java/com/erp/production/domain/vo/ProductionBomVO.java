package com.erp.production.domain.vo;

import com.erp.common.core.domain.BaseVO;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ProductionBomVO extends BaseVO {
    private UUID id;
    private String code;
    private UUID productId;
    private String productCode;
    private String productName;
    private String version;
    private Integer status;
    private LocalDate effectiveDate;
    private String remark;
    private List<ProductionBomItemVO> items;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public void setEffectiveDate(LocalDate effectiveDate) { this.effectiveDate = effectiveDate; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<ProductionBomItemVO> getItems() { return items; }
    public void setItems(List<ProductionBomItemVO> items) { this.items = items; }
}
