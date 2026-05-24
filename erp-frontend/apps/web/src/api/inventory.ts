import { http } from "./http";
import type { PageQuery, PageResult } from "../types/page";
import type { InventoryReceiptRecord, InventoryTransactionRecord } from "../types/inventory";

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

export async function fetchInventoryTransactions(query: PageQuery): Promise<PageResult<InventoryTransactionRecord>> {
  const response = await http.get<ApiResponse<PageResult<InventoryTransactionRecord>>>("/inventory/transactions", { params: query });
  return response.data.data;
}
