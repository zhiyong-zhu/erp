package com.erp.production.domain.vo;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductionProcessStepVO {
    private UUID id;
    private Integer stepNo;
    private String name;
    private String workstation;
    private BigDecimal standardMinutes;
    private String qualityRequirement;
    private String remark;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Integer getStepNo() { return stepNo; }
    public void setStepNo(Integer stepNo) { this.stepNo = stepNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getWorkstation() { return workstation; }
    public void setWorkstation(String workstation) { this.workstation = workstation; }
    public BigDecimal getStandardMinutes() { return standardMinutes; }
    public void setStandardMinutes(BigDecimal standardMinutes) { this.standardMinutes = standardMinutes; }
    public String getQualityRequirement() { return qualityRequirement; }
    public void setQualityRequirement(String qualityRequirement) { this.qualityRequirement = qualityRequirement; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
