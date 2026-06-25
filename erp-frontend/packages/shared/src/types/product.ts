export interface ProductRecord {
  id: string;
  code: string;
  name: string;
  unit: string;
  status?: number;
  categoryName?: string;
  skus?: Array<{ id?: string; skuCode: string; barcode?: string | null }>;
}

export interface ProductPackageRecord {
  id?: string;
  level: number;
  name: string;
  quantity: number;
  labelTemplateId?: string | null;
}

export interface LabelTemplateRecord {
  id?: string;
  name: string;
}

export interface LabelPrintResult {
  pdfUrl: string;
  totalCount: number;
  summary: string;
  previewHtml?: string;
}
