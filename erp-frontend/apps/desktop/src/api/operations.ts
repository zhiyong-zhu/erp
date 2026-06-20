import { request } from "./http";
import type {
  InventoryCheckPayload,
  InventoryCheckRecord,
  InventoryIssuePayload,
  InventoryIssueRecord,
  InventoryReceiptRecord,
  LabelPrintResult,
  LabelTemplateRecord,
  MaterialRecord,
  Paged,
  ProductPackageRecord,
  ProductRecord,
  ProductionBatchPayload,
  ProductionBatchRecord,
  ProductionBoxPayload,
  ProductionBoxRecord,
  ProductionReportPayload
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
  return request<Paged<ProductRecord>>(withQuery("/product/products", { pageNum: 1, pageSize: 20, name, status: 1 }));
}

export function fetchProductPackages(productId: string) {
  return request<ProductPackageRecord[]>(`/product/products/${productId}/packages`);
}

export function fetchMaterials(name?: string) {
  return request<Paged<MaterialRecord>>(withQuery("/material/materials", { pageNum: 1, pageSize: 30, name, status: 1 }));
}

export function fetchProductionBatches(batchNo?: string) {
  return request<Paged<ProductionBatchRecord>>(withQuery("/production/batches", { pageNum: 1, pageSize: 10, batchNo }));
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

export function createProductionReport(payload: ProductionReportPayload) {
  return request("/production/reports", { method: "POST", body: JSON.stringify(payload) });
}

export function fetchProductionBoxes(batchNo?: string) {
  return request<Paged<ProductionBoxRecord>>(withQuery("/production/boxes", { pageNum: 1, pageSize: 10, batchNo }));
}

export function createProductionBox(payload: ProductionBoxPayload) {
  return request<ProductionBoxRecord>("/production/boxes", { method: "POST", body: JSON.stringify(payload) });
}

export function fetchInventoryReceipts() {
  return request<Paged<InventoryReceiptRecord>>(withQuery("/inventory/receipts", { pageNum: 1, pageSize: 10 }));
}

export function fetchInventoryIssues() {
  return request<Paged<InventoryIssueRecord>>(withQuery("/inventory/issues", { pageNum: 1, pageSize: 10 }));
}

export function createInventoryIssue(payload: InventoryIssuePayload) {
  return request<InventoryIssueRecord>("/inventory/issues", { method: "POST", body: JSON.stringify(payload) });
}

export function fetchInventoryChecks() {
  return request<Paged<InventoryCheckRecord>>(withQuery("/inventory/checks", { pageNum: 1, pageSize: 10 }));
}

export function createInventoryCheck(payload: InventoryCheckPayload) {
  return request<InventoryCheckRecord>("/inventory/checks", { method: "POST", body: JSON.stringify(payload) });
}

export function fetchLabelTemplates() {
  return request<LabelTemplateRecord[]>("/product/label-templates");
}

export function previewLabelPrint(payload: { items: Array<{ skuId: string; packageLevel: number; labelTemplateId: string; quantity: number }>; printMode: string; printerId?: string }) {
  return request<LabelPrintResult>("/product/labels/preview", { method: "POST", body: JSON.stringify(payload) });
}
