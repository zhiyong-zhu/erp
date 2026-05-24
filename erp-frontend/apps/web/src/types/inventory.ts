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
  remark?: string | null;
  createdAt?: string;
}
