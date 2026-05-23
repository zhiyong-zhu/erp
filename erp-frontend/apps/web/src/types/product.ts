import type { BasePayload, BaseRecord } from "@erp/shared";

export interface ProductCategoryRecord extends BaseRecord {
  id: string;
  parentId?: string | null;
  name: string;
  code?: string | null;
  sortOrder: number;
  status: number;
  children?: ProductCategoryRecord[];
}

export interface ProductCategoryPayload extends BasePayload {
  parentId?: string | null;
  name: string;
  code?: string;
  sortOrder?: number;
  status?: number;
}

export interface ProductSkuRecord extends BaseRecord {
  rowId?: string;
  id?: string;
  skuCode: string;
  attributes: string;
  barcode?: string | null;
  price?: number | null;
  costPrice?: number | null;
  weight?: number | null;
  status?: number;
}

export interface ProductRecord extends BaseRecord {
  id: string;
  code: string;
  name: string;
  categoryId?: string | null;
  categoryName?: string | null;
  brand?: string | null;
  unit: string;
  description?: string | null;
  images: string[];
  specifications?: string | null;
  status: number;
  skuCount?: number | null;
  skus?: ProductSkuRecord[];
}

export interface ProductPackageRecord extends BaseRecord {
  id?: string;
  level: number;
  name: string;
  quantity: number;
  weight?: number | null;
  dimensions?: string | null;
  dimensionsDraft?: {
    length?: number | null;
    width?: number | null;
    height?: number | null;
    unit?: string | null;
  };
  barcode?: string | null;
  labelTemplateId?: string | null;
}

export interface ProductBomItemRecord extends BaseRecord {
  rowId?: string;
  id?: string;
  materialId: string;
  materialType?: number;
  quantity: number;
  unit?: string | null;
  lossRate?: number | null;
  remark?: string | null;
  sortOrder?: number | null;
}

export interface ProductBomRecord extends BaseRecord {
  id?: string;
  version?: string | null;
  status?: number;
  effectiveDate?: string | null;
  items?: ProductBomItemRecord[];
}

export interface LabelTemplateRecord extends BaseRecord {
  id?: string;
  name: string;
  widthMm: number;
  heightMm: number;
  templateConfig: string;
  templateConfigDraft?: string;
  previewImage?: string | null;
  status?: number;
}

export interface LabelPrintItemPayload {
  skuId: string;
  packageLevel: number;
  labelTemplateId: string;
  quantity: number;
}

export interface LabelPrintPayload {
  items: LabelPrintItemPayload[];
  printerId?: string;
  printMode: string;
}

export interface LabelPrintResult {
  pdfUrl: string;
  totalCount: number;
  summary: string;
  previewHtml?: string;
}

export interface ProductPayload extends BasePayload {
  code: string;
  name: string;
  categoryId?: string | null;
  brand?: string;
  unit: string;
  description?: string;
  images?: string[];
  specifications?: string;
  status?: number;
  skus: ProductSkuRecord[];
}

export interface ProductUpdatePayload extends BasePayload {
  name: string;
  categoryId?: string | null;
  brand?: string;
  unit: string;
  description?: string;
  images?: string[];
  specifications?: string;
  status?: number;
  skus: ProductSkuRecord[];
}

export interface ProductStatusFlowPayload {
  action: "submit" | "enable" | "disable" | "reject";
}
