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
  /** 客户等级：A=核心客户 B=潜力客户 C=普通客户 */
  grade?: string | null;
  paymentTerms?: number | null;
  salesRepId?: string | null;
  taxNumber?: string | null;
  status: number;
  remark?: string | null;
}

export interface CustomerPayload extends BasePayload {
  code?: string;
  name: string;
  shortName?: string;
  customerType?: number;
  contactPerson?: string;
  phone?: string;
  email?: string;
  address?: string;
  /** 客户等级：A=核心客户 B=潜力客户 C=普通客户 */
  grade?: string;
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
  /** 是否存在未处理（OPEN）销售异常，用于确认门禁 */
  hasOpenException?: boolean;
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
  /** 编辑时回传已有明细主键，用于服务端 diff 判断是否保留明细行 */
  id?: string;
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
  items?: Array<{
    saleOrderItemId: string;
    quantity: number;
    serialNos?: string[];
  }>;
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
  items?: ShippingItemRecord[];
}

export interface ShippingItemRecord {
  id: string;
  shippingOrderId: string;
  saleOrderItemId: string;
  skuId?: string | null;
  skuCode?: string | null;
  productName?: string | null;
  quantity: number;
  serialNos?: string[];
  createdAt?: string | null;
}

// ========== 应收统计 ==========
export interface SaleReceivableStatRecord {
  customerId: string;
  customerName: string;
  orderAmount?: number | null;
  returnAmount?: number | null;
  netReceivableAmount?: number | null;
  receivedAmount?: number | null;
  unreceivedAmount?: number | null;
  orderCount?: number | null;
  returnCount?: number | null;
}

export interface SalePaymentPayload {
  receivedAmount: number;
  paymentMethod?: string;
  remark?: string;
}

export interface SalePaymentRecord {
  id: string;
  paymentNo: string;
  saleOrderId: string;
  saleOrderNo: string;
  customerName?: string | null;
  receivedAmount: number;
  paymentMethod?: string | null;
  paymentTime?: string | null;
  remark?: string | null;
  createdAt?: string | null;
}

// ========== 销售异常 ==========
export interface SaleExceptionRecord {
  id: string;
  exceptionNo: string;
  saleOrderId?: string | null;
  saleOrderItemId?: string | null;
  saleReturnId?: string | null;
  saleReturnItemId?: string | null;
  customerName?: string | null;
  skuCode?: string | null;
  productName?: string | null;
  exceptionType: string;
  status: string;
  description?: string | null;
  resolution?: string | null;
  createdAt?: string | null;
  handledAt?: string | null;
}

export interface SaleExceptionHandlePayload {
  action: "resolve" | "close";
  resolution?: string;
}

// ========== 销售报表 ==========
export interface SaleReportSummary {
  orderAmount?: number | null;
  returnAmount?: number | null;
  netSalesAmount?: number | null;
  orderCount?: number | null;
  returnCount?: number | null;
  customerCount?: number | null;
  topCustomers: SaleCustomerRank[];
  topProducts: SaleProductRank[];
}

export interface SaleCustomerRank {
  customerName?: string | null;
  orderAmount?: number | null;
  returnAmount?: number | null;
  netSalesAmount?: number | null;
  orderCount?: number | null;
}

export interface SaleProductRank {
  skuCode?: string | null;
  productName?: string | null;
  quantity?: number | null;
  salesAmount?: number | null;
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
