package com.erp.production.domain.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

public class ProductionProcessRequest {
    @NotBlank
    private String code;
    @NotBlank
    private String name;
    private UUID productId;
    @NotBlank
    private String version;
    private Integer status;
    private String remark;
    @Valid
    private List<ProductionProcessStepRequest> steps;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<ProductionProcessStepRequest> getSteps() { return steps; }
    public void setSteps(List<ProductionProcessStepRequest> steps) { this.steps = steps; }
}
