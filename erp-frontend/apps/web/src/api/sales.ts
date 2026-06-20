import { http } from "./http";
import type { PageQuery, PageResult } from "../types/page";
import type {
  CustomerPayload,
  CustomerRecord,
  EcommerceShopPayload,
  EcommerceShopRecord,
  SaleOrderCreatePayload,
  SaleOrderRecord,
  SaleOrderStatusPayload,
  SaleExceptionHandlePayload,
  SaleExceptionRecord,
  SaleReceivableStatRecord,
  SaleReturnCreatePayload,
  SaleReturnRecord,
  SaleReturnStatusPayload,
  ShippingOrderPayload,
  ShippingRecord
} from "../types/sales";

interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
  timestamp: number;
}

// ========== 客户 ==========
export async function fetchCustomers(query: PageQuery & { name?: string }): Promise<PageResult<CustomerRecord>> {
  const response = await http.get<ApiResponse<PageResult<CustomerRecord>>>("/sales/customers", { params: query });
  return response.data.data;
}

export async function createCustomer(payload: CustomerPayload): Promise<CustomerRecord> {
  const response = await http.post<ApiResponse<CustomerRecord>>("/sales/customers", payload);
  return response.data.data;
}

export async function updateCustomer(id: string, payload: CustomerPayload): Promise<CustomerRecord> {
  const response = await http.put<ApiResponse<CustomerRecord>>(`/sales/customers/${id}`, payload);
  return response.data.data;
}

// ========== 销售订单 ==========
export async function fetchSaleOrders(query: PageQuery & { status?: string; customerName?: string }): Promise<PageResult<SaleOrderRecord>> {
  const response = await http.get<ApiResponse<PageResult<SaleOrderRecord>>>("/sales/orders", { params: query });
  return response.data.data;
}

export async function fetchSaleOrderDetail(id: string): Promise<SaleOrderRecord> {
  const response = await http.get<ApiResponse<SaleOrderRecord>>(`/sales/orders/${id}`);
  return response.data.data;
}

export async function createSaleOrder(payload: SaleOrderCreatePayload): Promise<SaleOrderRecord> {
  const response = await http.post<ApiResponse<SaleOrderRecord>>("/sales/orders", payload);
  return response.data.data;
}

export async function updateSaleOrder(id: string, payload: SaleOrderCreatePayload): Promise<SaleOrderRecord> {
  const response = await http.put<ApiResponse<SaleOrderRecord>>(`/sales/orders/${id}`, payload);
  return response.data.data;
}

export async function changeSaleOrderStatus(id: string, payload: SaleOrderStatusPayload): Promise<SaleOrderRecord> {
  const response = await http.post<ApiResponse<SaleOrderRecord>>(`/sales/orders/${id}/status`, payload);
  return response.data.data;
}

export async function shipSaleOrder(id: string, payload: ShippingOrderPayload): Promise<SaleOrderRecord> {
  const response = await http.post<ApiResponse<SaleOrderRecord>>(`/sales/orders/${id}/ship`, payload);
  return response.data.data;
}

export function getSaleOrderDeliveryNoteUrl(id: string): string {
  return `http://localhost:8080/api/v1/sales/orders/${id}/delivery-note`;
}

// ========== 销售退货 ==========
export async function fetchSaleReturns(query: PageQuery): Promise<PageResult<SaleReturnRecord>> {
  const response = await http.get<ApiResponse<PageResult<SaleReturnRecord>>>("/sales/returns", { params: query });
  return response.data.data;
}

export async function fetchSaleReturnDetail(id: string): Promise<SaleReturnRecord> {
  const response = await http.get<ApiResponse<SaleReturnRecord>>(`/sales/returns/${id}`);
  return response.data.data;
}

export async function createSaleReturn(payload: SaleReturnCreatePayload): Promise<SaleReturnRecord> {
  const response = await http.post<ApiResponse<SaleReturnRecord>>("/sales/returns", payload);
  return response.data.data;
}

export async function changeSaleReturnStatus(id: string, payload: SaleReturnStatusPayload): Promise<SaleReturnRecord> {
  const response = await http.post<ApiResponse<SaleReturnRecord>>(`/sales/returns/${id}/status`, payload);
  return response.data.data;
}

// ========== 发货/物流 ==========
export async function fetchShippingOrders(query: PageQuery): Promise<PageResult<ShippingRecord>> {
  const response = await http.get<ApiResponse<PageResult<ShippingRecord>>>("/sales/shipping", { params: query });
  return response.data.data;
}

export async function fetchShippingOrderDetail(id: string): Promise<ShippingRecord> {
  const response = await http.get<ApiResponse<ShippingRecord>>(`/sales/shipping/${id}`);
  return response.data.data;
}

// ========== 应收统计 ==========
export async function fetchSaleReceivables(query: PageQuery): Promise<PageResult<SaleReceivableStatRecord>> {
  const response = await http.get<ApiResponse<PageResult<SaleReceivableStatRecord>>>("/sales/receivables", { params: query });
  return response.data.data;
}

// ========== 销售异常 ==========
export async function fetchSaleExceptions(query: PageQuery): Promise<PageResult<SaleExceptionRecord>> {
  const response = await http.get<ApiResponse<PageResult<SaleExceptionRecord>>>("/sales/exceptions", { params: query });
  return response.data.data;
}

export async function handleSaleException(id: string, payload: SaleExceptionHandlePayload): Promise<SaleExceptionRecord> {
  const response = await http.post<ApiResponse<SaleExceptionRecord>>(`/sales/exceptions/${id}/handle`, payload);
  return response.data.data;
}

// ========== 电商店铺 ==========
export async function fetchEcommerceShops(query: PageQuery): Promise<PageResult<EcommerceShopRecord>> {
  const response = await http.get<ApiResponse<PageResult<EcommerceShopRecord>>>("/sales/ecommerce/shops", { params: query });
  return response.data.data;
}

export async function createEcommerceShop(payload: EcommerceShopPayload): Promise<EcommerceShopRecord> {
  const response = await http.post<ApiResponse<EcommerceShopRecord>>("/sales/ecommerce/shops", payload);
  return response.data.data;
}

export async function syncEcommerceOrders(shopId: string): Promise<{ syncedCount: number }> {
  const response = await http.post<ApiResponse<{ syncedCount: number }>>(`/sales/ecommerce/shops/${shopId}/sync-orders`);
  return response.data.data;
}
