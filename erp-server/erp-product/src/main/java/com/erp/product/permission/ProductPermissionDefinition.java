package com.erp.product.permission;

import com.erp.common.core.permission.PermissionDefinition;

public enum ProductPermissionDefinition implements PermissionDefinition {
    PRODUCT_ROOT(ProductPermissionCodes.ROOT, "产品管理", 1, "/product", "appstore", 100, null, true),

    PRODUCT_CATEGORY_LIST(ProductPermissionCodes.CATEGORY_LIST, "产品分类", 2, "/product/categories", "bars", 110, PRODUCT_ROOT.code, true),
    PRODUCT_CATEGORY_CREATE(ProductPermissionCodes.CATEGORY_CREATE, "创建分类", 3, null, null, 111, PRODUCT_CATEGORY_LIST.code, true),
    PRODUCT_CATEGORY_UPDATE(ProductPermissionCodes.CATEGORY_UPDATE, "更新分类", 3, null, null, 112, PRODUCT_CATEGORY_LIST.code, true),

    PRODUCT_LIST(ProductPermissionCodes.PRODUCT_LIST, "产品列表", 2, "/product/products", "shopping", 120, PRODUCT_ROOT.code, true),
    PRODUCT_CREATE(ProductPermissionCodes.PRODUCT_CREATE, "创建产品", 3, null, null, 121, PRODUCT_LIST.code, true),
    PRODUCT_UPDATE(ProductPermissionCodes.PRODUCT_UPDATE, "更新产品", 3, null, null, 122, PRODUCT_LIST.code, true),
    PRODUCT_DETAIL(ProductPermissionCodes.PRODUCT_DETAIL, "查看产品详情", 3, null, null, 123, PRODUCT_LIST.code, true),
    PRODUCT_COST(ProductPermissionCodes.PRODUCT_COST, "查看产品成本字段", 3, null, null, 124, PRODUCT_LIST.code, true),
    PRODUCT_IMPORT(ProductPermissionCodes.PRODUCT_IMPORT, "导入产品", 3, null, null, 125, PRODUCT_LIST.code, true),
    PRODUCT_EXPORT(ProductPermissionCodes.PRODUCT_EXPORT, "导出产品", 3, null, null, 126, PRODUCT_LIST.code, true),
    PACKAGE_LIST(ProductPermissionCodes.PACKAGE_LIST, "包装规格", 2, null, null, 130, PRODUCT_ROOT.code, true),
    PACKAGE_UPDATE(ProductPermissionCodes.PACKAGE_UPDATE, "维护包装规格", 3, null, null, 131, PACKAGE_LIST.code, true),
    LABEL_LIST(ProductPermissionCodes.LABEL_LIST, "标签模板", 2, null, null, 140, PRODUCT_ROOT.code, true),
    LABEL_UPDATE(ProductPermissionCodes.LABEL_UPDATE, "维护标签模板", 3, null, null, 141, LABEL_LIST.code, true),
    LABEL_PRINT(ProductPermissionCodes.LABEL_PRINT, "标签打印", 3, null, null, 142, LABEL_LIST.code, true),
    BOM_LIST(ProductPermissionCodes.BOM_LIST, "BOM", 2, null, null, 150, PRODUCT_ROOT.code, true),
    BOM_UPDATE(ProductPermissionCodes.BOM_UPDATE, "维护BOM", 3, null, null, 151, BOM_LIST.code, true);

    private final String code;
    private final String name;
    private final Integer type;
    private final String path;
    private final String icon;
    private final Integer sortOrder;
    private final String parentCode;
    private final boolean grantToAdminByDefault;

    ProductPermissionDefinition(String code,
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
