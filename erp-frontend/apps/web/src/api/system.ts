import { http } from "./http";
import type { PageQuery, PageResult } from "../types/page";
import type {
  DepartmentPayload,
  DepartmentRecord,
  DictDataPayload,
  DictDataRecord,
  DictTypePayload,
  DictTypeRecord,
  OperationLogRecord,
  PermissionRecord,
  RolePayload,
  RoleRecord
} from "../types/system";

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

export async function fetchRoleDetail(id: string): Promise<RoleRecord> {
  const response = await http.get<ApiResponse<RoleRecord>>(`/system/roles/${id}`);
  return response.data.data;
}

export async function updateRoleStatus(id: string, status: number): Promise<void> {
  await http.put(`/system/roles/${id}/status`, { status });
}

export async function fetchPermissionTree(): Promise<PermissionRecord[]> {
  const response = await http.get<ApiResponse<PermissionRecord[]>>("/system/roles/permissions/tree");
  return response.data.data;
}

export async function updateRolePermissions(id: string, permissionIds: string[]): Promise<void> {
  await http.put(`/system/roles/${id}/permissions`, { permissionIds });
}

export async function fetchDictTypes(query: PageQuery = { pageNum: 1, pageSize: 10 }): Promise<PageResult<DictTypeRecord>> {
  const response = await http.get<ApiResponse<PageResult<DictTypeRecord>>>("/system/dicts/types", { params: query });
  return response.data.data;
}

export async function createDictType(payload: DictTypePayload): Promise<DictTypeRecord> {
  const response = await http.post<ApiResponse<DictTypeRecord>>("/system/dicts/types", payload);
  return response.data.data;
}

export async function updateDictType(id: string, payload: DictTypePayload): Promise<DictTypeRecord> {
  const response = await http.put<ApiResponse<DictTypeRecord>>(`/system/dicts/types/${id}`, payload);
  return response.data.data;
}

export async function fetchDictData(code: string): Promise<DictDataRecord[]> {
  const response = await http.get<ApiResponse<DictDataRecord[]>>(`/system/dicts/${code}`);
  return response.data.data;
}

export async function createDictData(code: string, payload: DictDataPayload): Promise<DictDataRecord> {
  const response = await http.post<ApiResponse<DictDataRecord>>(`/system/dicts/${code}/items`, payload);
  return response.data.data;
}

export async function updateDictData(id: string, payload: DictDataPayload): Promise<DictDataRecord> {
  const response = await http.put<ApiResponse<DictDataRecord>>(`/system/dicts/items/${id}`, payload);
  return response.data.data;
}

export async function fetchOperationLogs(query: PageQuery & { module?: string; action?: string; username?: string }): Promise<PageResult<OperationLogRecord>> {
  const response = await http.get<ApiResponse<PageResult<OperationLogRecord>>>("/system/logs/operations", { params: query });
  return response.data.data;
}
