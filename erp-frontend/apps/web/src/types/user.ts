import type { BasePayload, BaseRecord } from "@erp/shared";

export interface UserRecord extends BaseRecord {
  id: string;
  username: string;
  realName: string;
  phone?: string | null;
  email?: string | null;
  departmentId?: string | null;
  departmentName?: string | null;
  status: number;
  roleIds?: string[];
  roleCodes: string[];
}

export interface UserCreatePayload extends BasePayload {
  username: string;
  password: string;
  realName: string;
  phone?: string;
  email?: string;
  departmentId?: string | null;
  roleIds: string[];
}

export interface UserUpdatePayload extends BasePayload {
  realName?: string;
  phone?: string;
  email?: string;
  departmentId?: string | null;
  roleIds: string[];
}

export interface DataScopeInfo {
  level: "ALL" | "DEPARTMENT" | "SELF";
  userId?: string | null;
  departmentIds: string[];
}

export interface UserFieldPermissionInfo {
  canViewSensitiveFields: boolean;
  canEditSensitiveFields: boolean;
}
