export interface DepartmentRecord {
  id: string;
  parentId?: string | null;
  name: string;
  code: string;
  leader?: string | null;
  phone?: string | null;
  sortOrder: number;
  status: number;
  children?: DepartmentRecord[];
}

export interface DepartmentPayload {
  parentId?: string | null;
  name: string;
  code: string;
  leader?: string;
  phone?: string;
  sortOrder?: number;
}

export interface RoleRecord {
  id: string;
  name: string;
  code: string;
  description?: string | null;
  dataScope: number;
  status: number;
  permissionIds?: string[];
}

export interface RolePayload {
  name: string;
  code: string;
  description?: string;
  dataScope?: number;
  permissionIds?: string[];
}
