package com.erp.system.domain.vo;

import com.erp.common.core.domain.BaseVO;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class UserVO extends BaseVO {
    private UUID id;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private UUID departmentId;
    private String departmentName;
    private Integer status;
    private List<UUID> roleIds;
    private List<String> roleCodes;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public UUID getDepartmentId() { return departmentId; }
    public void setDepartmentId(UUID departmentId) { this.departmentId = departmentId; }
    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public List<UUID> getRoleIds() { return roleIds; }
    public void setRoleIds(List<UUID> roleIds) { this.roleIds = roleIds; }
    public List<String> getRoleCodes() { return roleCodes; }
    public void setRoleCodes(List<String> roleCodes) { this.roleCodes = roleCodes; }
}
