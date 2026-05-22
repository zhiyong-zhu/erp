import { request, setAccessToken } from "./http";
import type { LoginRequest, LoginResponse, UserInfo } from "../types/auth";

export async function login(payload: LoginRequest): Promise<LoginResponse> {
  const data = await request<LoginResponse>("/auth/login", {
    method: "POST",
    body: JSON.stringify(payload)
  });
  setAccessToken(data.accessToken);
  return data;
}

export async function logout(): Promise<void> {
  try {
    await request<void>("/auth/logout", { method: "POST" });
  } finally {
    setAccessToken(null);
  }
}

export async function fetchUserInfo(): Promise<UserInfo> {
  return request<UserInfo>("/auth/userinfo");
}
