import { http } from "./http";
import type { PageQuery, PageResult } from "../types/page";
import type {
  PurchaseDraftGeneratePayload,
  PurchaseOrderReceivePayload,
  PurchaseOrderRecord,
  PurchaseOrderStatusPayload,
  PurchaseOrderUpdatePayload,
  PurchaseExceptionHandlePayload,
  PurchaseExceptionRecord,
  PurchasePayableStatRecord,
  PurchasePaymentPayload,
  PurchasePaymentRecord,
  PurchaseReturnPayload,
  PurchaseReturnRecord
} from "../types/purchase";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export async function fetchPurchaseOrders(query: PageQuery): Promise<PageResult<PurchaseOrderRecord>> {
  const response = await http.get<ApiResponse<PageResult<PurchaseOrderRecord>>>("/purchase/orders", { params: query });
  return response.data.data;
}

export async function fetchPurchaseOrderDetail(id: string): Promise<PurchaseOrderRecord> {
  const response = await http.get<ApiResponse<PurchaseOrderRecord>>(`/purchase/orders/${id}`);
  return response.data.data;
}

export async function generatePurchaseDraftsFromReplenishment(payload: PurchaseDraftGeneratePayload): Promise<PageResult<PurchaseOrderRecord>> {
  const response = await http.post<ApiResponse<PageResult<PurchaseOrderRecord>>>("/purchase/orders/generate-from-replenishment", payload);
  return response.data.data;
}

export async function updatePurchaseOrder(id: string, payload: PurchaseOrderUpdatePayload): Promise<PurchaseOrderRecord> {
  const response = await http.put<ApiResponse<PurchaseOrderRecord>>(`/purchase/orders/${id}`, payload);
  return response.data.data;
}

export async function createPurchaseOrder(payload: PurchaseOrderUpdatePayload): Promise<PurchaseOrderRecord> {
  const response = await http.post<ApiResponse<PurchaseOrderRecord>>("/purchase/orders", payload);
  return response.data.data;
}

export async function changePurchaseOrderStatus(id: string, payload: PurchaseOrderStatusPayload): Promise<PurchaseOrderRecord> {
  const response = await http.post<ApiResponse<PurchaseOrderRecord>>(`/purchase/orders/${id}/status`, payload);
  return response.data.data;
}

export async function receivePurchaseOrder(id: string, payload: PurchaseOrderReceivePayload): Promise<PurchaseOrderRecord> {
  const response = await http.post<ApiResponse<PurchaseOrderRecord>>(`/purchase/orders/${id}/receive`, payload);
  return response.data.data;
}

export async function createPurchaseReturn(purchaseOrderId: string, payload: PurchaseReturnPayload): Promise<PurchaseReturnRecord> {
  const response = await http.post<ApiResponse<PurchaseReturnRecord>>(`/purchase/returns/${purchaseOrderId}`, payload);
  return response.data.data;
}

export async function fetchPurchaseReturns(query: PageQuery): Promise<PageResult<PurchaseReturnRecord>> {
  const response = await http.get<ApiResponse<PageResult<PurchaseReturnRecord>>>("/purchase/returns", { params: query });
  return response.data.data;
}

export async function fetchPurchasePayables(query: PageQuery): Promise<PageResult<PurchasePayableStatRecord>> {
  const response = await http.get<ApiResponse<PageResult<PurchasePayableStatRecord>>>("/purchase/orders/payables", { params: query });
  return response.data.data;
}

export async function fetchPurchaseExceptions(query: PageQuery): Promise<PageResult<PurchaseExceptionRecord>> {
  const response = await http.get<ApiResponse<PageResult<PurchaseExceptionRecord>>>("/purchase/exceptions", { params: query });
  return response.data.data;
}

export async function handlePurchaseException(id: string, payload: PurchaseExceptionHandlePayload): Promise<PurchaseExceptionRecord> {
  const response = await http.post<ApiResponse<PurchaseExceptionRecord>>(`/purchase/exceptions/${id}/handle`, payload);
  return response.data.data;
}

export async function createPurchasePayment(purchaseOrderId: string, payload: PurchasePaymentPayload): Promise<PurchasePaymentRecord> {
  const response = await http.post<ApiResponse<PurchasePaymentRecord>>(`/purchase/payments/${purchaseOrderId}`, payload);
  return response.data.data;
}

export async function fetchPurchasePayments(query: PageQuery & { purchaseOrderId?: string; supplierId?: string }): Promise<PageResult<PurchasePaymentRecord>> {
  const response = await http.get<ApiResponse<PageResult<PurchasePaymentRecord>>>("/purchase/payments", { params: query });
  return response.data.data;
}
