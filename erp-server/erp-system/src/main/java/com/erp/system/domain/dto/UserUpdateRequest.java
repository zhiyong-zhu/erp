package com.erp.system.domain.dto;

import java.util.List;
import java.util.UUID;

public class UserUpdateRequest {
    private String realName;
    private String phone;
    private String email;
    private UUID departmentId;
    private List<UUID> roleIds;

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public UUID getDepartmentId() { return departmentId; }
    public void setDepartmentId(UUID departmentId) { this.departmentId = departmentId; }
    public List<UUID> getRoleIds() { return roleIds; }
    public void setRoleIds(List<UUID> roleIds) { this.roleIds = roleIds; }
}
