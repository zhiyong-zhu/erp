import { getAccessToken, getRefreshToken, setAccessToken, setRefreshToken } from "../api/http";
import type { UserInfo } from "../types/auth";

export interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: UserInfo | null;
}

type Listener = () => void;

const listeners = new Set<Listener>();

const authState: AuthState = {
  accessToken: getAccessToken(),
  refreshToken: getRefreshToken(),
  user: null
};

export function getAuthState() {
  return authState;
}

export function subscribeAuth(listener: Listener) {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}

function emitChange() {
  for (const listener of listeners) {
    listener();
  }
}

export function saveAccessToken(token: string | null) {
  authState.accessToken = token;
  setAccessToken(token);
  emitChange();
}

export function saveRefreshToken(token: string | null) {
  authState.refreshToken = token;
  setRefreshToken(token);
  emitChange();
}

export function saveTokens(accessToken: string | null, refreshToken: string | null) {
  authState.accessToken = accessToken;
  authState.refreshToken = refreshToken;
  setAccessToken(accessToken);
  setRefreshToken(refreshToken);
  emitChange();
}

export function saveUser(user: UserInfo | null) {
  authState.user = user;
  emitChange();
}

export function clearAuth() {
  authState.accessToken = null;
  authState.refreshToken = null;
  authState.user = null;
  setAccessToken(null);
  setRefreshToken(null);
  emitChange();
}

export function hasPermission(permission: string) {
  return Boolean(authState.user?.permissions?.includes(permission));
}
