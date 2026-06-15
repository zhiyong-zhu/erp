package com.erp.production.domain.vo;

import com.erp.common.core.domain.BaseVO;
import java.util.List;
import java.util.UUID;

public class ProductionProcessVO extends BaseVO {
    private UUID id;
    private String code;
    private String name;
    private UUID productId;
    private String productName;
    private String version;
    private Integer status;
    private String remark;
    private List<ProductionProcessStepVO> steps;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<ProductionProcessStepVO> getSteps() { return steps; }
    public void setSteps(List<ProductionProcessStepVO> steps) { this.steps = steps; }
}
