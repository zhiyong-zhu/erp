package com.erp.system.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.util.UUID;

@TableName("sys_role_permission")
public class SysRolePermission {
    private UUID roleId;
    private UUID permissionId;

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

    public UUID getPermissionId() {
        return permissionId;
    }

    public void setPermissionId(UUID permissionId) {
        this.permissionId = permissionId;
    }
}
