import { request } from "./http";
import type {
  InventoryCheckRecord,
  InventoryIssueRecord,
  InventoryReceiptRecord,
  MaterialActionPayload,
  MaterialRecord,
  PageResult,
  ProductPackageRecord,
  ProductRecord,
  ProductionBatchPayload,
  ProductionBatchRecord,
  ProductionBoxRecord
} from "../types/operations";

type QueryValue = string | number | undefined | null;

function withQuery(path: string, query: Record<string, QueryValue>) {
  const params = new URLSearchParams();
  Object.entries(query).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      params.set(key, String(value));
    }
  });
  const queryString = params.toString();
  return queryString ? `${path}?${queryString}` : path;
}

export function fetchProducts(name?: string) {
  return request<PageResult<ProductRecord>>(withQuery("/product/products", { pageNum: 1, pageSize: 10, name, status: 1 }));
}

export function fetchProductPackages(productId: string) {
  return request<ProductPackageRecord[]>(`/product/products/${productId}/packages`);
}

export function fetchMaterials(name?: string) {
  return request<PageResult<MaterialRecord>>(withQuery("/material/materials", { pageNum: 1, pageSize: 10, name, status: 1 }));
}

export function fetchProductionBatches(batchNo?: string) {
  return request<PageResult<ProductionBatchRecord>>(withQuery("/production/batches", { pageNum: 1, pageSize: 10, batchNo }));
}

export function createProductionBatch(payload: ProductionBatchPayload) {
  return request<ProductionBatchRecord>("/production/batches", { method: "POST", body: JSON.stringify(payload) });
}

export function startProductionBatch(id: string) {
  return request<ProductionBatchRecord>(`/production/batches/${id}/start`, { method: "POST" });
}

export function receiveProductionBatch(id: string) {
  return request<ProductionBatchRecord>(`/production/batches/${id}/receipt`, { method: "POST" });
}

export function createProductionReport(payload: { batchId: string; reportQuantity: number; goodQuantity?: number; defectQuantity?: number; operatorName?: string; remark?: string }) {
  return request("/production/reports", { method: "POST", body: JSON.stringify(payload) });
}

export function createProductionBox(payload: { batchId: string; packageId: string; quantity?: number; remark?: string }) {
  return request<ProductionBoxRecord>("/production/boxes", { method: "POST", body: JSON.stringify(payload) });
}

export function fetchProductionBoxes(batchNo?: string) {
  return request<PageResult<ProductionBoxRecord>>(withQuery("/production/boxes", { pageNum: 1, pageSize: 10, batchNo }));
}

export function fetchInventoryReceipts() {
  return request<PageResult<InventoryReceiptRecord>>(withQuery("/inventory/receipts", { pageNum: 1, pageSize: 5 }));
}

export function fetchInventoryIssues() {
  return request<PageResult<InventoryIssueRecord>>(withQuery("/inventory/issues", { pageNum: 1, pageSize: 5 }));
}

export function createInventoryIssue(payload: MaterialActionPayload & { sourceOrderNo?: string }) {
  return request<InventoryIssueRecord>("/inventory/issues", {
    method: "POST",
    body: JSON.stringify({
      issueType: "MANUAL_OUT",
      sourceOrderNo: payload.sourceOrderNo,
      remark: payload.remark,
      items: [{ materialId: payload.materialId, quantity: payload.quantity, remark: payload.remark }]
    })
  });
}

export function fetchInventoryChecks() {
  return request<PageResult<InventoryCheckRecord>>(withQuery("/inventory/checks", { pageNum: 1, pageSize: 5 }));
}

export function createInventoryCheck(payload: MaterialActionPayload) {
  return request<InventoryCheckRecord>("/inventory/checks", {
    method: "POST",
    body: JSON.stringify({
      checkType: "FULL",
      remark: payload.remark,
      items: [{ materialId: payload.materialId, actualQuantity: payload.quantity, remark: payload.remark }]
    })
  });
}
