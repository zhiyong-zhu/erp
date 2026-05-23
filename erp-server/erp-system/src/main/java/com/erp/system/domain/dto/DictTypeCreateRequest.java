package com.erp.system.domain.dto;

import jakarta.validation.constraints.NotBlank;

public class DictTypeCreateRequest {
    @NotBlank(message = "字典名称不能为空")
    private String name;
    @NotBlank(message = "字典编码不能为空")
    private String code;
    private String description;
    private Integer status;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
