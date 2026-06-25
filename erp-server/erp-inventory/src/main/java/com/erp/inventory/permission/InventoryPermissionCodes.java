package com.erp.inventory.permission;

public final class InventoryPermissionCodes {
    public static final String ROOT = "inventory";
    public static final String WAREHOUSE_LIST = "inventory:warehouse:list";
    public static final String WAREHOUSE_CREATE = "inventory:warehouse:create";
    public static final String WAREHOUSE_UPDATE = "inventory:warehouse:update";
    public static final String LOCATION_LIST = "inventory:location:list";
    public static final String LOCATION_CREATE = "inventory:location:create";
    public static final String LOCATION_UPDATE = "inventory:location:update";
    public static final String RECEIPT_LIST = "inventory:receipt:list";
    public static final String ISSUE_LIST = "inventory:issue:list";
    public static final String ISSUE_CREATE = "inventory:issue:create";
    public static final String TRANSFER_LIST = "inventory:transfer:list";
    public static final String TRANSFER_CREATE = "inventory:transfer:create";
    public static final String CHECK_LIST = "inventory:check:list";
    public static final String CHECK_CREATE = "inventory:check:create";
    public static final String CHECK_REVIEW = "inventory:check:review";
    public static final String CHECK_APPROVE = "inventory:check:approve";
    public static final String CHECK_REJECT = "inventory:check:reject";
    public static final String TRANSACTION_LIST = "inventory:transaction:list";

    private InventoryPermissionCodes() {
    }
}
