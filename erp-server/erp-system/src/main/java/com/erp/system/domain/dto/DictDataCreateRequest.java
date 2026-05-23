package com.erp.system.domain.dto;

import jakarta.validation.constraints.NotBlank;

public class DictDataCreateRequest {
    @NotBlank(message = "字典标签不能为空")
    private String label;
    @NotBlank(message = "字典值不能为空")
    private String value;
    private Integer sortOrder;
    private String cssClass;
    private Integer status;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getCssClass() {
        return cssClass;
    }

    public void setCssClass(String cssClass) {
        this.cssClass = cssClass;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
