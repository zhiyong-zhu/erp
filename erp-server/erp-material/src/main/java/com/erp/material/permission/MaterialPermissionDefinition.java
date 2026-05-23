package com.erp.material.permission;

import com.erp.common.core.permission.PermissionDefinition;

public enum MaterialPermissionDefinition implements PermissionDefinition {
    MATERIAL_ROOT(MaterialPermissionCodes.ROOT, "原料管理", 1, "/material", "database", 200, null, true),

    CATEGORY_LIST(MaterialPermissionCodes.CATEGORY_LIST, "原料分类", 2, "/material/categories", "bars", 210, MATERIAL_ROOT.code, true),
    CATEGORY_CREATE(MaterialPermissionCodes.CATEGORY_CREATE, "创建原料分类", 3, null, null, 211, CATEGORY_LIST.code, true),
    CATEGORY_UPDATE(MaterialPermissionCodes.CATEGORY_UPDATE, "更新原料分类", 3, null, null, 212, CATEGORY_LIST.code, true),

    MATERIAL_LIST(MaterialPermissionCodes.MATERIAL_LIST, "原料列表", 2, "/material/materials", "appstore", 220, MATERIAL_ROOT.code, true),
    MATERIAL_CREATE(MaterialPermissionCodes.MATERIAL_CREATE, "创建原料", 3, null, null, 221, MATERIAL_LIST.code, true),
    MATERIAL_UPDATE(MaterialPermissionCodes.MATERIAL_UPDATE, "更新原料", 3, null, null, 222, MATERIAL_LIST.code, true),

    SUPPLIER_LIST(MaterialPermissionCodes.SUPPLIER_LIST, "供应商管理", 2, "/material/suppliers", "team", 230, MATERIAL_ROOT.code, true),
    SUPPLIER_CREATE(MaterialPermissionCodes.SUPPLIER_CREATE, "创建供应商", 3, null, null, 231, SUPPLIER_LIST.code, true),
    SUPPLIER_UPDATE(MaterialPermissionCodes.SUPPLIER_UPDATE, "更新供应商", 3, null, null, 232, SUPPLIER_LIST.code, true);

    private final String code;
    private final String name;
    private final Integer type;
    private final String path;
    private final String icon;
    private final Integer sortOrder;
    private final String parentCode;
    private final boolean grantToAdminByDefault;

    MaterialPermissionDefinition(String code, String name, Integer type, String path, String icon, Integer sortOrder, String parentCode, boolean grantToAdminByDefault) {
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
    public String getCode() { return code; }
    @Override
    public String getName() { return name; }
    @Override
    public Integer getType() { return type; }
    @Override
    public String getPath() { return path; }
    @Override
    public String getIcon() { return icon; }
    @Override
    public Integer getSortOrder() { return sortOrder; }
    @Override
    public String getParentCode() { return parentCode; }
    @Override
    public boolean grantToAdminByDefault() { return grantToAdminByDefault; }
}
