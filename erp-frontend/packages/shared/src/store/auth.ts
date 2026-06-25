import type { UserInfo } from "../types/auth";

export interface AuthStoreConfig {
  storagePrefix: string;
}

export function createAuthStore(config: AuthStoreConfig) {
  const prefix = config.storagePrefix;

  function getAccessToken(): string | null {
    return localStorage.getItem(`${prefix}.accessToken`);
  }

  function setAccessToken(token: string | null) {
    if (token) {
      localStorage.setItem(`${prefix}.accessToken`, token);
    } else {
      localStorage.removeItem(`${prefix}.accessToken`);
    }
  }

  function getRefreshToken(): string | null {
    return localStorage.getItem(`${prefix}.refreshToken`);
  }

  function setRefreshToken(token: string | null) {
    if (token) {
      localStorage.setItem(`${prefix}.refreshToken`, token);
    } else {
      localStorage.removeItem(`${prefix}.refreshToken`);
    }
  }

  function getUser(): UserInfo | null {
    const raw = localStorage.getItem(`${prefix}.user`);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as UserInfo;
    } catch {
      return null;
    }
  }

  function setUser(user: UserInfo | null) {
    if (user) {
      localStorage.setItem(`${prefix}.user`, JSON.stringify(user));
    } else {
      localStorage.removeItem(`${prefix}.user`);
    }
  }

  function clearAll() {
    setAccessToken(null);
    setRefreshToken(null);
    setUser(null);
  }

  function hasPermission(permission: string): boolean {
    const user = getUser();
    if (!user) return false;
    return user.permissions.includes(permission);
  }

  return {
    getAccessToken,
    setAccessToken,
    getRefreshToken,
    setRefreshToken,
    getUser,
    setUser,
    clearAll,
    hasPermission
  };
}
