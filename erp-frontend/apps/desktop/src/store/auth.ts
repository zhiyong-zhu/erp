import { getAccessToken, getRefreshToken, setAccessToken, setRefreshToken } from "../api/http";
import type { UserInfo } from "../types/auth";

export interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: UserInfo | null;
}

export const authStore: AuthState = {
  accessToken: getAccessToken(),
  refreshToken: getRefreshToken(),
  user: null
};

export function saveAccessToken(token: string | null) {
  authStore.accessToken = token;
  setAccessToken(token);
}

export function saveRefreshToken(token: string | null) {
  authStore.refreshToken = token;
  setRefreshToken(token);
}

export function saveTokens(accessToken: string | null, refreshToken: string | null) {
  authStore.accessToken = accessToken;
  authStore.refreshToken = refreshToken;
  setAccessToken(accessToken);
  setRefreshToken(refreshToken);
}

export function saveUser(user: UserInfo | null) {
  authStore.user = user;
}
