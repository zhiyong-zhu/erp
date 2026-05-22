import { http } from "./http";
import type { UserCreatePayload, UserRecord, UserUpdatePayload } from "../types/user";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export async function fetchUsers(): Promise<UserRecord[]> {
  const response = await http.get<ApiResponse<UserRecord[]>>("/system/users");
  return response.data.data;
}

export async function createUser(payload: UserCreatePayload): Promise<UserRecord> {
  const response = await http.post<ApiResponse<UserRecord>>("/system/users", payload);
  return response.data.data;
}

export async function updateUser(id: string, payload: UserUpdatePayload): Promise<UserRecord> {
  const response = await http.put<ApiResponse<UserRecord>>(`/system/users/${id}`, payload);
  return response.data.data;
}

export async function updateUserStatus(id: string, status: number): Promise<void> {
  await http.put(`/system/users/${id}/status`, { status });
}
