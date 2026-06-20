import { http } from "./http";
import type { PageQuery, PageResult } from "../types/page";
import type {
  InventoryCheckPayload,
  InventoryCheckRecord,
  InventoryIssuePayload,
  InventoryIssueRecord,
  InventoryReceiptRecord,
  InventoryTransactionRecord,
  InventoryTransferPayload,
  InventoryTransferRecord
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

export async function fetchInventoryTransactions(query: PageQuery): Promise<PageResult<InventoryTransactionRecord>> {
  const response = await http.get<ApiResponse<PageResult<InventoryTransactionRecord>>>("/inventory/transactions", { params: query });
  return response.data.data;
}
