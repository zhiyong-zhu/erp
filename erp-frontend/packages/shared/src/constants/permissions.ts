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
