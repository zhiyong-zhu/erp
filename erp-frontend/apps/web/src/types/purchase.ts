export interface PurchaseOrderItemRecord {
  id: string;
  materialId: string;
  materialCode: string;
  materialName: string;
  unit?: string | null;
  quantity?: number | null;
  quotePrice?: number | null;
  estimatedAmount?: number | null;
  leadTimeDays?: number | null;
  sourceType?: string | null;
  sourceRefId?: string | null;
  receivedQuantity?: number | null;
  acceptedQuantity?: number | null;
  rejectedQuantity?: number | null;
  returnedQuantity?: number | null;
  inspectionResult?: string | null;
  exceptionReason?: string | null;
}

export interface PurchaseOrderRecord {
  id: string;
  orderNo: string;
  supplierId: string;
  supplierName: string;
  orderType: string;
  status: string;
  totalAmount?: number | null;
  sourceType?: string | null;
  remark?: string | null;
  createdAt?: string | null;
  receivedAt?: string | null;
  items: PurchaseOrderItemRecord[];
}

export interface PurchaseDraftGeneratePayload {
  materialIds: string[];
  remark?: string;
}

export interface PurchaseOrderItemUpdatePayload {
  id?: string;
  materialId: string;
  materialCode: string;
  materialName: string;
  unit?: string;
  quantity: number;
  quotePrice?: number;
  estimatedAmount?: number;
  leadTimeDays?: number;
  sourceType?: string;
  sourceRefId?: string;
  receivedQuantity?: number;
}

export interface PurchaseOrderUpdatePayload {
  supplierId: string;
  supplierName?: string;
  remark?: string;
  items: PurchaseOrderItemUpdatePayload[];
}

export interface PurchaseOrderStatusPayload {
  action: "submit" | "approve" | "reject" | "cancel";
  remark?: string;
}

export interface PurchaseOrderReceivePayload {
  items: Array<{
    itemId: string;
    receivedQuantity: number;
    acceptedQuantity: number;
    rejectedQuantity?: number;
    inspectionResult?: string;
    exceptionReason?: string;
  }>;
}

export interface PurchaseReturnPayload {
  items: Array<{
    itemId: string;
    returnQuantity: number;
    reason?: string;
  }>;
  remark?: string;
}

export interface PurchaseReturnItemRecord {
  id: string;
  purchaseOrderItemId: string;
  materialId: string;
  materialCode: string;
  materialName: string;
  unit?: string | null;
  returnQuantity?: number | null;
  quotePrice?: number | null;
  returnAmount?: number | null;
  reason?: string | null;
}

export interface PurchaseReturnRecord {
  id: string;
  returnNo: string;
  purchaseOrderId: string;
  purchaseOrderNo: string;
  supplierId?: string | null;
  supplierName?: string | null;
  status: string;
  totalAmount?: number | null;
  remark?: string | null;
  createdAt?: string | null;
  items: PurchaseReturnItemRecord[];
}

export interface PurchasePayableStatRecord {
  supplierId: string;
  supplierName: string;
  orderAmount?: number | null;
  returnAmount?: number | null;
  netPayableAmount?: number | null;
  orderCount?: number | null;
  returnCount?: number | null;
}

export interface PurchaseExceptionRecord {
  id: string;
  exceptionNo: string;
  purchaseOrderId: string;
  purchaseOrderItemId?: string | null;
  supplierName?: string | null;
  materialCode?: string | null;
  materialName?: string | null;
  exceptionType: string;
  status: string;
  description?: string | null;
  resolution?: string | null;
  createdAt?: string | null;
  handledAt?: string | null;
}

export interface PurchaseExceptionHandlePayload {
  action: "resolve" | "close";
  resolution?: string;
}
