import axios from "axios";

const ACCESS_TOKEN_KEY = "erp.accessToken";
const REFRESH_TOKEN_KEY = "erp.refreshToken";

interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export const http = axios.create({
  baseURL: import.meta.env.PROD ? "/api/v1" : "http://localhost:8080/api/v1",
  timeout: 15000
});

http.interceptors.request.use((config) => {
  const token = localStorage.getItem(ACCESS_TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  const refreshToken = getRefreshToken();
  if (refreshToken) {
    config.headers["X-Refresh-Token"] = refreshToken;
  }
  return config;
});

http.interceptors.response.use((response) => {
  const payload = response.data as ApiResponse | undefined;
  if (payload && typeof payload.code === "number" && payload.code !== 200) {
    const error = new Error(payload.message || "请求失败");
    (error as Error & { response?: typeof response }).response = response;
    throw error;
  }
  return response;
});

export function setAccessToken(token: string | null) {
  if (token) {
    localStorage.setItem(ACCESS_TOKEN_KEY, token);
  } else {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
  }
}

export function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY);
}

export function setRefreshToken(token: string | null) {
  if (token) {
    localStorage.setItem(REFRESH_TOKEN_KEY, token);
  } else {
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  }
}

export function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}
