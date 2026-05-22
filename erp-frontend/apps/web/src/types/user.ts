export interface UserRecord {
  id: string;
  username: string;
  realName: string;
  phone?: string | null;
  email?: string | null;
  departmentId?: string | null;
  status: number;
  roleCodes: string[];
  createdAt?: string;
}

export interface UserCreatePayload {
  username: string;
  password: string;
  realName: string;
  phone?: string;
  email?: string;
  departmentId?: string | null;
  roleIds: string[];
}

export interface UserUpdatePayload {
  realName?: string;
  phone?: string;
  email?: string;
  departmentId?: string | null;
  roleIds: string[];
}
