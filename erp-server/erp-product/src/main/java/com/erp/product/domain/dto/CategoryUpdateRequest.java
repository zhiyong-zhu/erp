package com.erp.product.domain.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class CategoryUpdateRequest {
    private UUID parentId;
    @NotBlank(message = "分类名称不能为空")
    private String name;
    private String code;
    private Integer sortOrder;
    private Integer status;

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

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

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
