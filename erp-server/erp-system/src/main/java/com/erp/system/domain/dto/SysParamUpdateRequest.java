package com.erp.system.domain.dto;

import jakarta.validation.constraints.NotBlank;

public class SysParamUpdateRequest {
    @NotBlank(message = "参数值不能为空")
    private String value;
    private String description;
    private Integer status;

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
