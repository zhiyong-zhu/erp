import { http } from "./http";
import type { PageQuery, PageResult } from "../types/page";
import type {
  ProductionBatchPayload,
  ProductionBatchRecord,
  ProductionBoxPayload,
  ProductionBoxRecord,
  ProductionBomPayload,
  ProductionBomRecord,
  ProductionProductStockRecord,
  ProductionProcessPayload,
  ProductionProcessRecord,
  ProductionReportPayload,
  ProductionReportRecord,
  SerialNumberGeneratePayload,
  SerialNumberPayload,
  SerialNumberRecord
} from "../types/production";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export async function fetchProductionProcesses(query: PageQuery & { name?: string; productId?: string; status?: number }): Promise<PageResult<ProductionProcessRecord>> {
  const response = await http.get<ApiResponse<PageResult<ProductionProcessRecord>>>("/production/processes", { params: query });
  return response.data.data;
}

export async function createProductionProcess(payload: ProductionProcessPayload): Promise<ProductionProcessRecord> {
  const response = await http.post<ApiResponse<ProductionProcessRecord>>("/production/processes", payload);
  return response.data.data;
}

export async function updateProductionProcess(id: string, payload: ProductionProcessPayload): Promise<ProductionProcessRecord> {
  const response = await http.put<ApiResponse<ProductionProcessRecord>>(`/production/processes/${id}`, payload);
  return response.data.data;
}

export async function fetchProductionBoms(query: PageQuery & { productId?: string; status?: number }): Promise<PageResult<ProductionBomRecord>> {
  const response = await http.get<ApiResponse<PageResult<ProductionBomRecord>>>("/production/boms", { params: query });
  return response.data.data;
}

export async function createProductionBom(payload: ProductionBomPayload): Promise<ProductionBomRecord> {
  const response = await http.post<ApiResponse<ProductionBomRecord>>("/production/boms", payload);
  return response.data.data;
}

export async function updateProductionBom(id: string, payload: ProductionBomPayload): Promise<ProductionBomRecord> {
  const response = await http.put<ApiResponse<ProductionBomRecord>>(`/production/boms/${id}`, payload);
  return response.data.data;
}

export async function fetchProductionBatches(query: PageQuery & { batchNo?: string; productId?: string; status?: string }): Promise<PageResult<ProductionBatchRecord>> {
  const response = await http.get<ApiResponse<PageResult<ProductionBatchRecord>>>("/production/batches", { params: query });
  return response.data.data;
}

export async function createProductionBatch(payload: ProductionBatchPayload): Promise<ProductionBatchRecord> {
  const response = await http.post<ApiResponse<ProductionBatchRecord>>("/production/batches", payload);
  return response.data.data;
}

export async function updateProductionBatch(id: string, payload: ProductionBatchPayload): Promise<ProductionBatchRecord> {
  const response = await http.put<ApiResponse<ProductionBatchRecord>>(`/production/batches/${id}`, payload);
  return response.data.data;
}

export async function startProductionBatch(id: string): Promise<ProductionBatchRecord> {
  const response = await http.post<ApiResponse<ProductionBatchRecord>>(`/production/batches/${id}/start`);
  return response.data.data;
}

export async function receiveProductionBatch(id: string): Promise<ProductionBatchRecord> {
  const response = await http.post<ApiResponse<ProductionBatchRecord>>(`/production/batches/${id}/receipt`);
  return response.data.data;
}

export async function generateBatchSerialNumbers(id: string, payload: SerialNumberGeneratePayload): Promise<SerialNumberRecord[]> {
  const response = await http.post<ApiResponse<SerialNumberRecord[]>>(`/production/batches/${id}/serial-numbers/generate`, payload);
  return response.data.data;
}

export async function fetchProductionReports(query: PageQuery & { batchNo?: string; productId?: string; status?: string }): Promise<PageResult<ProductionReportRecord>> {
  const response = await http.get<ApiResponse<PageResult<ProductionReportRecord>>>("/production/reports", { params: query });
  return response.data.data;
}

export async function createProductionReport(payload: ProductionReportPayload): Promise<ProductionReportRecord> {
  const response = await http.post<ApiResponse<ProductionReportRecord>>("/production/reports", payload);
  return response.data.data;
}

export async function fetchProductionBoxes(query: PageQuery & { batchNo?: string; productId?: string; status?: string }): Promise<PageResult<ProductionBoxRecord>> {
  const response = await http.get<ApiResponse<PageResult<ProductionBoxRecord>>>("/production/boxes", { params: query });
  return response.data.data;
}

export async function createProductionBox(payload: ProductionBoxPayload): Promise<ProductionBoxRecord> {
  const response = await http.post<ApiResponse<ProductionBoxRecord>>("/production/boxes", payload);
  return response.data.data;
}

export async function fetchProductionProductStock(query: PageQuery & { productName?: string }): Promise<PageResult<ProductionProductStockRecord>> {
  const response = await http.get<ApiResponse<PageResult<ProductionProductStockRecord>>>("/production/product-stock", { params: query });
  return response.data.data;
}

export async function fetchSerialNumbers(query: PageQuery & { serialNo?: string; batchId?: string; productId?: string; status?: string }): Promise<PageResult<SerialNumberRecord>> {
  const response = await http.get<ApiResponse<PageResult<SerialNumberRecord>>>("/production/serial-numbers", { params: query });
  return response.data.data;
}

export async function createSerialNumber(payload: SerialNumberPayload): Promise<SerialNumberRecord> {
  const response = await http.post<ApiResponse<SerialNumberRecord>>("/production/serial-numbers", payload);
  return response.data.data;
}

export async function updateSerialNumber(id: string, payload: SerialNumberPayload): Promise<SerialNumberRecord> {
  const response = await http.put<ApiResponse<SerialNumberRecord>>(`/production/serial-numbers/${id}`, payload);
  return response.data.data;
}
