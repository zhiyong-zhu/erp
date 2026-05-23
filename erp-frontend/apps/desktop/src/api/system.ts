import { request } from "./http";
import type { PageResult } from "../types/page";
import type { DepartmentPayload, DepartmentRecord, RolePayload, RoleRecord } from "../types/system";

export async function fetchDepartments(): Promise<DepartmentRecord[]> {
  return request<DepartmentRecord[]>("/system/departments");
}

export async function createDepartment(payload: DepartmentPayload): Promise<DepartmentRecord> {
  return request<DepartmentRecord>("/system/departments", { method: "POST", body: JSON.stringify(payload) });
}

export async function fetchRoles(): Promise<RoleRecord[]> {
  const data = await request<PageResult<RoleRecord>>("/system/roles?pageNum=1&pageSize=100");
  return data.records;
}

export async function createRole(payload: RolePayload): Promise<RoleRecord> {
  return request<RoleRecord>("/system/roles", { method: "POST", body: JSON.stringify(payload) });
}
