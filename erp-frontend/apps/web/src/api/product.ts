import { http, getAccessToken } from "./http";
import type { PageQuery, PageResult } from "../types/page";
import type {
  LabelPrintPayload,
  LabelPrintResult,
  LabelTemplateRecord,
  ProductCategoryPayload,
  ProductCategoryRecord,
  ProductPackageRecord,
  ProductPayload,
  ProductRecord,
  ProductStatusFlowPayload,
  ProductUpdatePayload
} from "../types/product";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

const API_BASE = import.meta.env.PROD ? "/api/v1" : "http://localhost:8080/api/v1";

function encodePathSegments(key: string): string {
  return key.split("/").map(encodeURIComponent).join("/");
}

export function buildImageUrl(key: string): string {
  const token = getAccessToken() ?? "";
  let storageKey = key;
  if (key.startsWith("http://") || key.startsWith("https://")) {
    try {
      const url = new URL(key);
      const idx = url.pathname.indexOf("/product/images/");
      if (idx >= 0) {
        storageKey = decodeURIComponent(url.pathname.substring(idx + 1));
      } else {
        return key;
      }
    } catch {
      return key;
    }
  }
  return `${API_BASE}/product/products/images/${encodePathSegments(storageKey)}?token=${token}`;
}

export async function fetchProductCategoryTree(): Promise<ProductCategoryRecord[]> {
  const response = await http.get<ApiResponse<ProductCategoryRecord[]>>("/product/categories/tree");
  return response.data.data;
}

export async function createProductCategory(payload: ProductCategoryPayload): Promise<ProductCategoryRecord> {
  const response = await http.post<ApiResponse<ProductCategoryRecord>>("/product/categories", payload);
  return response.data.data;
}

export async function updateProductCategory(id: string, payload: ProductCategoryPayload): Promise<ProductCategoryRecord> {
  const response = await http.put<ApiResponse<ProductCategoryRecord>>(`/product/categories/${id}`, payload);
  return response.data.data;
}

export async function fetchProducts(query: PageQuery & { name?: string; categoryId?: string; status?: number; isSemifinished?: boolean }): Promise<PageResult<ProductRecord>> {
  const response = await http.get<ApiResponse<PageResult<ProductRecord>>>("/product/products", { params: query });
  return response.data.data;
}

export async function fetchProductDetail(id: string): Promise<ProductRecord> {
  const response = await http.get<ApiResponse<ProductRecord>>(`/product/products/${id}`);
  return response.data.data;
}

export async function createProduct(payload: ProductPayload): Promise<ProductRecord> {
  const response = await http.post<ApiResponse<ProductRecord>>("/product/products", payload);
  return response.data.data;
}

export async function updateProduct(id: string, payload: ProductUpdatePayload): Promise<ProductRecord> {
  const response = await http.put<ApiResponse<ProductRecord>>(`/product/products/${id}`, payload);
  return response.data.data;
}

export async function updateProductStatus(id: string, status: number): Promise<void> {
  await http.put(`/product/products/${id}/status`, { status });
}

export async function changeProductStatusFlow(id: string, payload: ProductStatusFlowPayload): Promise<ProductRecord> {
  const response = await http.post<ApiResponse<ProductRecord>>(`/product/products/${id}/flow`, payload);
  return response.data.data;
}

export async function uploadProductImage(file: File): Promise<{ url: string; filename: string }> {
  const formData = new FormData();
  formData.append("file", file);
  const response = await http.post<ApiResponse<{ url: string; filename: string }>>("/product/products/upload-image", formData, {
    headers: { "Content-Type": "multipart/form-data" }
  });
  return response.data.data;
}

export async function exportProductsFile(): Promise<Blob> {
  const response = await http.get("/product/products/export", { responseType: "blob" });
  return response.data;
}

export async function importProductsFile(file: File): Promise<void> {
  const formData = new FormData();
  formData.append("file", file);
  await http.post("/product/products/import", formData, {
    headers: { "Content-Type": "multipart/form-data" }
  });
}

export async function fetchProductPackages(productId: string): Promise<ProductPackageRecord[]> {
  const response = await http.get<ApiResponse<ProductPackageRecord[]>>(`/product/products/${productId}/packages`);
  return response.data.data;
}

export async function saveProductPackages(productId: string, payload: ProductPackageRecord[]): Promise<ProductPackageRecord[]> {
  const response = await http.post<ApiResponse<ProductPackageRecord[]>>(`/product/products/${productId}/packages`, payload);
  return response.data.data;
}

export async function fetchLabelTemplates(): Promise<LabelTemplateRecord[]> {
  const response = await http.get<ApiResponse<LabelTemplateRecord[]>>("/product/label-templates");
  return response.data.data;
}

export async function saveLabelTemplate(payload: LabelTemplateRecord): Promise<LabelTemplateRecord> {
  const response = await http.post<ApiResponse<LabelTemplateRecord>>("/product/label-templates", payload);
  return response.data.data;
}

export async function previewLabelPrint(payload: LabelPrintPayload): Promise<LabelPrintResult> {
  const response = await http.post<ApiResponse<LabelPrintResult>>("/product/labels/preview", payload);
  return response.data.data;
}
