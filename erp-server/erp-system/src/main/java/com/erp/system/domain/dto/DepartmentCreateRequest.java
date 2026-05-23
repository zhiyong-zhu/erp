package com.erp.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public class DepartmentCreateRequest {
    private UUID parentId;
    @NotBlank(message = "部门名称不能为空")
    private String name;
    @NotBlank(message = "部门编码不能为空")
    private String code;
    private String leader;
    private String phone;
    private Integer sortOrder;

    public UUID getParentId() { return parentId; }
    public void setParentId(UUID parentId) { this.parentId = parentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLeader() { return leader; }
    public void setLeader(String leader) { this.leader = leader; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
