import { http } from "./http";
import type { PageQuery, PageResult } from "../types/page";
import type {
  InventoryBalanceRecord,
  InventoryCheckPayload,
  InventoryCheckRecord,
  InventoryIssuePayload,
  InventoryIssueRecord,
  InventoryLocationPayload,
  InventoryLocationRecord,
  InventoryReceiptPayload,
  InventoryReceiptRecord,
  InventoryTransactionRecord,
  InventoryTransferPayload,
  InventoryTransferRecord,
  InventoryWarehousePayload,
  InventoryWarehouseRecord
} from "../types/inventory";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export async function fetchInventoryReceipts(query: PageQuery): Promise<PageResult<InventoryReceiptRecord>> {
  const response = await http.get<ApiResponse<PageResult<InventoryReceiptRecord>>>("/inventory/receipts", { params: query });
  return response.data.data;
}

export async function fetchInventoryWarehouses(query: PageQuery & {
  keyword?: string;
  status?: number;
}): Promise<PageResult<InventoryWarehouseRecord>> {
  const response = await http.get<ApiResponse<PageResult<InventoryWarehouseRecord>>>("/inventory/warehouses", { params: query });
  return response.data.data;
}

export async function createInventoryWarehouse(payload: InventoryWarehousePayload): Promise<InventoryWarehouseRecord> {
  const response = await http.post<ApiResponse<InventoryWarehouseRecord>>("/inventory/warehouses", payload);
  return response.data.data;
}

export async function updateInventoryWarehouse(id: string, payload: InventoryWarehousePayload): Promise<InventoryWarehouseRecord> {
  const response = await http.put<ApiResponse<InventoryWarehouseRecord>>(`/inventory/warehouses/${id}`, payload);
  return response.data.data;
}

export async function fetchInventoryLocations(query: PageQuery & {
  warehouseId?: string;
  keyword?: string;
  status?: number;
}): Promise<PageResult<InventoryLocationRecord>> {
  const response = await http.get<ApiResponse<PageResult<InventoryLocationRecord>>>("/inventory/locations", { params: query });
  return response.data.data;
}

export async function createInventoryLocation(payload: InventoryLocationPayload): Promise<InventoryLocationRecord> {
  const response = await http.post<ApiResponse<InventoryLocationRecord>>("/inventory/locations", payload);
  return response.data.data;
}

export async function updateInventoryLocation(id: string, payload: InventoryLocationPayload): Promise<InventoryLocationRecord> {
  const response = await http.put<ApiResponse<InventoryLocationRecord>>(`/inventory/locations/${id}`, payload);
  return response.data.data;
}

export async function createInventoryReceipt(payload: InventoryReceiptPayload): Promise<string> {
  const response = await http.post<ApiResponse<string>>("/inventory/receipts", payload);
  return response.data.data;
}

export async function fetchInventoryBalances(query: PageQuery & {
  materialName?: string;
  warehouseCode?: string;
  locationCode?: string;
  batchNo?: string;
}): Promise<PageResult<InventoryBalanceRecord>> {
  const response = await http.get<ApiResponse<PageResult<InventoryBalanceRecord>>>("/inventory/balances", { params: query });
  return response.data.data;
}

export async function exportInventoryBalances(query: {
  materialName?: string;
  warehouseCode?: string;
  locationCode?: string;
  batchNo?: string;
}): Promise<Blob> {
  const response = await http.get("/inventory/balances/export", { params: query, responseType: "blob" });
  return response.data;
}

export async function fetchInventoryIssues(query: PageQuery): Promise<PageResult<InventoryIssueRecord>> {
  const response = await http.get<ApiResponse<PageResult<InventoryIssueRecord>>>("/inventory/issues", { params: query });
  return response.data.data;
}

export async function createInventoryIssue(payload: InventoryIssuePayload): Promise<InventoryIssueRecord> {
  const response = await http.post<ApiResponse<InventoryIssueRecord>>("/inventory/issues", payload);
  return response.data.data;
}

export async function fetchInventoryTransfers(query: PageQuery): Promise<PageResult<InventoryTransferRecord>> {
  const response = await http.get<ApiResponse<PageResult<InventoryTransferRecord>>>("/inventory/transfers", { params: query });
  return response.data.data;
}

export async function createInventoryTransfer(payload: InventoryTransferPayload): Promise<InventoryTransferRecord> {
  const response = await http.post<ApiResponse<InventoryTransferRecord>>("/inventory/transfers", payload);
  return response.data.data;
}

export async function fetchInventoryChecks(query: PageQuery): Promise<PageResult<InventoryCheckRecord>> {
  const response = await http.get<ApiResponse<PageResult<InventoryCheckRecord>>>("/inventory/checks", { params: query });
  return response.data.data;
}

export async function createInventoryCheck(payload: InventoryCheckPayload): Promise<InventoryCheckRecord> {
  const response = await http.post<ApiResponse<InventoryCheckRecord>>("/inventory/checks", payload);
  return response.data.data;
}

export async function reviewInventoryCheck(id: string, remark?: string): Promise<InventoryCheckRecord> {
  const response = await http.post<ApiResponse<InventoryCheckRecord>>(`/inventory/checks/${id}/review`, { remark });
  return response.data.data;
}

export async function approveInventoryCheck(id: string, remark?: string): Promise<InventoryCheckRecord> {
  const response = await http.post<ApiResponse<InventoryCheckRecord>>(`/inventory/checks/${id}/approve`, { remark });
  return response.data.data;
}

export async function rejectInventoryCheck(id: string, remark?: string): Promise<InventoryCheckRecord> {
  const response = await http.post<ApiResponse<InventoryCheckRecord>>(`/inventory/checks/${id}/reject`, { remark });
  return response.data.data;
}

export async function fetchInventoryTransactions(query: PageQuery & {
  receiptId?: string;
  issueId?: string;
  transferId?: string;
  checkId?: string;
  sourceOrderId?: string;
}): Promise<PageResult<InventoryTransactionRecord>> {
  const response = await http.get<ApiResponse<PageResult<InventoryTransactionRecord>>>("/inventory/transactions", { params: query });
  return response.data.data;
}

export async function exportInventoryTransactions(query: {
  receiptId?: string;
  issueId?: string;
  transferId?: string;
  checkId?: string;
  sourceOrderId?: string;
}): Promise<Blob> {
  const response = await http.get("/inventory/transactions/export", { params: query, responseType: "blob" });
  return response.data;
}
