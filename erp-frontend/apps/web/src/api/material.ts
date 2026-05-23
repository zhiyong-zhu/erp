import { http } from "./http";
import type { PageQuery, PageResult } from "../types/page";
import type {
  MaterialCategoryPayload,
  MaterialCategoryRecord,
  MaterialPayload,
  MaterialRecord,
  SupplierPayload,
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

export async function createMaterial(payload: MaterialPayload): Promise<MaterialRecord> {
  const response = await http.post<ApiResponse<MaterialRecord>>("/material/materials", payload);
  return response.data.data;
}

export async function updateMaterial(id: string, payload: MaterialPayload): Promise<MaterialRecord> {
  const response = await http.put<ApiResponse<MaterialRecord>>(`/material/materials/${id}`, payload);
  return response.data.data;
}
