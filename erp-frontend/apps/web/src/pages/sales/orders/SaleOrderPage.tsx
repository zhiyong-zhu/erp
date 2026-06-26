import { CheckOutlined, EditOutlined, SendOutlined, PrinterOutlined, RollbackOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormList, ProFormSelect, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { SALES_PERMISSIONS } from "@erp/shared";
import { CreateForm } from "../../../components/CreateForm";
import { App, Button, Drawer, Input, InputNumber, Modal, Select, Space, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import {
  changeSaleOrderStatus,
  createSaleOrder,
  fetchCustomers,
  fetchSaleOrderDetail,
  fetchSaleOrders,
  shipSaleOrder,
  updateSaleOrder
} from "../../../api/sales";
import { fetchProducts } from "../../../api/product";
import { hasPermission } from "../../../store/auth";
import type {
  SaleOrderCreatePayload,
  SaleOrderItemPayload,
  SaleOrderItemRecord,
  SaleOrderRecord,
  SaleOrderStatusPayload,
  ShippingOrderPayload
} from "../../../types/sales";
import type { ProductRecord } from "../../../types/product";

const { Title, Text } = Typography;

const STATUS_CONFIG: Record<string, { label: string; color: string }> = {
  PENDING_CONFIRM: { label: "待确认", color: "orange" },
  CONFIRMED: { label: "已确认", color: "blue" },
  PENDING_SHIP: { label: "待发货", color: "cyan" },
  PARTIAL_SHIPPED: { label: "部分发货", color: "geekblue" },
  SHIPPED: { label: "已发货", color: "purple" },
  COMPLETED: { label: "已完成", color: "green" },
  CANCELLED: { label: "已取消", color: "red" },
  RETURN_REQUEST: { label: "退货申请", color: "volcano" },
  RETURNING: { label: "退货中", color: "volcano" },
  RETURNED: { label: "已退货", color: "default" }
};

const ORDER_SOURCE_MAP: Record<string, string> = {
  MANUAL: "手工录入",
  TAOBAO: "淘宝/天猫",
  JD: "京东",
  PDD: "拼多多",
  DOUYIN: "抖音",
  ALIBABA_1688: "1688"
};

type SaleOrderAction = SaleOrderStatusPayload["action"];

const ACTION_LABELS: Record<SaleOrderAction, string> = {
  confirm: "确认",
  cancel: "取消",
  complete: "完成",
  requestReturn: "申请退货"
};

function canEditOrder(status: string) {
  return status === "PENDING_CONFIRM";
}

function canConfirmOrder(status: string) {
  return status === "PENDING_CONFIRM";
}

function canCancelOrder(status: string) {
  return status === "PENDING_CONFIRM" || status === "CONFIRMED" || status === "PENDING_SHIP";
}

function canShipOrder(status: string) {
  return status === "CONFIRMED" || status === "PENDING_SHIP" || status === "PARTIAL_SHIPPED";
}

function canCompleteOrder(status: string) {
  return status === "SHIPPED";
}

function canRequestReturn(status: string) {
  return status === "SHIPPED" || status === "COMPLETED";
}

function canPrintDeliveryNote(status: string) {
  return status === "SHIPPED" || status === "COMPLETED" || status === "RETURN_REQUEST" || status === "RETURNING" || status === "RETURNED";
}

export function SaleOrderPage() {
  const [loading, setLoading] = useState(false);
  const [orders, setOrders] = useState<SaleOrderRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [viewingOrder, setViewingOrder] = useState<SaleOrderRecord | null>(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [editingOrder, setEditingOrder] = useState<SaleOrderRecord | null>(null);
  const [shippingOrder, setShippingOrder] = useState<SaleOrderRecord | null>(null);
  const [pdfPreviewUrl, setPdfPreviewUrl] = useState<string | null>(null);
  const { message } = App.useApp();
  const canCreate = hasPermission(SALES_PERMISSIONS.ORDER_CREATE);
  const canUpdate = hasPermission(SALES_PERMISSIONS.ORDER_UPDATE);
  const canShip = hasPermission(SALES_PERMISSIONS.SHIPPING_UPDATE);

  useEffect(() => {
    void loadOrders();
  }, []);

  async function loadOrders(nextPageNum = pageNum, nextPageSize = pageSize, customerName = keyword) {
    setLoading(true);
    try {
      const data = await fetchSaleOrders({ pageNum: nextPageNum, pageSize: nextPageSize, customerName });
      setOrders(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载销售订单失败");
    } finally {
      setLoading(false);
    }
  }

  async function openDetail(record: SaleOrderRecord) {
    try {
      setViewingOrder(await fetchSaleOrderDetail(record.id));
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载详情失败");
    }
  }

  async function openEdit(record: SaleOrderRecord) {
    try {
      setEditingOrder(await fetchSaleOrderDetail(record.id));
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载详情失败");
    }
  }

  async function openShip(record: SaleOrderRecord) {
    try {
      setShippingOrder(await fetchSaleOrderDetail(record.id));
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载详情失败");
    }
  }

  async function handleCreate(values: SaleOrderCreatePayload) {
    try {
      await createSaleOrder(values);
      message.success("销售订单创建成功");
      setCreateOpen(false);
      await loadOrders(1, pageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "创建失败");
      return false;
    }
  }

  async function handleUpdate(values: SaleOrderCreatePayload) {
    if (!editingOrder) return false;
    try {
      await updateSaleOrder(editingOrder.id, values);
      message.success("订单更新成功");
      setEditingOrder(null);
      await loadOrders();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "更新失败");
      return false;
    }
  }

  async function handleStatus(record: SaleOrderRecord, action: SaleOrderAction) {
    try {
      await changeSaleOrderStatus(record.id, { action });
      message.success("状态已更新");
      await loadOrders();
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "状态更新失败");
    }
  }

  function confirmStatusChange(record: SaleOrderRecord, action: SaleOrderAction) {
    Modal.confirm({
      title: `确认${ACTION_LABELS[action]}订单？`,
      content: `订单号：${record.orderNo}`,
      okText: "确认",
      cancelText: "取消",
      onOk: () => handleStatus(record, action)
    });
  }

  async function handleShip(values: ShippingOrderPayload) {
    if (!shippingOrder) return false;
    try {
      await shipSaleOrder(shippingOrder.id, values);
      message.success("发货单已提交，待复核出库");
      setShippingOrder(null);
      await loadOrders();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "发货失败");
      return false;
    }
  }

  function handlePrintDeliveryNote(orderId: string) {
    const token = localStorage.getItem("erp.accessToken");
    const url = `http://localhost:8080/api/v1/sales/orders/${orderId}/delivery-note${token ? `?token=${token}` : ""}`;
    setPdfPreviewUrl(url);
  }

  const columns: ColumnsType<SaleOrderRecord> = [
    { title: "订单号", dataIndex: "orderNo", key: "orderNo", width: 170 },
    { title: "客户", dataIndex: "customerName", key: "customerName", width: 140 },
    {
      title: "来源", dataIndex: "orderSource", key: "orderSource", width: 100,
      render: (v: string) => ORDER_SOURCE_MAP[v] ?? v
    },
    {
      title: "状态", dataIndex: "status", key: "status", width: 110,
      render: (v: string) => { const c = STATUS_CONFIG[v]; return c ? <Tag color={c.color}>{c.label}</Tag> : v; }
    },
    {
      title: "总金额", dataIndex: "totalAmount", key: "totalAmount", width: 110,
      render: (v: number) => v != null ? `¥${Number(v).toLocaleString()}` : "-"
    },
    {
      title: "应收", dataIndex: "payableAmount", key: "payableAmount", width: 110,
      render: (v: number) => v != null ? `¥${Number(v).toLocaleString()}` : "-"
    },
    { title: "下单时间", dataIndex: "orderedAt", key: "orderedAt", width: 170 },
    {
      title: "操作", key: "actions", width: 380,
      render: (_, record) => (
        <Space size="small" wrap>
          <Button type="link" onClick={() => void openDetail(record)}>详情</Button>
          {canEditOrder(record.status) && (
            <Button type="link" icon={<EditOutlined />} disabled={!canUpdate} onClick={() => void openEdit(record)}>编辑</Button>
          )}
          {canConfirmOrder(record.status) && (
            <Button type="link" icon={<CheckOutlined />} disabled={!canUpdate} onClick={() => confirmStatusChange(record, "confirm")}>确认</Button>
          )}
          {canCancelOrder(record.status) && (
            <Button type="link" disabled={!canUpdate} onClick={() => confirmStatusChange(record, "cancel")}>取消</Button>
          )}
          {canShipOrder(record.status) && (
            <Button type="link" icon={<SendOutlined />} disabled={!canShip} onClick={() => void openShip(record)}>发货</Button>
          )}
          {canCompleteOrder(record.status) && (
            <>
              <Button type="link" disabled={!canUpdate} onClick={() => confirmStatusChange(record, "complete")}>完成</Button>
            </>
          )}
          {canRequestReturn(record.status) && (
            <Button type="link" icon={<RollbackOutlined />} disabled={!canUpdate} onClick={() => confirmStatusChange(record, "requestReturn")}>申请退货</Button>
          )}
          {canPrintDeliveryNote(record.status) && (
            <Button type="link" icon={<PrinterOutlined />} onClick={() => handlePrintDeliveryNote(record.id)}>出库单</Button>
          )}
        </Space>
      )
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>销售管理 / 销售订单</Title>
          <Text type="secondary">管理销售订单的创建、确认、发货和完成流程。</Text>
        </div>
        <Space>
          <Input.Search
            allowClear
            placeholder="搜索客户名称"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onSearch={(value) => { setKeyword(value); void loadOrders(1, pageSize, value); }}
            style={{ width: 240 }}
          />
          <Button type="primary" disabled={!canCreate} onClick={() => setCreateOpen(true)}>新建订单</Button>
        </Space>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={orders}
        loading={loading}
        pagination={{
          current: pageNum, pageSize, total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadOrders(nextPageNum, nextPageSize)
        }}
      />

      {/* 详情 Drawer */}
      <Drawer title="销售订单详情" open={!!viewingOrder} width={920} onClose={() => setViewingOrder(null)} destroyOnClose>
        {viewingOrder ? (
          <>
            <Space direction="vertical" size={8} style={{ width: "100%" }}>
              <Text strong>订单号：{viewingOrder.orderNo}</Text>
              <Text>客户：{viewingOrder.customerName}</Text>
              <Text>来源：{ORDER_SOURCE_MAP[viewingOrder.orderSource] ?? viewingOrder.orderSource}</Text>
              <Text>状态：{STATUS_CONFIG[viewingOrder.status]?.label ?? viewingOrder.status}</Text>
              <Text>总金额：¥{Number(viewingOrder.totalAmount ?? 0).toLocaleString()}</Text>
              <Text>优惠：¥{Number(viewingOrder.discountAmount ?? 0).toLocaleString()}</Text>
              <Text>运费：¥{Number(viewingOrder.freightAmount ?? 0).toLocaleString()}</Text>
              <Text>应付：¥{Number(viewingOrder.payableAmount ?? 0).toLocaleString()}</Text>
              {viewingOrder.remark && <Text>备注：{viewingOrder.remark}</Text>}
            </Space>
            <Table
              rowKey="id"
              style={{ marginTop: 16 }}
              pagination={false}
              size="small"
              columns={[
                { title: "SKU编码", dataIndex: "skuCode", key: "skuCode", width: 130 },
                { title: "品名", dataIndex: "productName", key: "productName", width: 160 },
                { title: "单位", dataIndex: "unit", key: "unit", width: 60 },
                { title: "数量", dataIndex: "quantity", key: "quantity", width: 80 },
                { title: "已发货", dataIndex: "shippedQuantity", key: "shippedQuantity", width: 80 },
                { title: "单价", dataIndex: "unitPrice", key: "unitPrice", width: 90 },
                { title: "金额", dataIndex: "amount", key: "amount", width: 100 }
              ]}
              dataSource={viewingOrder.items}
            />
            {viewingOrder.shippingOrders && viewingOrder.shippingOrders.length > 0 && (
              <>
                <Title level={5} style={{ marginTop: 16 }}>物流信息</Title>
                {viewingOrder.shippingOrders.map((s) => (
                  <Space key={s.id} direction="vertical" size={4}>
                    <Text>承运商：{s.carrierName ?? "-"}</Text>
                    <Text>运单号：{s.trackingNumber ?? "-"}</Text>
                    <Text>发货时间：{s.shippedAt ?? "-"}</Text>
                  </Space>
                ))}
              </>
            )}
          </>
        ) : null}
      </Drawer>

      {/* 新建/编辑订单 */}
      <OrderFormModal
        title={editingOrder ? "编辑销售订单" : "新建销售订单"}
        open={!!editingOrder || createOpen}
        initialValues={editingOrder ? {
          customerId: editingOrder.customerId,
          orderSource: editingOrder.orderSource,
          shippingAddress: editingOrder.shippingAddress ?? "",
          discountAmount: editingOrder.discountAmount ?? undefined,
          freightAmount: editingOrder.freightAmount ?? undefined,
          remark: editingOrder.remark ?? "",
          items: editingOrder.items.map((i) => ({
            skuId: i.skuId ?? "",
            skuCode: i.skuCode ?? "",
            productName: i.productName ?? "",
            unit: i.unit ?? "",
            quantity: Number(i.quantity ?? 0),
            unitPrice: i.unitPrice ? Number(i.unitPrice) : undefined,
            amount: i.amount ? Number(i.amount) : undefined
          }))
        } : undefined}
        onCancel={() => { setCreateOpen(false); setEditingOrder(null); }}
        onFinish={editingOrder ? handleUpdate : handleCreate}
      />

      <ShippingFormModal
        order={shippingOrder}
        onCancel={() => setShippingOrder(null)}
        onFinish={handleShip}
      />

      {/* PDF 预览 */}
      <Modal
        title="出库单预览"
        open={!!pdfPreviewUrl}
        width={800}
        onCancel={() => setPdfPreviewUrl(null)}
        footer={[
          <Button key="close" onClick={() => setPdfPreviewUrl(null)}>关闭</Button>,
          <Button key="print" type="primary" onClick={() => window.print()}>打印</Button>,
          <Button key="download" onClick={() => {
            if (pdfPreviewUrl) {
              const a = document.createElement("a");
              a.href = pdfPreviewUrl;
              a.download = "delivery-note.pdf";
              a.click();
            }
          }}>下载</Button>
        ]}
      >
        {pdfPreviewUrl && (
          <iframe src={pdfPreviewUrl} style={{ width: "100%", height: "600px", border: "none" }} title="出库单" />
        )}
      </Modal>
    </section>
  );
}

function remainingQuantity(item: SaleOrderItemRecord) {
  return Math.max(Number(item.quantity ?? 0) - Number(item.shippedQuantity ?? 0), 0);
}

function ShippingFormModal({
  order, onCancel, onFinish
}: {
  order: SaleOrderRecord | null;
  onCancel: () => void;
  onFinish: (values: ShippingOrderPayload) => Promise<boolean>;
}) {
  const [carrierName, setCarrierName] = useState("");
  const [trackingNumber, setTrackingNumber] = useState("");
  const [remark, setRemark] = useState("");
  const [lineQuantities, setLineQuantities] = useState<Record<string, number>>({});
  const [lineSerialNos, setLineSerialNos] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!order) {
      setCarrierName("");
      setTrackingNumber("");
      setRemark("");
      setLineQuantities({});
      setLineSerialNos({});
      return;
    }
    setLineQuantities(Object.fromEntries(
      order.items
        .filter((item) => remainingQuantity(item) > 0)
        .map((item) => [item.id, remainingQuantity(item)])
    ));
  }, [order]);

  const shippableItems = order?.items.filter((item) => remainingQuantity(item) > 0) ?? [];
  const selectedItems = shippableItems
    .map((item) => ({
      saleOrderItemId: item.id,
      quantity: Number(lineQuantities[item.id] ?? 0),
      serialNos: parseSerialNos(lineSerialNos[item.id])
    }))
    .filter((item) => item.quantity > 0);

  async function handleOk() {
    if (!order) return;
    setSubmitting(true);
    try {
      const ok = await onFinish({
        carrierName,
        trackingNumber,
        remark,
        items: selectedItems
      });
      if (ok) {
        setCarrierName("");
        setTrackingNumber("");
        setRemark("");
        setLineQuantities({});
        setLineSerialNos({});
      }
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <Modal
      title="发货"
      open={!!order}
      width={980}
      destroyOnClose
      onCancel={onCancel}
      confirmLoading={submitting}
      okButtonProps={{ disabled: selectedItems.length === 0 }}
      okText="提交发货单"
      cancelText="取消"
      onOk={() => void handleOk()}
    >
      <Space direction="vertical" size={16} style={{ width: "100%" }}>
        <Space wrap style={{ width: "100%" }}>
          <Input
            value={carrierName}
            onChange={(event) => setCarrierName(event.target.value)}
            placeholder="承运商"
            style={{ width: 220 }}
          />
          <Input
            value={trackingNumber}
            onChange={(event) => setTrackingNumber(event.target.value)}
            placeholder="运单号"
            style={{ width: 260 }}
          />
        </Space>
        <Table<SaleOrderItemRecord>
          rowKey="id"
          size="small"
          pagination={false}
          dataSource={shippableItems}
          columns={[
            { title: "SKU编码", dataIndex: "skuCode", key: "skuCode", width: 140 },
            { title: "品名", dataIndex: "productName", key: "productName", width: 180 },
            {
              title: "订单数量", dataIndex: "quantity", key: "quantity", width: 90,
              render: (value: number) => Number(value ?? 0)
            },
            {
              title: "已发", dataIndex: "shippedQuantity", key: "shippedQuantity", width: 80,
              render: (value: number) => Number(value ?? 0)
            },
            {
              title: "剩余", key: "remaining", width: 80,
              render: (_, record) => remainingQuantity(record)
            },
            {
              title: "本次发货", key: "shipQuantity", width: 150,
              render: (_, record) => {
                const remaining = remainingQuantity(record);
                return (
                  <InputNumber
                    min={0}
                    max={remaining}
                    precision={2}
                    value={lineQuantities[record.id] ?? 0}
                    style={{ width: 120 }}
                    onChange={(value) => {
                      const nextValue = Math.min(Math.max(Number(value ?? 0), 0), remaining);
                      setLineQuantities((prev) => ({ ...prev, [record.id]: nextValue }));
                    }}
                  />
                );
              }
            },
            {
              title: "本次序列号", key: "serialNos", width: 260,
              render: (_, record) => (
                <Input.TextArea
                  value={lineSerialNos[record.id] ?? ""}
                  placeholder="可选；多个序列号用换行或逗号分隔"
                  autoSize={{ minRows: 1, maxRows: 3 }}
                  onChange={(event) => {
                    const value = event.target.value;
                    setLineSerialNos((prev) => ({ ...prev, [record.id]: value }));
                  }}
                />
              )
            }
          ]}
        />
        <Input.TextArea
          value={remark}
          onChange={(event) => setRemark(event.target.value)}
          placeholder="备注"
          autoSize={{ minRows: 2, maxRows: 4 }}
        />
      </Space>
    </Modal>
  );
}

