package com.erp.system.domain.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class RolePermissionUpdateRequest {
    @NotNull(message = "权限ID列表不能为空")
    private List<UUID> permissionIds;

    public List<UUID> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<UUID> permissionIds) {
        this.permissionIds = permissionIds;
    }
}
