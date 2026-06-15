import type { BasePayload, BaseRecord } from "@erp/shared";

export interface ProductionProcessStepRecord {
  id?: string;
  rowId?: string;
  stepNo: number;
  name: string;
  workstation?: string | null;
  standardMinutes?: number | null;
  qualityRequirement?: string | null;
  remark?: string | null;
}

export interface ProductionProcessRecord extends BaseRecord {
  id: string;
  code: string;
  name: string;
  productId?: string | null;
  productName?: string | null;
  version: string;
  status: number;
  remark?: string | null;
  steps?: ProductionProcessStepRecord[];
}

export interface ProductionProcessPayload extends BasePayload {
  code: string;
  name: string;
  productId?: string | null;
  version: string;
  status?: number;
  remark?: string;
  steps: ProductionProcessStepRecord[];
}

export interface ProductionBomItemRecord {
  id?: string;
  rowId?: string;
  materialId: string;
  materialCode?: string | null;
  materialName?: string | null;
  quantity: number;
  unit?: string | null;
  lossRate?: number | null;
  processStepNo?: number | null;
  remark?: string | null;
}

export interface ProductionBomRecord extends BaseRecord {
  id: string;
  code: string;
  productId: string;
  productCode?: string | null;
  productName?: string | null;
  version: string;
  status: number;
  effectiveDate?: string | null;
  remark?: string | null;
  items?: ProductionBomItemRecord[];
}

export interface ProductionBomPayload extends BasePayload {
  code: string;
  productId: string;
  version: string;
  status?: number;
  effectiveDate?: string;
  remark?: string;
  items: ProductionBomItemRecord[];
}

export interface ProductionBatchRecord extends BaseRecord {
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

export interface ProductionBatchPayload extends BasePayload {
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

export interface ProductionReportRecord extends BaseRecord {
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

export interface ProductionReportPayload extends BasePayload {
  batchId: string;
  reportNo?: string;
  reportQuantity: number;
  goodQuantity?: number;
  defectQuantity?: number;
  reportAt?: string;
  operatorName?: string;
  remark?: string;
}

export interface SerialNumberGeneratePayload extends BasePayload {
  quantity?: number;
  prefix?: string;
}

export interface ProductionBoxRecord extends BaseRecord {
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

export interface ProductionBoxPayload extends BasePayload {
  batchId: string;
  packageId: string;
  quantity?: number;
  serialNos?: string[];
  remark?: string;
}

export interface ProductionProductStockRecord extends BaseRecord {
  id: string;
  productId: string;
  productCode?: string | null;
  productName?: string | null;
  currentStock: number;
}

export interface SerialNumberRecord extends BaseRecord {
  id: string;
  serialNo: string;
  batchId?: string | null;
  batchNo?: string | null;
  productId: string;
  productCode?: string | null;
  productName?: string | null;
  status: string;
  producedAt?: string | null;
  shippedAt?: string | null;
  remark?: string | null;
}

export interface SerialNumberPayload extends BasePayload {
  serialNo: string;
  batchId?: string | null;
  productId: string;
  status?: string;
  producedAt?: string;
  shippedAt?: string;
  remark?: string;
}
