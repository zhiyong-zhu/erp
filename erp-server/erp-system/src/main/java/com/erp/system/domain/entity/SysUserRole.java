package com.erp.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.util.UUID;

@TableName("sys_user_role")
public class SysUserRole {
    private UUID userId;
    private UUID roleId;

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getRoleId() { return roleId; }
    public void setRoleId(UUID roleId) { this.roleId = roleId; }
}