function parseSerialNos(value?: string) {
  if (!value?.trim()) {
    return undefined;
  }
  const serialNos = value
    .split(/[\n,，]/)
    .map((item) => item.trim())
    .filter(Boolean);
  return serialNos.length > 0 ? serialNos : undefined;
}

function OrderFormModal({
  title, open, initialValues, onCancel, onFinish
}: {
  title: string;
  open: boolean;
  initialValues?: any;
  onCancel: () => void;
  onFinish: (values: SaleOrderCreatePayload) => Promise<boolean>;
}) {
  const [products, setProducts] = useState<ProductRecord[]>([]);
  const [productsLoading, setProductsLoading] = useState(false);
  // Map skuId -> resolved SKU info (filled when user selects a SKU in any row)
  const [skuInfoMap, setSkuInfoMap] = useState<Record<string, {
    skuCode: string; productName: string; unit: string; unitPrice?: number
  }>>({});

  // Load products for SKU selection
  useEffect(() => {
    if (open) {
      setProductsLoading(true);
      fetchProducts({ pageNum: 1, pageSize: 200, status: 1 })
        .then((data) => setProducts(data.records))
        .catch(() => setProducts([]))
        .finally(() => setProductsLoading(false));
    } else {
      setSkuInfoMap({});
    }
  }, [open]);

  // Build flat list: all SKUs with parent product info
  const allSkus = products.flatMap((p) =>
    (p.skus ?? []).map((sku) => ({
      skuId: sku.id ?? "",
      skuCode: sku.skuCode,
      productName: p.name,
      unit: p.unit,
      unitPrice: sku.price ?? undefined,
      label: `${p.name} - ${sku.skuCode}${sku.attributes ? ` (${sku.attributes})` : ""}`
    }))
  );

  return (
    <ModalForm<SaleOrderCreatePayload>
      title={title}
      open={open}
      width={1080}
      grid
      rowProps={{ gutter: 16 }}
      initialValues={initialValues ?? { orderSource: "MANUAL" }}
      modalProps={{ destroyOnClose: true, onCancel }}
      onFinish={async (values) => {
        // Enrich items with SKU info from the skuInfoMap
        const enrichedItems = values.items?.map((item) => {
          const info = skuInfoMap[item.skuId || ""];
          return {
            ...item,
            skuCode: info?.skuCode || item.skuCode,
            productName: info?.productName || item.productName,
            unit: info?.unit || item.unit,
            unitPrice: info?.unitPrice ?? item.unitPrice
          };
        });
        return onFinish({ ...values, items: enrichedItems as SaleOrderItemPayload[] });
      }}
    >
      <ProFormSelect
        name="customerId" label="客户" rules={[{ required: true, message: "请选择客户" }]}
        colProps={{ xs: 24, md: 8 }}
        request={async () => {
          const data = await fetchCustomers({ pageNum: 1, pageSize: 200 });
          return data.records.map((c) => ({ label: `${c.name} (${c.code})`, value: c.id }));
        }}
      />
      <ProFormSelect
        name="orderSource" label="订单来源" rules={[{ required: true }]}
        colProps={{ xs: 24, md: 8 }}
        options={[
          { label: "手工录入", value: "MANUAL" },
          { label: "淘宝/天猫", value: "TAOBAO" },
          { label: "京东", value: "JD" },
          { label: "1688", value: "ALIBABA_1688" },
          { label: "拼多多", value: "PDD" },
          { label: "抖音", value: "DOUYIN" }
        ]}
      />
      <ProFormText name="shippingAddress" label="收货地址" colProps={{ xs: 24, md: 8 }} />
      <ProFormDigit name="discountAmount" label="优惠金额" min={0} fieldProps={{ precision: 2 }} colProps={{ xs: 24, md: 4 }} />
      <ProFormDigit name="freightAmount" label="运费" min={0} fieldProps={{ precision: 2 }} colProps={{ xs: 24, md: 4 }} />
      <ProFormTextArea name="remark" label="备注" fieldProps={{ autoSize: { minRows: 2, maxRows: 4 } }} colProps={{ span: 24 }} />
      <ProFormList
        name="items" label="订单明细"
        creatorButtonProps={{ creatorButtonText: "添加商品" }}
        itemRender={(dom, index) => dom.listDom}
        colProps={{ span: 24 }}
      >
        <SkuSelectField
          allSkus={allSkus}
          productsLoading={productsLoading}
          onSkuSelected={(skuInfo) => {
            setSkuInfoMap((prev) => ({ ...prev, [skuInfo.skuId]: skuInfo }));
          }}
        />
        <ProFormDigit name="quantity" label="数量" min={0.01} rules={[{ required: true, message: "请输入数量" }]} colProps={{ xs: 24, md: 3 }} />
        <ProFormDigit name="unitPrice" label="单价" min={0} fieldProps={{ precision: 2 }} colProps={{ xs: 24, md: 3 }} />
      </ProFormList>
    </ModalForm>
  );
}

