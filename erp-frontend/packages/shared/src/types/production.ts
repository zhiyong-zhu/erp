export interface ProductionBatchRecord {
  id: string;
  batchNo: string;
  productId: string;
  productCode?: string | null;
  productName?: string | null;
  plannedQuantity: number;
  completedQuantity?: number | null;
  unit?: string | null;
  processId?: string | null;
  processName?: string | null;
  bomId?: string | null;
  bomCode?: string | null;
  status: string;
  plannedStartDate?: string | null;
  plannedEndDate?: string | null;
  startedAt?: string | null;
  completedAt?: string | null;
  remark?: string | null;
}

export interface ProductionBatchPayload {
  batchNo: string;
  productId: string;
  plannedQuantity: number;
  completedQuantity?: number;
  unit?: string;
  processId?: string | null;
  bomId?: string | null;
  status?: string;
  plannedStartDate?: string;
  plannedEndDate?: string;
  remark?: string;
}

export interface ProductionReportRecord {
  id: string;
  reportNo: string;
  batchId: string;
  batchNo: string;
  productId: string;
  productCode?: string | null;
  productName?: string | null;
  reportQuantity: number;
  goodQuantity: number;
  defectQuantity: number;
  reportAt: string;
  operatorName?: string | null;
  status: string;
  remark?: string | null;
}

export interface ProductionReportPayload {
  batchId: string;
  reportNo?: string;
  reportQuantity: number;
  goodQuantity?: number;
  defectQuantity?: number;
  reportAt?: string;
  operatorName?: string;
  remark?: string;
}

export interface ProductionBoxRecord {
  id: string;
  boxCode: string;
  batchId: string;
  batchNo: string;
  productId: string;
  productCode?: string | null;
  productName?: string | null;
  packageId?: string | null;
  packageName?: string | null;
  packageLevel?: number | null;
  quantity: number;
  serialNos?: string[];
  labelHtml?: string | null;
  status: string;
  remark?: string | null;
}

export interface ProductionBoxPayload {
  batchId: string;
  packageId: string;
  quantity?: number;
  serialNos?: string[];
  remark?: string;
}
