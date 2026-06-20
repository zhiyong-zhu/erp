export interface PageResult<T> {
  records: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

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
}

export interface ProductionBatchPayload {
  batchNo: string;
  productId: string;
  plannedQuantity: number;
  unit?: string;
  status?: string;
  remark?: string;
}

export interface MaterialActionPayload {
  materialId: string;
  quantity: number;
  remark?: string;
}

export interface ProductionBoxRecord {
  id: string;
  boxCode: string;
  batchNo: string;
  productName?: string | null;
  packageName?: string | null;
  quantity: number;
  labelHtml?: string | null;
}

export interface InventoryReceiptRecord {
  id: string;
  receiptNo: string;
  sourceType: string;
  sourceOrderNo?: string | null;
  status: string;
}

export interface InventoryIssueRecord {
  id: string;
  issueNo: string;
  issueType: string;
  sourceOrderNo?: string | null;
  status: string;
  totalQuantity: number;
}

export interface InventoryCheckRecord {
  id: string;
  checkNo: string;
  checkType: string;
  status: string;
  totalDifference: number;
}
