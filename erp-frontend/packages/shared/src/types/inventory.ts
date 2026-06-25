export interface InventoryReceiptRecord {
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

export interface InventoryIssueRecord {
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
  sourceOrderNo?: string;
  remark?: string;
  items: Array<{ materialId: string; quantity: number; remark?: string }>;
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

export interface InventoryCheckPayload {
  checkType?: string;
  remark?: string;
  items: Array<{ materialId: string; actualQuantity: number; remark?: string }>;
}

export interface InventoryTransactionRecord {
  id: string;
  materialId: string;
  materialCode: string;
  materialName: string;
  transactionType: string;
  quantity: number;
  balanceAfter: number;
  sourceType?: string | null;
  sourceOrderNo?: string | null;
  remark?: string | null;
  createdAt?: string;
}
