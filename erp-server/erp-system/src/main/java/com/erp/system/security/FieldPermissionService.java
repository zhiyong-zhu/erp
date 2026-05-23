package com.erp.system.security;

public interface FieldPermissionService {
    boolean canViewUserSensitiveFields();
    boolean canEditUserSensitiveFields();
    String currentPermissionSnapshot();
}
