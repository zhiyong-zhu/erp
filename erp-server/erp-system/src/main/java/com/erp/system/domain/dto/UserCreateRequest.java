package com.erp.system.domain.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.UUID;

public class UserCreateRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    @NotBlank(message = "姓名不能为空")
    private String realName;
    private String phone;
    private String email;
    private UUID departmentId;
    private List<UUID> roleIds;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
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
