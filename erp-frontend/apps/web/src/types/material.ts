import type { BasePayload, BaseRecord } from "@erp/shared";

export interface MaterialCategoryRecord extends BaseRecord {
  id: string;
  parentId?: string | null;
  name: string;
  code?: string | null;
  sortOrder: number;
  status: number;
  children?: MaterialCategoryRecord[];
}

export interface MaterialCategoryPayload extends BasePayload {
  parentId?: string | null;
  name: string;
  code?: string;
  sortOrder?: number;
  status?: number;
}

export interface SupplierRecord extends BaseRecord {
  id: string;
  code: string;
  name: string;
  shortName?: string | null;
  contactPerson?: string | null;
  phone?: string | null;
  email?: string | null;
  address?: string | null;
  bankName?: string | null;
  bankAccount?: string | null;
  taxNumber?: string | null;
  creditRating?: number | null;
  status: number;
}

export interface SupplierPayload extends BasePayload {
  code: string;
  name: string;
  shortName?: string;
  contactPerson?: string;
  phone?: string;
  email?: string;
  address?: string;
  bankName?: string;
  bankAccount?: string;
  taxNumber?: string;
  creditRating?: number;
  status?: number;
}

export interface MaterialRecord extends BaseRecord {
  id: string;
  code: string;
  name: string;
  categoryId?: string | null;
  categoryName?: string | null;
  unit: string;
  specifications?: string | null;
  defaultSupplierId?: string | null;
  defaultSupplierName?: string | null;
  safetyStock?: number | null;
  leadTimeDays?: number | null;
  status: number;
}

export interface MaterialPayload extends BasePayload {
  code: string;
  name: string;
  categoryId?: string | null;
  unit: string;
  specifications?: string;
  defaultSupplierId?: string | null;
  safetyStock?: number;
  leadTimeDays?: number;
  status?: number;
}
