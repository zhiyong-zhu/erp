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
  currentStock?: number | null;
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
  currentStock?: number;
  leadTimeDays?: number;
  status?: number;
}

export interface MaterialAlertRecord extends MaterialRecord {
  shortageAmount?: number | null;
}

export interface SupplierQuoteRecord extends BaseRecord {
  id: string;
  supplierId: string;
  supplierName?: string | null;
  materialId: string;
  materialName?: string | null;
  quotePrice: number;
  currency: string;
  minOrderQuantity?: number | null;
  leadTimeDays?: number | null;
  remark?: string | null;
  effectiveDate?: string | null;
  expiryDate?: string | null;
}

export interface SupplierQuotePayload extends BasePayload {
  supplierId: string;
  materialId: string;
  quotePrice: number;
  currency: string;
  minOrderQuantity?: number;
  leadTimeDays?: number;
  remark?: string;
  effectiveDate?: string;
  expiryDate?: string;
}

export interface MaterialReplenishmentRecord {
  materialId: string;
  materialCode: string;
  materialName: string;
  unit: string;
  currentStock?: number | null;
  safetyStock?: number | null;
  shortageAmount?: number | null;
  suggestedQuantity?: number | null;
  supplierId?: string | null;
  supplierName?: string | null;
  quotePrice?: number | null;
  currency?: string | null;
  estimatedAmount?: number | null;
  leadTimeDays?: number | null;
  recommendationReason?: string | null;
}
