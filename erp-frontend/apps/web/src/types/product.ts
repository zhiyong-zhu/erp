export interface ProductCategoryRecord {
  id: string;
  parentId?: string | null;
  name: string;
  code?: string | null;
  sortOrder: number;
  status: number;
  createdAt?: string;
  children?: ProductCategoryRecord[];
}

export interface ProductCategoryPayload {
  parentId?: string | null;
  name: string;
  code?: string;
  sortOrder?: number;
  status?: number;
}

export interface ProductSkuRecord {
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

export interface ProductRecord {
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
  createdAt?: string;
}

export interface ProductPackageRecord {
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
  createdAt?: string;
}

export interface LabelTemplateRecord {
  id?: string;
  name: string;
  widthMm: number;
  heightMm: number;
  templateConfig: string;
  previewImage?: string | null;
  status?: number;
  createdAt?: string;
}

export interface ProductPayload {
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

export interface ProductUpdatePayload {
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
