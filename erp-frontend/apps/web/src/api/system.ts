import { http } from "./http";
import type { PageQuery, PageResult } from "../types/page";
import type { DepartmentPayload, DepartmentRecord, RolePayload, RoleRecord } from "../types/system";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export async function fetchDepartments(): Promise<DepartmentRecord[]> {
  const response = await http.get<ApiResponse<DepartmentRecord[]>>("/system/departments");
  return response.data.data;
}

export async function createDepartment(payload: DepartmentPayload): Promise<DepartmentRecord> {
  const response = await http.post<ApiResponse<DepartmentRecord>>("/system/departments", payload);
  return response.data.data;
}

export async function updateDepartment(id: string, payload: DepartmentPayload): Promise<DepartmentRecord> {
  const response = await http.put<ApiResponse<DepartmentRecord>>(`/system/departments/${id}`, payload);
  return response.data.data;
}

export async function updateDepartmentStatus(id: string, status: number): Promise<void> {
  await http.put(`/system/departments/${id}/status`, { status });
}

export async function fetchRoles(query: PageQuery = { pageNum: 1, pageSize: 100 }): Promise<PageResult<RoleRecord>> {
  const response = await http.get<ApiResponse<PageResult<RoleRecord>>>("/system/roles", { params: query });
  return response.data.data;
}

export async function createRole(payload: RolePayload): Promise<RoleRecord> {
  const response = await http.post<ApiResponse<RoleRecord>>("/system/roles", payload);
  return response.data.data;
}

export async function updateRole(id: string, payload: RolePayload): Promise<RoleRecord> {
  const response = await http.put<ApiResponse<RoleRecord>>(`/system/roles/${id}`, payload);
  return response.data.data;
}

export async function updateRoleStatus(id: string, status: number): Promise<void> {
  await http.put(`/system/roles/${id}/status`, { status });
}
