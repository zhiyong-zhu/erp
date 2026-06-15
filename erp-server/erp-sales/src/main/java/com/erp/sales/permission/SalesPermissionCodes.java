package com.erp.sales.permission;

public final class SalesPermissionCodes {
    public static final String ROOT = "sales";
    public static final String CUSTOMER_LIST = "sales:customer:list";
    public static final String CUSTOMER_CREATE = "sales:customer:create";
    public static final String CUSTOMER_UPDATE = "sales:customer:update";
    public static final String ORDER_LIST = "sales:order:list";
    public static final String ORDER_CREATE = "sales:order:create";
    public static final String ORDER_UPDATE = "sales:order:update";
    public static final String RETURN_LIST = "sales:return:list";
    public static final String RETURN_CREATE = "sales:return:create";
    public static final String RETURN_UPDATE = "sales:return:update";
    public static final String SHIPPING_LIST = "sales:shipping:list";
    public static final String SHIPPING_UPDATE = "sales:shipping:update";
    public static final String RECEIVABLE_LIST = "sales:receivable:list";
    public static final String REPORT_LIST = "sales:report:list";

    private SalesPermissionCodes() {
    }
}
