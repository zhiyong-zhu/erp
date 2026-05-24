package com.erp.purchase.permission;

import com.erp.common.core.permission.PermissionDefinition;

public enum PurchasePermissionDefinition implements PermissionDefinition {
    ROOT(PurchasePermissionCodes.ROOT, "采购管理", 1, "/purchase", "shopping-cart", 300, null, true),
    ORDER_LIST(PurchasePermissionCodes.ORDER_LIST, "采购单列表", 2, "/purchase/orders", "profile", 310, ROOT.code, true),
    ORDER_CREATE(PurchasePermissionCodes.ORDER_CREATE, "创建采购单", 3, null, null, 311, ORDER_LIST.code, true),
    ORDER_UPDATE(PurchasePermissionCodes.ORDER_UPDATE, "更新采购单", 3, null, null, 312, ORDER_LIST.code, true);

    private final String code;
    private final String name;
    private final Integer type;
    private final String path;
    private final String icon;
    private final Integer sortOrder;
    private final String parentCode;
    private final boolean grantToAdminByDefault;

    PurchasePermissionDefinition(
            String code,
            String name,
            Integer type,
            String path,
            String icon,
            Integer sortOrder,
            String parentCode,
            boolean grantToAdminByDefault
    ) {
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
