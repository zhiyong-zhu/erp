export interface ApiResponse<T = unknown> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export interface HttpClientConfig {
  baseUrl: string;
  getAccessToken: () => string | null;
  getRefreshToken: () => string | null;
  onUnauthorized?: () => void;
}

export function createHttpClient(config: HttpClientConfig) {
  async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
    const headers = new Headers(options.headers);
    headers.set("Content-Type", "application/json");

    const token = config.getAccessToken();
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
    const refreshToken = config.getRefreshToken();
    if (refreshToken) {
      headers.set("X-Refresh-Token", refreshToken);
    }

    const response = await fetch(`${config.baseUrl}${path}`, {
      ...options,
      headers
    });

    if (response.status === 401) {
      config.onUnauthorized?.();
      throw new Error("登录已过期，请重新登录");
    }

    const payload = (await response.json()) as ApiResponse<T>;
    if (!response.ok || payload.code !== 200) {
      throw new Error(payload.message || "请求失败");
    }

    return payload.data;
  }

  function withQuery(path: string, query: Record<string, string | number | undefined | null>) {
    const params = new URLSearchParams();
    Object.entries(query).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== "") {
        params.set(key, String(value));
      }
    });
    const queryString = params.toString();
    return queryString ? `${path}?${queryString}` : path;
  }

  return { request, withQuery };
}

export type HttpClient = ReturnType<typeof createHttpClient>;
