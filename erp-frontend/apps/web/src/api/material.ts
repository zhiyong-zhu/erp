import { http } from "./http";
import type { PageQuery, PageResult } from "../types/page";
import type {
  MaterialAlertRecord,
  MaterialCategoryPayload,
  MaterialCategoryRecord,
  MaterialPayload,
  MaterialReplenishmentRecord,
  MaterialRecord,
  SupplierPayload,
  SupplierQuotePayload,
  SupplierQuoteRecord,
  SupplierRecord
} from "../types/material";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

export async function fetchMaterialCategoryTree(): Promise<MaterialCategoryRecord[]> {
  const response = await http.get<ApiResponse<MaterialCategoryRecord[]>>("/material/categories/tree");
  return response.data.data;
}

export async function createMaterialCategory(payload: MaterialCategoryPayload): Promise<MaterialCategoryRecord> {
  const response = await http.post<ApiResponse<MaterialCategoryRecord>>("/material/categories", payload);
  return response.data.data;
}

export async function updateMaterialCategory(id: string, payload: MaterialCategoryPayload): Promise<MaterialCategoryRecord> {
  const response = await http.put<ApiResponse<MaterialCategoryRecord>>(`/material/categories/${id}`, payload);
  return response.data.data;
}

export async function fetchSuppliers(query: PageQuery & { name?: string }): Promise<PageResult<SupplierRecord>> {
  const response = await http.get<ApiResponse<PageResult<SupplierRecord>>>("/material/suppliers", { params: query });
  return response.data.data;
}

export async function createSupplier(payload: SupplierPayload): Promise<SupplierRecord> {
  const response = await http.post<ApiResponse<SupplierRecord>>("/material/suppliers", payload);
  return response.data.data;
}

export async function updateSupplier(id: string, payload: SupplierPayload): Promise<SupplierRecord> {
  const response = await http.put<ApiResponse<SupplierRecord>>(`/material/suppliers/${id}`, payload);
  return response.data.data;
}

export async function fetchMaterials(query: PageQuery & { name?: string; categoryId?: string; status?: number }): Promise<PageResult<MaterialRecord>> {
  const response = await http.get<ApiResponse<PageResult<MaterialRecord>>>("/material/materials", { params: query });
  return response.data.data;
}

export async function fetchMaterialAlerts(query: PageQuery & { name?: string }): Promise<PageResult<MaterialAlertRecord>> {
  const response = await http.get<ApiResponse<PageResult<MaterialAlertRecord>>>("/material/materials/alerts", { params: query });
  return response.data.data;
}

export async function fetchMaterialReplenishmentSuggestions(query: PageQuery & { name?: string }): Promise<PageResult<MaterialReplenishmentRecord>> {
  const response = await http.get<ApiResponse<PageResult<MaterialReplenishmentRecord>>>("/material/materials/replenishment", { params: query });
  return response.data.data;
}

export async function createMaterial(payload: MaterialPayload): Promise<MaterialRecord> {
  const response = await http.post<ApiResponse<MaterialRecord>>("/material/materials", payload);
  return response.data.data;
}

export async function updateMaterial(id: string, payload: MaterialPayload): Promise<MaterialRecord> {
  const response = await http.put<ApiResponse<MaterialRecord>>(`/material/materials/${id}`, payload);
  return response.data.data;
}

export async function exportMaterialsFile(): Promise<Blob> {
  const response = await http.get("/material/materials/export", { responseType: "blob" });
  return response.data;
}

export async function importMaterialsFile(file: File): Promise<void> {
  const formData = new FormData();
  formData.append("file", file);
  await http.post("/material/materials/import", formData, {
    headers: { "Content-Type": "multipart/form-data" }
  });
}

export async function fetchSupplierQuotes(query: PageQuery & { supplierId?: string; materialId?: string }): Promise<PageResult<SupplierQuoteRecord>> {
  const response = await http.get<ApiResponse<PageResult<SupplierQuoteRecord>>>("/material/quotes", { params: query });
  return response.data.data;
}

export async function createSupplierQuote(payload: SupplierQuotePayload): Promise<SupplierQuoteRecord> {
  const response = await http.post<ApiResponse<SupplierQuoteRecord>>("/material/quotes", payload);
  return response.data.data;
}

export async function updateSupplierQuote(id: string, payload: SupplierQuotePayload): Promise<SupplierQuoteRecord> {
  const response = await http.put<ApiResponse<SupplierQuoteRecord>>(`/material/quotes/${id}`, payload);
  return response.data.data;
}
