import type { BaseRecord } from "@erp/shared";

export interface InventoryWarehouseRecord extends BaseRecord {
  id: string;
  code: string;
  name: string;
  address?: string | null;
  managerName?: string | null;
  phone?: string | null;
  sortOrder?: number | null;
  status: number;
  remark?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface InventoryWarehousePayload {
  code: string;
  name: string;
  address?: string;
  managerName?: string;
  phone?: string;
  sortOrder?: number;
  status?: number;
  remark?: string;
}

export interface InventoryLocationRecord extends BaseRecord {
  id: string;
  warehouseId: string;
  warehouseCode: string;
  warehouseName: string;
  code: string;
  name: string;
  areaCode?: string | null;
  areaName?: string | null;
  sortOrder?: number | null;
  status: number;
  remark?: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface InventoryLocationPayload {
  warehouseId: string;
  code: string;
  name: string;
  areaCode?: string;
  areaName?: string;
  sortOrder?: number;
  status?: number;
  remark?: string;
}

export interface InventoryBalanceRecord extends BaseRecord {
  id: string;
  materialId: string;
  materialCode: string;
  materialName: string;
  warehouseCode: string;
  warehouseName: string;
  locationCode: string;
  locationName: string;
  batchNo: string;
  availableQuantity: number;
  frozenQuantity: number;
  totalQuantity: number;
  updatedAt?: string;
}

export interface InventoryReceiptRecord extends BaseRecord {
  id: string;
  receiptNo: string;
  sourceType: string;
  sourceOrderId?: string | null;
  sourceOrderNo?: string | null;
  supplierId?: string | null;
  supplierName?: string | null;
  idempotencyKey?: string | null;
  status: string;
  remark?: string | null;
  createdAt?: string;
}

export interface InventoryReceiptPayload {
  sourceType?: string;
  sourceOrderId?: string | null;
  sourceOrderNo?: string;
  idempotencyKey?: string;
  supplierId?: string | null;
  supplierName?: string;
  warehouseCode?: string;
  warehouseName?: string;
  locationCode?: string;
  locationName?: string;
  batchNo?: string;
  remark?: string;
  items: Array<{
    materialId: string;
    materialCode?: string;
    materialName?: string;
    sourceItemId?: string | null;
    quantity: number;
    warehouseCode?: string;
    warehouseName?: string;
    locationCode?: string;
    locationName?: string;
    batchNo?: string;
  }>;
}

export interface InventoryIssueRecord extends BaseRecord {
  id: string;
  issueNo: string;
  issueType: string;
  sourceOrderId?: string | null;
  sourceOrderNo?: string | null;
  idempotencyKey?: string | null;
  status: string;
  totalQuantity: number;
  remark?: string | null;
  createdAt?: string;
}

export interface InventoryIssuePayload {
  issueType?: string;
  sourceOrderId?: string | null;
  sourceOrderNo?: string;
  idempotencyKey?: string;
  warehouseCode?: string;
  warehouseName?: string;
  locationCode?: string;
  locationName?: string;
  batchNo?: string;
  remark?: string;
  items: Array<{
    materialId: string;
    quantity: number;
    warehouseCode?: string;
    warehouseName?: string;
    locationCode?: string;
    locationName?: string;
    batchNo?: string;
    remark?: string;
  }>;
}

export interface InventoryTransferRecord extends BaseRecord {
  id: string;
  transferNo: string;
  fromLocation: string;
  toLocation: string;
  status: string;
  totalQuantity: number;
  remark?: string | null;
  createdAt?: string;
}

export interface InventoryTransferPayload {
  fromWarehouseCode?: string;
  fromWarehouseName?: string;
  fromLocation: string;
  fromLocationCode?: string;
  fromLocationName?: string;
  fromBatchNo?: string;
  toWarehouseCode?: string;
  toWarehouseName?: string;
  toLocation: string;
  toLocationCode?: string;
  toLocationName?: string;
  toBatchNo?: string;
  remark?: string;
  items: Array<{
    materialId: string;
    quantity: number;
    fromWarehouseCode?: string;
    fromWarehouseName?: string;
    fromLocationCode?: string;
    fromLocationName?: string;
    fromBatchNo?: string;
    toWarehouseCode?: string;
    toWarehouseName?: string;
    toLocationCode?: string;
    toLocationName?: string;
    toBatchNo?: string;
    remark?: string;
  }>;
}

export interface InventoryCheckRecord extends BaseRecord {
  id: string;
  checkNo: string;
  checkType: string;
  status: string;
  totalDifference: number;
  remark?: string | null;
  createdAt?: string;
  reviewedAt?: string | null;
  reviewRemark?: string | null;
  approvedAt?: string | null;
  approvalRemark?: string | null;
  rejectedAt?: string | null;
  rejectRemark?: string | null;
}

export interface InventoryCheckPayload {
  checkType?: string;
  warehouseCode?: string;
  warehouseName?: string;
  locationCode?: string;
  locationName?: string;
  batchNo?: string;
  remark?: string;
  items: Array<{
    materialId: string;
    actualQuantity: number;
    warehouseCode?: string;
    warehouseName?: string;
    locationCode?: string;
    locationName?: string;
    batchNo?: string;
    remark?: string;
  }>;
}

export interface InventoryTransactionRecord extends BaseRecord {
  id: string;
  materialId: string;
  materialCode: string;
  materialName: string;
  transactionType: string;
  quantity: number;
  balanceBefore?: number | null;
  balanceAfter: number;
  warehouseCode?: string | null;
  warehouseName?: string | null;
  locationCode?: string | null;
  locationName?: string | null;
  batchNo?: string | null;
  sourceType?: string | null;
  sourceOrderId?: string | null;
  sourceOrderNo?: string | null;
  sourceItemId?: string | null;
  receiptId?: string | null;
  issueId?: string | null;
  transferId?: string | null;
  checkId?: string | null;
  idempotencyKey?: string | null;
  remark?: string | null;
  createdAt?: string;
}
