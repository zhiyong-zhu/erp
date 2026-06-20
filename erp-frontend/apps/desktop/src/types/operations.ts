import type { PageResult } from "./page";

export interface ProductRecord {
  id: string;
  code: string;
  name: string;
  unit: string;
  skus?: Array<{ id?: string; skuCode: string; barcode?: string | null }>;
}

export interface ProductPackageRecord {
  id?: string;
  level: number;
  name: string;
  quantity: number;
  labelTemplateId?: string | null;
}

export interface MaterialRecord {
  id: string;
  code: string;
  name: string;
  unit: string;
  currentStock?: number | null;
}

export interface ProductionBatchRecord {
  id: string;
  batchNo: string;
  productId: string;
  productCode?: string | null;
  productName?: string | null;
  plannedQuantity: number;
  completedQuantity?: number | null;
  unit?: string | null;
  status: string;
  plannedStartDate?: string | null;
  plannedEndDate?: string | null;
  remark?: string | null;
}

export interface ProductionBatchPayload {
  batchNo: string;
  productId: string;
  plannedQuantity: number;
  completedQuantity?: number;
  unit?: string;
  status?: string;
  plannedStartDate?: string;
  plannedEndDate?: string;
  remark?: string;
}

export interface ProductionReportPayload {
  batchId: string;
  reportNo?: string;
  reportQuantity: number;
  goodQuantity?: number;
  defectQuantity?: number;
  operatorName?: string;
  remark?: string;
}

export interface ProductionBoxPayload {
  batchId: string;
  packageId: string;
  quantity?: number;
  remark?: string;
}

export interface ProductionBoxRecord {
  id: string;
  boxCode: string;
  batchId: string;
  batchNo: string;
  productName?: string | null;
  packageName?: string | null;
  quantity: number;
  labelHtml?: string | null;
  status: string;
}

export interface InventoryReceiptRecord {
  id: string;
  receiptNo: string;
  sourceType: string;
  sourceOrderNo?: string | null;
  supplierName?: string | null;
  status: string;
  remark?: string | null;
  createdAt?: string;
}

export interface InventoryIssuePayload {
  issueType?: string;
  sourceOrderNo?: string;
  remark?: string;
  items: Array<{ materialId: string; quantity: number; remark?: string }>;
}

export interface InventoryIssueRecord {
  id: string;
  issueNo: string;
  issueType: string;
  sourceOrderNo?: string | null;
  status: string;
  totalQuantity: number;
  remark?: string | null;
  createdAt?: string;
}

export interface InventoryCheckPayload {
  checkType?: string;
  remark?: string;
  items: Array<{ materialId: string; actualQuantity: number; remark?: string }>;
}

export interface InventoryCheckRecord {
  id: string;
  checkNo: string;
  checkType: string;
  status: string;
  totalDifference: number;
  remark?: string | null;
  createdAt?: string;
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

export type Paged<T> = PageResult<T>;
