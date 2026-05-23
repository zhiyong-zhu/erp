import { http } from "./http";
import { clearAuth, saveTokens } from "../store/auth";
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
  saveTokens(data.accessToken, data.refreshToken);
  return data;
}

export async function logout(): Promise<void> {
  try {
    await http.post("/auth/logout");
  } finally {
    clearAuth();
  }
}

export async function fetchUserInfo(): Promise<UserInfo> {
  const response = await http.get<ApiResponse<UserInfo>>("/auth/userinfo");
  return response.data.data;
}
