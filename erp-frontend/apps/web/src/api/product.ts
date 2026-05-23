import { http } from "./http";
import type { PageQuery, PageResult } from "../types/page";
import type {
  LabelTemplateRecord,
  ProductCategoryPayload,
  ProductCategoryRecord,
  ProductPackageRecord,
  ProductPayload,
  ProductRecord,
  ProductUpdatePayload
} from "../types/product";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
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

export async function fetchProducts(query: PageQuery & { name?: string; categoryId?: string; status?: number }): Promise<PageResult<ProductRecord>> {
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
