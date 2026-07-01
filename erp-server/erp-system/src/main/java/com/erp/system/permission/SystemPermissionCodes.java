package com.erp.system.permission;

public final class SystemPermissionCodes {
    public static final String SYSTEM = "system";

    public static final String USER_LIST = "system:user:list";
    public static final String USER_CREATE = "system:user:create";
    public static final String USER_UPDATE = "system:user:update";
    public static final String USER_SENSITIVE_VIEW = "system:user:sensitive:view";
    public static final String USER_SENSITIVE_EDIT = "system:user:sensitive:edit";

    public static final String DEPT_LIST = "system:dept:list";
    public static final String DEPT_CREATE = "system:dept:create";
    public static final String DEPT_UPDATE = "system:dept:update";

    public static final String ROLE_LIST = "system:role:list";
    public static final String ROLE_CREATE = "system:role:create";
    public static final String ROLE_UPDATE = "system:role:update";

    public static final String DICT_LIST = "system:dict:list";
    public static final String DICT_CREATE = "system:dict:create";
    public static final String DICT_UPDATE = "system:dict:update";

    public static final String PARAM_LIST = "system:param:list";
    public static final String PARAM_UPDATE = "system:param:update";

    public static final String LOG_LIST = "system:log:list";

    private SystemPermissionCodes() {
    }
}
