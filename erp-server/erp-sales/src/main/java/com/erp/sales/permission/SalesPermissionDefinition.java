package com.erp.sales.permission;

import com.erp.common.core.permission.PermissionDefinition;

public enum SalesPermissionDefinition implements PermissionDefinition {
    ROOT(SalesPermissionCodes.ROOT, "销售管理", 1, "/sales", "shopping", 400, null, true),
    CUSTOMER_LIST(SalesPermissionCodes.CUSTOMER_LIST, "客户列表", 2, "/sales/customers", "team", 410, ROOT.code, true),
    CUSTOMER_CREATE(SalesPermissionCodes.CUSTOMER_CREATE, "创建客户", 3, null, null, 411, CUSTOMER_LIST.code, true),
    CUSTOMER_UPDATE(SalesPermissionCodes.CUSTOMER_UPDATE, "编辑客户", 3, null, null, 412, CUSTOMER_LIST.code, true),
    ORDER_LIST(SalesPermissionCodes.ORDER_LIST, "销售订单列表", 2, "/sales/orders", "profile", 420, ROOT.code, true),
    ORDER_CREATE(SalesPermissionCodes.ORDER_CREATE, "创建销售订单", 3, null, null, 421, ORDER_LIST.code, true),
    ORDER_UPDATE(SalesPermissionCodes.ORDER_UPDATE, "编辑销售订单", 3, null, null, 422, ORDER_LIST.code, true),
    RETURN_LIST(SalesPermissionCodes.RETURN_LIST, "退货列表", 2, "/sales/returns", "rollback", 430, ROOT.code, true),
    RETURN_CREATE(SalesPermissionCodes.RETURN_CREATE, "创建退货", 3, null, null, 431, RETURN_LIST.code, true),
    RETURN_UPDATE(SalesPermissionCodes.RETURN_UPDATE, "处理退货", 3, null, null, 432, RETURN_LIST.code, true),
    SHIPPING_LIST(SalesPermissionCodes.SHIPPING_LIST, "发货列表", 2, "/sales/shipping", "car", 440, ROOT.code, true),
    SHIPPING_UPDATE(SalesPermissionCodes.SHIPPING_UPDATE, "编辑发货", 3, null, null, 441, SHIPPING_LIST.code, true),
    RECEIVABLE_LIST(SalesPermissionCodes.RECEIVABLE_LIST, "应收统计", 2, "/sales/receivables", "wallet", 450, ROOT.code, true),
    REPORT_LIST(SalesPermissionCodes.REPORT_LIST, "销售报表", 2, "/sales/reports", "bar-chart", 460, ROOT.code, true);

    private final String code;
    private final String name;
    private final Integer type;
    private final String path;
    private final String icon;
    private final Integer sortOrder;
    private final String parentCode;
    private final boolean grantToAdminByDefault;

    SalesPermissionDefinition(
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
