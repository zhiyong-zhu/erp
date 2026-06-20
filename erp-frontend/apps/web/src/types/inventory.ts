import type { BaseRecord } from "@erp/shared";

export interface InventoryReceiptRecord extends BaseRecord {
  id: string;
  receiptNo: string;
  sourceType: string;
  sourceOrderId?: string | null;
  sourceOrderNo?: string | null;
  supplierId?: string | null;
  supplierName?: string | null;
  status: string;
  remark?: string | null;
  createdAt?: string;
}

export interface InventoryIssueRecord extends BaseRecord {
  id: string;
  issueNo: string;
  issueType: string;
  sourceOrderId?: string | null;
  sourceOrderNo?: string | null;
  status: string;
  totalQuantity: number;
  remark?: string | null;
  createdAt?: string;
}

export interface InventoryIssuePayload {
  issueType?: string;
  sourceOrderId?: string | null;
  sourceOrderNo?: string;
  remark?: string;
  items: Array<{
    materialId: string;
    quantity: number;
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
  fromLocation: string;
  toLocation: string;
  remark?: string;
  items: Array<{
    materialId: string;
    quantity: number;
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
}

export interface InventoryCheckPayload {
  checkType?: string;
  remark?: string;
  items: Array<{
    materialId: string;
    actualQuantity: number;
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
  balanceAfter: number;
  sourceType?: string | null;
  sourceOrderId?: string | null;
  sourceOrderNo?: string | null;
  sourceItemId?: string | null;
  receiptId?: string | null;
  issueId?: string | null;
  transferId?: string | null;
  checkId?: string | null;
  remark?: string | null;
  createdAt?: string;
}
