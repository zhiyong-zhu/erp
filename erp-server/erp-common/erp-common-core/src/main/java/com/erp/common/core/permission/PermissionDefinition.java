package com.erp.common.core.permission;

public interface PermissionDefinition {
    String getCode();
    String getName();
    Integer getType();
    String getPath();
    String getIcon();
    Integer getSortOrder();
    String getParentCode();
    boolean grantToAdminByDefault();
}
