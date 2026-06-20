export const SYSTEM_PERMISSIONS = {
  ROOT: "system",

  USER_LIST: "system:user:list",
  USER_CREATE: "system:user:create",
  USER_UPDATE: "system:user:update",
  USER_SENSITIVE_VIEW: "system:user:sensitive:view",
  USER_SENSITIVE_EDIT: "system:user:sensitive:edit",

  DEPT_LIST: "system:dept:list",
  DEPT_CREATE: "system:dept:create",
  DEPT_UPDATE: "system:dept:update",

  ROLE_LIST: "system:role:list",
  ROLE_CREATE: "system:role:create",
  ROLE_UPDATE: "system:role:update",

  DICT_LIST: "system:dict:list",
  DICT_CREATE: "system:dict:create",
  DICT_UPDATE: "system:dict:update",

  LOG_LIST: "system:log:list"
} as const;

export type SystemPermissionCode = (typeof SYSTEM_PERMISSIONS)[keyof typeof SYSTEM_PERMISSIONS];

export const PRODUCT_PERMISSIONS = {
  ROOT: "product",

  CATEGORY_LIST: "product:category:list",
  CATEGORY_CREATE: "product:category:create",
  CATEGORY_UPDATE: "product:category:update",

  PRODUCT_LIST: "product:list",
  PRODUCT_CREATE: "product:create",
  PRODUCT_UPDATE: "product:update",
  PRODUCT_DETAIL: "product:detail",
  PRODUCT_COST: "product:cost",
  PRODUCT_IMPORT: "product:import",
  PRODUCT_EXPORT: "product:export",
  PACKAGE_LIST: "product:package:list",
  PACKAGE_UPDATE: "product:package:update",
  LABEL_LIST: "product:label:list",
  LABEL_UPDATE: "product:label:update",
  LABEL_PRINT: "product:label:print",
  BOM_LIST: "product:bom:list",
  BOM_UPDATE: "product:bom:update"
} as const;

export type ProductPermissionCode = (typeof PRODUCT_PERMISSIONS)[keyof typeof PRODUCT_PERMISSIONS];

export const MATERIAL_PERMISSIONS = {
  ROOT: "material",
  CATEGORY_LIST: "material:category:list",
  CATEGORY_CREATE: "material:category:create",
  CATEGORY_UPDATE: "material:category:update",
  MATERIAL_LIST: "material:list",
  MATERIAL_CREATE: "material:create",
  MATERIAL_UPDATE: "material:update",
  MATERIAL_IMPORT: "material:import",
  MATERIAL_EXPORT: "material:export",
  ALERT_LIST: "material:alert:list",
  REPLENISH_LIST: "material:replenish:list",
  SUPPLIER_LIST: "material:supplier:list",
  SUPPLIER_CREATE: "material:supplier:create",
  SUPPLIER_UPDATE: "material:supplier:update",
  QUOTE_LIST: "material:quote:list",
  QUOTE_CREATE: "material:quote:create",
  QUOTE_UPDATE: "material:quote:update"
} as const;

export type MaterialPermissionCode = (typeof MATERIAL_PERMISSIONS)[keyof typeof MATERIAL_PERMISSIONS];

export const PURCHASE_PERMISSIONS = {
  ROOT: "purchase",
  ORDER_LIST: "purchase:order:list",
  ORDER_CREATE: "purchase:order:create",
  ORDER_UPDATE: "purchase:order:update",
  PAYABLE_LIST: "purchase:payable:list",
  EXCEPTION_LIST: "purchase:exception:list",
  EXCEPTION_UPDATE: "purchase:exception:update"
} as const;

export type PurchasePermissionCode = (typeof PURCHASE_PERMISSIONS)[keyof typeof PURCHASE_PERMISSIONS];

export const SALES_PERMISSIONS = {
  ROOT: "sales",
  CUSTOMER_LIST: "sales:customer:list",
  CUSTOMER_CREATE: "sales:customer:create",
  CUSTOMER_UPDATE: "sales:customer:update",
  ORDER_LIST: "sales:order:list",
  ORDER_CREATE: "sales:order:create",
  ORDER_UPDATE: "sales:order:update",
  RETURN_LIST: "sales:return:list",
  RETURN_CREATE: "sales:return:create",
  RETURN_UPDATE: "sales:return:update",
  SHIPPING_LIST: "sales:shipping:list",
  SHIPPING_UPDATE: "sales:shipping:update",
  RECEIVABLE_LIST: "sales:receivable:list",
  EXCEPTION_LIST: "sales:exception:list",
  EXCEPTION_UPDATE: "sales:exception:update",
  REPORT_LIST: "sales:report:list"
} as const;

export type SalesPermissionCode = (typeof SALES_PERMISSIONS)[keyof typeof SALES_PERMISSIONS];

export const PRODUCTION_PERMISSIONS = {
  ROOT: "production",
  PROCESS_LIST: "production:process:list",
  PROCESS_CREATE: "production:process:create",
  PROCESS_UPDATE: "production:process:update",
  BOM_LIST: "production:bom:list",
  BOM_CREATE: "production:bom:create",
  BOM_UPDATE: "production:bom:update",
  BATCH_LIST: "production:batch:list",
  BATCH_CREATE: "production:batch:create",
  BATCH_UPDATE: "production:batch:update",
  REPORT_LIST: "production:report:list",
  REPORT_CREATE: "production:report:create",
  SERIAL_LIST: "production:serial:list",
  SERIAL_CREATE: "production:serial:create",
  SERIAL_UPDATE: "production:serial:update"
} as const;

export type ProductionPermissionCode = (typeof PRODUCTION_PERMISSIONS)[keyof typeof PRODUCTION_PERMISSIONS];
