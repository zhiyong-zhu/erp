package com.erp.production.permission;

import com.erp.common.core.permission.PermissionDefinition;

public enum ProductionPermissionDefinition implements PermissionDefinition {
    ROOT(ProductionPermissionCodes.ROOT, "生产管理", 1, "/production", "build", 500, null, true),
    PROCESS_LIST(ProductionPermissionCodes.PROCESS_LIST, "工艺路线", 2, "/production/processes", "branches", 510, ROOT.code, true),
    PROCESS_CREATE(ProductionPermissionCodes.PROCESS_CREATE, "新建工艺路线", 3, null, null, 511, PROCESS_LIST.code, true),
    PROCESS_UPDATE(ProductionPermissionCodes.PROCESS_UPDATE, "编辑工艺路线", 3, null, null, 512, PROCESS_LIST.code, true),
    BOM_LIST(ProductionPermissionCodes.BOM_LIST, "生产BOM", 2, "/product/products", "profile", 520, ROOT.code, true),
    BOM_CREATE(ProductionPermissionCodes.BOM_CREATE, "新建生产BOM", 3, null, null, 521, BOM_LIST.code, true),
    BOM_UPDATE(ProductionPermissionCodes.BOM_UPDATE, "编辑生产BOM", 3, null, null, 522, BOM_LIST.code, true),
    BATCH_LIST(ProductionPermissionCodes.BATCH_LIST, "生产工单", 2, "/production/batches", "schedule", 530, ROOT.code, true),
    BATCH_CREATE(ProductionPermissionCodes.BATCH_CREATE, "新建生产工单", 3, null, null, 531, BATCH_LIST.code, true),
    BATCH_UPDATE(ProductionPermissionCodes.BATCH_UPDATE, "编辑生产工单", 3, null, null, 532, BATCH_LIST.code, true),
    REPORT_LIST(ProductionPermissionCodes.REPORT_LIST, "生产执行", 2, "/production/reports", "tool", 540, ROOT.code, true),
    REPORT_CREATE(ProductionPermissionCodes.REPORT_CREATE, "执行生产流转", 3, null, null, 541, REPORT_LIST.code, true),
    SERIAL_LIST(ProductionPermissionCodes.SERIAL_LIST, "序列号追溯", 2, "/production/serial-numbers", "barcode", 550, ROOT.code, true),
    SERIAL_CREATE(ProductionPermissionCodes.SERIAL_CREATE, "新建序列号", 3, null, null, 551, SERIAL_LIST.code, true),
    SERIAL_UPDATE(ProductionPermissionCodes.SERIAL_UPDATE, "编辑序列号", 3, null, null, 552, SERIAL_LIST.code, true);

    private final String code;
    private final String name;
    private final Integer type;
    private final String path;
    private final String icon;
    private final Integer sortOrder;
    private final String parentCode;
    private final boolean grantToAdminByDefault;

    ProductionPermissionDefinition(String code, String name, Integer type, String path, String icon,
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
