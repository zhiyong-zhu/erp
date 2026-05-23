import { request } from "./http";
import { saveAccessToken, saveRefreshToken, saveTokens } from "../store/auth";
import type { LoginRequest, LoginResponse, UserInfo } from "../types/auth";

export async function login(payload: LoginRequest): Promise<LoginResponse> {
  const data = await request<LoginResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify(payload)
  });
  saveTokens(data.accessToken, data.refreshToken);
  return data;
}

export async function logout(): Promise<void> {
  try {
    await request<void>("/auth/logout", { method: "POST" });
  } finally {
    saveAccessToken(null);
    saveRefreshToken(null);
  }
}

export async function fetchUserInfo(): Promise<UserInfo> {
  return request<UserInfo>("/auth/userinfo");
}
