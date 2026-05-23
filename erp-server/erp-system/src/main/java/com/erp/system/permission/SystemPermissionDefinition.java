package com.erp.system.permission;

import com.erp.common.core.permission.PermissionDefinition;

public enum SystemPermissionDefinition implements PermissionDefinition {
    SYSTEM_ROOT(SystemPermissionCodes.SYSTEM, "系统管理", 1, "/system", "setting", 0, null, true),

    USER_LIST(SystemPermissionCodes.USER_LIST, "用户管理", 2, "/system/users", "user", 10, SYSTEM_ROOT.code, true),
    USER_CREATE(SystemPermissionCodes.USER_CREATE, "创建用户", 3, null, null, 11, USER_LIST.code, true),
    USER_UPDATE(SystemPermissionCodes.USER_UPDATE, "更新用户", 3, null, null, 12, USER_LIST.code, true),
    USER_SENSITIVE_VIEW(SystemPermissionCodes.USER_SENSITIVE_VIEW, "查看用户敏感字段", 3, null, null, 13, USER_LIST.code, true),
    USER_SENSITIVE_EDIT(SystemPermissionCodes.USER_SENSITIVE_EDIT, "编辑用户敏感字段", 3, null, null, 14, USER_LIST.code, true),

    DEPT_LIST(SystemPermissionCodes.DEPT_LIST, "部门管理", 2, "/system/departments", "apartment", 20, SYSTEM_ROOT.code, true),
    DEPT_CREATE(SystemPermissionCodes.DEPT_CREATE, "创建部门", 3, null, null, 21, DEPT_LIST.code, true),
    DEPT_UPDATE(SystemPermissionCodes.DEPT_UPDATE, "更新部门", 3, null, null, 22, DEPT_LIST.code, true),

    ROLE_LIST(SystemPermissionCodes.ROLE_LIST, "角色管理", 2, "/system/roles", "team", 30, SYSTEM_ROOT.code, true),
    ROLE_CREATE(SystemPermissionCodes.ROLE_CREATE, "创建角色", 3, null, null, 31, ROLE_LIST.code, true),
    ROLE_UPDATE(SystemPermissionCodes.ROLE_UPDATE, "更新角色", 3, null, null, 32, ROLE_LIST.code, true),

    DICT_LIST(SystemPermissionCodes.DICT_LIST, "数据字典", 2, "/system/dict", "profile", 40, SYSTEM_ROOT.code, true),
    DICT_CREATE(SystemPermissionCodes.DICT_CREATE, "创建字典", 3, null, null, 41, DICT_LIST.code, true),
    DICT_UPDATE(SystemPermissionCodes.DICT_UPDATE, "更新字典", 3, null, null, 42, DICT_LIST.code, true),

    LOG_LIST(SystemPermissionCodes.LOG_LIST, "操作日志", 2, "/system/logs", "file-search", 50, SYSTEM_ROOT.code, true);

    private final String code;
    private final String name;
    private final Integer type;
    private final String path;
    private final String icon;
    private final Integer sortOrder;
    private final String parentCode;
    private final boolean grantToAdminByDefault;

    SystemPermissionDefinition(String code,
                               String name,
                               Integer type,
                               String path,
                               String icon,
                               Integer sortOrder,
                               String parentCode,
                               boolean grantToAdminByDefault) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.path = path;
        this.icon = icon;
        this.sortOrder = sortOrder;
        this.parentCode = parentCode;
        this.grantToAdminByDefault = grantToAdminByDefault;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Integer getType() {
        return type;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public Integer getSortOrder() {
        return sortOrder;
    }

    @Override
    public String getParentCode() {
        return parentCode;
    }

    @Override
    public boolean grantToAdminByDefault() {
        return grantToAdminByDefault;
    }
}