/**
 * SKU selector connected to the product catalog.
 * When selected, notifies parent with SKU details for auto-fill.
 */
function SkuSelectField({
  allSkus, productsLoading, onSkuSelected
}: {
  allSkus: Array<{
    skuId: string; skuCode: string; productName: string;
    unit: string; unitPrice?: number; label: string
  }>;
  productsLoading: boolean;
  onSkuSelected: (skuInfo: {
    skuId: string; skuCode: string; productName: string; unit: string; unitPrice?: number
  }) => void;
}) {
  return (
    <ProFormSelect
      name="skuId"
      label="选择商品SKU"
      rules={[{ required: true, message: "请选择商品" }]}
      colProps={{ xs: 24, md: 5 }}
      options={allSkus.map((s) => ({ label: s.label, value: s.skuId }))}
      fieldProps={{
        placeholder: "搜索产品名称或SKU编码",
        showSearch: true,
        loading: productsLoading,
        filterOption: (input: string, option: any) =>
          (option?.label as string)?.toLowerCase().includes(input.toLowerCase()) ?? false,
        onChange: (value: string) => {
          const found = allSkus.find((s) => s.skuId === value);
          if (found) {
            onSkuSelected({
              skuId: found.skuId,
              skuCode: found.skuCode,
              productName: found.productName,
              unit: found.unit,
              unitPrice: found.unitPrice
            });
          }
        }
      }}
    />
  );
}
