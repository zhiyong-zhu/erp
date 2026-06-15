import type { BasePayload, BaseRecord } from "@erp/shared";

// ========== 客户 ==========
export interface CustomerRecord extends BaseRecord {
  id: string;
  code: string;
  name: string;
  shortName?: string | null;
  customerType?: number | null;
  contactPerson?: string | null;
  phone?: string | null;
  email?: string | null;
  address?: string | null;
  creditLimit?: number | null;
  paymentTerms?: number | null;
  salesRepId?: string | null;
  taxNumber?: string | null;
  status: number;
  remark?: string | null;
}

export interface CustomerPayload extends BasePayload {
  code: string;
  name: string;
  shortName?: string;
  customerType?: number;
  contactPerson?: string;
  phone?: string;
  email?: string;
  address?: string;
  creditLimit?: number;
  paymentTerms?: number;
  salesRepId?: string;
  taxNumber?: string;
  status?: number;
  remark?: string;
}

// ========== 销售订单 ==========
export interface SaleOrderItemRecord {
  id: string;
  saleOrderId: string;
  skuId?: string | null;
  skuCode?: string | null;
  productName?: string | null;
  unit?: string | null;
  quantity: number;
  shippedQuantity?: number | null;
  unitPrice?: number | null;
  amount?: number | null;
  remark?: string | null;
}

export interface SaleOrderRecord extends BaseRecord {
  id: string;
  orderNo: string;
  customerId: string;
  customerName: string;
  orderSource: string;
  platformOrderNo?: string | null;
  platformData?: string | null;
  status: string;
  totalAmount?: number | null;
  discountAmount?: number | null;
  freightAmount?: number | null;
  payableAmount?: number | null;
  paidAmount?: number | null;
  paymentStatus?: string | null;
  shippingAddress?: string | null;
  remark?: string | null;
  orderedAt?: string | null;
  paidAt?: string | null;
  shippedAt?: string | null;
  completedAt?: string | null;
  items: SaleOrderItemRecord[];
  shippingOrders?: ShippingRecord[];
}

export interface SaleOrderCreatePayload extends BasePayload {
  customerId: string;
  orderSource: string;
  platformOrderNo?: string;
  shippingAddress?: string;
  remark?: string;
  items: SaleOrderItemPayload[];
}

export interface SaleOrderItemPayload {
  skuId: string;
  skuCode?: string;
  productName?: string;
  unit?: string;
  quantity: number;
  unitPrice?: number;
  amount?: number;
  remark?: string;
}

export interface SaleOrderStatusPayload {
  action: "confirm" | "cancel" | "complete" | "requestReturn";
  remark?: string;
}

export interface ShippingOrderPayload {
  carrierCode?: string;
  carrierName?: string;
  trackingNumber?: string;
  remark?: string;
}

// ========== 销售退货 ==========
export interface SaleReturnItemRecord {
  id: string;
  saleReturnId: string;
  saleOrderItemId?: string | null;
  skuId?: string | null;
  skuCode?: string | null;
  productName?: string | null;
  quantity: number;
  unitPrice?: number | null;
  returnAmount?: number | null;
  reason?: string | null;
}

export interface SaleReturnRecord extends BaseRecord {
  id: string;
  returnNo: string;
  saleOrderId: string;
  saleOrderNo: string;
  customerId: string;
  customerName: string;
  status: string;
  totalAmount?: number | null;
  reason?: string | null;
  remark?: string | null;
  items: SaleReturnItemRecord[];
}

export interface SaleReturnCreatePayload extends BasePayload {
  saleOrderId: string;
  items: Array<{
    saleOrderItemId: string;
    quantity: number;
    reason?: string;
  }>;
  remark?: string;
}

export interface SaleReturnStatusPayload {
  action: "approve" | "reject" | "inspect" | "refund" | "complete";
  remark?: string;
}

// ========== 发货/物流 ==========
export interface ShippingRecord extends BaseRecord {
  id: string;
  saleOrderId: string;
  carrierCode?: string | null;
  carrierName?: string | null;
  trackingNumber?: string | null;
  status: string;
  shippedAt?: string | null;
  receivedAt?: string | null;
  remark?: string | null;
}

// ========== 应收统计 ==========
export interface SaleReceivableStatRecord {
  customerId: string;
  customerName: string;
  orderAmount?: number | null;
  returnAmount?: number | null;
  netReceivableAmount?: number | null;
  orderCount?: number | null;
  returnCount?: number | null;
}

// ========== 电商店铺 ==========
export interface EcommerceShopRecord extends BaseRecord {
  id: string;
  platform: string;
  shopName: string;
  shopIdOnPlatform: string;
  accessToken?: string | null;
  tokenExpiresAt?: string | null;
  syncConfig?: string | null;
  status: number;
}

export interface EcommerceShopPayload extends BasePayload {
  platform: string;
  shopName: string;
  shopIdOnPlatform: string;
  accessToken?: string;
  syncConfig?: string;
  status?: number;
}

export interface EcommerceSkuMappingRecord extends BaseRecord {
  id: string;
  shopId: string;
  platformSkuId: string;
  platformProductName?: string | null;
  skuId?: string | null;
}
