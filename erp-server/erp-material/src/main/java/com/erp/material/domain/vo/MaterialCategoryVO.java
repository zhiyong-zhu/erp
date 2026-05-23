package com.erp.material.domain.vo;

import com.erp.common.core.domain.BaseVO;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MaterialCategoryVO extends BaseVO {
    private UUID id;
    private UUID parentId;
    private String name;
    private String code;
    private Integer sortOrder;
    private Integer status;
    private List<MaterialCategoryVO> children = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public List<MaterialCategoryVO> getChildren() { return children; }
    public void setChildren(List<MaterialCategoryVO> children) { this.children = children; }
}
