export interface DepartmentRecord {
  id: string;
  parentId?: string | null;
  name: string;
  code: string;
  leader?: string | null;
  phone?: string | null;
  sortOrder: number;
  status: number;
  createdAt?: string;
  children?: DepartmentRecord[];
}

export interface DepartmentPayload {
  parentId?: string | null;
  name: string;
  code: string;
  leader?: string;
  phone?: string;
  sortOrder?: number;
}

export interface RoleRecord {
  id: string;
  name: string;
  code: string;
  description?: string | null;
  dataScope: number;
  status: number;
  createdAt?: string;
  permissionIds?: string[];
}

export interface RolePayload {
  name: string;
  code: string;
  description?: string;
  dataScope?: number;
  permissionIds?: string[];
}

export interface PermissionRecord {
  id: string;
  parentId?: string | null;
  name: string;
  code: string;
  type: number;
  path?: string | null;
  icon?: string | null;
  sortOrder?: number | null;
  status: number;
  children?: PermissionRecord[];
}

export interface DictTypeRecord {
  id: string;
  name: string;
  code: string;
  description?: string | null;
  status: number;
  createdAt?: string;
}

export interface DictTypePayload {
  name: string;
  code: string;
  description?: string;
  status?: number;
}

export interface DictDataRecord {
  id: string;
  dictTypeCode: string;
  label: string;
  value: string;
  sortOrder: number;
  cssClass?: string | null;
  status: number;
  createdAt?: string;
}

export interface DictDataPayload {
  label: string;
  value: string;
  sortOrder?: number;
  cssClass?: string;
  status?: number;
}

/** 值类型：BOOL / STRING / INT */
export type ParamValueType = "BOOL" | "STRING" | "INT";

export interface SysParamRecord {
  id: string;
  code: string;
  name: string;
  value: string;
  valueType: ParamValueType;
  description?: string | null;
  status: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface SysParamUpdatePayload {
  value: string;
  description?: string;
  status?: number;
}

export interface OperationLogRecord {

  id: number;
  userId?: string | null;
  username?: string | null;
  module: string;
  action: string;
  description?: string | null;
  method?: string | null;
  requestUrl?: string | null;
  requestParams?: string | null;
  responseCode?: number | null;
  ip?: string | null;
  duration?: number | null;
  traceId?: string | null;
  success?: boolean | null;
  errorMessage?: string | null;
  dataScopeLevel?: string | null;
  dataScopeSnapshot?: string | null;
  fieldPermissionSnapshot?: string | null;
  auditTags?: string | null;
  createdAt?: string;
}
