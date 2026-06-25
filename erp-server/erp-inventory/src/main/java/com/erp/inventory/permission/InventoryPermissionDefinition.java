package com.erp.inventory.permission;

import com.erp.common.core.permission.PermissionDefinition;

public enum InventoryPermissionDefinition implements PermissionDefinition {
    ROOT(InventoryPermissionCodes.ROOT, "库存管理", 1, "/inventory", "database", 350, null, true),
    WAREHOUSE_LIST(InventoryPermissionCodes.WAREHOUSE_LIST, "仓库管理", 2, "/inventory/warehouses", "database", 351, ROOT.code, true),
    WAREHOUSE_CREATE(InventoryPermissionCodes.WAREHOUSE_CREATE, "新建仓库", 3, null, null, 352, WAREHOUSE_LIST.code, true),
    WAREHOUSE_UPDATE(InventoryPermissionCodes.WAREHOUSE_UPDATE, "编辑仓库", 3, null, null, 353, WAREHOUSE_LIST.code, true),
    LOCATION_LIST(InventoryPermissionCodes.LOCATION_LIST, "库位管理", 2, "/inventory/locations", "apartment", 354, ROOT.code, true),
    LOCATION_CREATE(InventoryPermissionCodes.LOCATION_CREATE, "新建库位", 3, null, null, 355, LOCATION_LIST.code, true),
    LOCATION_UPDATE(InventoryPermissionCodes.LOCATION_UPDATE, "编辑库位", 3, null, null, 356, LOCATION_LIST.code, true),
    RECEIPT_LIST(InventoryPermissionCodes.RECEIPT_LIST, "正式入库单", 2, "/inventory/receipts", "profile", 360, ROOT.code, true),
    ISSUE_LIST(InventoryPermissionCodes.ISSUE_LIST, "出库管理", 2, "/inventory/issues", "profile", 370, ROOT.code, true),
    ISSUE_CREATE(InventoryPermissionCodes.ISSUE_CREATE, "新建出库", 3, null, null, 371, ISSUE_LIST.code, true),
    TRANSFER_LIST(InventoryPermissionCodes.TRANSFER_LIST, "调拨管理", 2, "/inventory/transfers", "swap", 380, ROOT.code, true),
    TRANSFER_CREATE(InventoryPermissionCodes.TRANSFER_CREATE, "新建调拨", 3, null, null, 381, TRANSFER_LIST.code, true),
    CHECK_LIST(InventoryPermissionCodes.CHECK_LIST, "盘点管理", 2, "/inventory/checks", "check-square", 390, ROOT.code, true),
    CHECK_CREATE(InventoryPermissionCodes.CHECK_CREATE, "新建盘点", 3, null, null, 391, CHECK_LIST.code, true),
    CHECK_REVIEW(InventoryPermissionCodes.CHECK_REVIEW, "复核盘点", 3, null, null, 392, CHECK_LIST.code, true),
    CHECK_APPROVE(InventoryPermissionCodes.CHECK_APPROVE, "审批盘点", 3, null, null, 393, CHECK_LIST.code, true),
    CHECK_REJECT(InventoryPermissionCodes.CHECK_REJECT, "驳回盘点", 3, null, null, 394, CHECK_LIST.code, true),
    TRANSACTION_LIST(InventoryPermissionCodes.TRANSACTION_LIST, "库存流水", 2, "/inventory/transactions", "file-search", 400, ROOT.code, true);

    private final String code;
    private final String name;
    private final Integer type;
    private final String path;
    private final String icon;
    private final Integer sortOrder;
    private final String parentCode;
    private final boolean grantToAdminByDefault;

    InventoryPermissionDefinition(String code, String name, Integer type, String path, String icon,
                                  Integer sortOrder, String parentCode, boolean grantToAdminByDefault) {
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
