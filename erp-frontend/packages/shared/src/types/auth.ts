export interface UserInfo {
  id: string;
  username: string;
  realName?: string;
  phone?: string;
  email?: string;
  avatar?: string;
  departmentId?: string;
  departmentName?: string;
  status: number;
  permissions: string[];
  roles?: string[];
}

export interface LoginPayload {
  username: string;
  password: string;
}

export interface LoginResult {
  accessToken: string;
  refreshToken?: string;
  expiresIn?: number;
}

export interface RefreshPayload {
  refreshToken: string;
}
