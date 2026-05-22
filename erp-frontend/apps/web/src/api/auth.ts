import { http, setAccessToken } from "./http";
import type { LoginRequest, LoginResponse, UserInfo } from "../types/auth";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export async function login(payload: LoginRequest): Promise<LoginResponse> {
  const response = await http.post<ApiResponse<LoginResponse>>("/auth/login", payload);
  const data = response.data.data;
  setAccessToken(data.accessToken);
  return data;
}

export async function logout(): Promise<void> {
  await http.post("/auth/logout");
  setAccessToken(null);
}

export async function fetchUserInfo(): Promise<UserInfo> {
  const response = await http.get<ApiResponse<UserInfo>>("/auth/userinfo");
  return response.data.data;
}
