import { getAccessToken, setAccessToken } from "../api/http";
import type { UserInfo } from "../types/auth";

export interface AuthState {
  accessToken: string | null;
  user: UserInfo | null;
}

export const authStore: AuthState = {
  accessToken: getAccessToken(),
  user: null
};

export function saveAccessToken(token: string | null) {
  authStore.accessToken = token;
  setAccessToken(token);
}

export function saveUser(user: UserInfo | null) {
  authStore.user = user;
}
