import { CheckOutlined, EditOutlined, InboxOutlined, PlusOutlined, SendOutlined } from "@ant-design/icons";
import {
  ModalForm,
  ProFormDigit,
  ProFormList,
  ProFormSelect,
  ProFormText,
  ProFormTextArea
} from "@ant-design/pro-components";
import { App, Button, Drawer, Input, Modal, Space, Table, Tag, Typography } from "antd";
import { PURCHASE_PERMISSIONS } from "@erp/shared";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import {
  changePurchaseOrderStatus,
  createPurchaseOrder,
  createPurchaseReturn,
  fetchPurchaseOrderDetail,
  fetchPurchaseOrders,
  fetchPurchaseReturns,
  receivePurchaseOrder,
  updatePurchaseOrder
} from "../../../api/purchase";
import { fetchMaterials, fetchSuppliers } from "../../../api/material";
import { hasPermission } from "../../../store/auth";
import type {
  PurchaseOrderReceivePayload,
  PurchaseOrderRecord,
  PurchaseOrderStatusPayload,
  PurchaseOrderUpdatePayload
} from "../../../types/purchase";

const { Title, Text } = Typography;

export function PurchaseOrderPage() {
  const [loading, setLoading] = useState(false);
  const [orders, setOrders] = useState<PurchaseOrderRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [viewingOrder, setViewingOrder] = useState<PurchaseOrderRecord | null>(null);
  const [editingOrder, setEditingOrder] = useState<PurchaseOrderRecord | null>(null);
  const [createOpen, setCreateOpen] = useState(false);
  const [receivingOrder, setReceivingOrder] = useState<PurchaseOrderRecord | null>(null);
  const [returningOrder, setReturningOrder] = useState<PurchaseOrderRecord | null>(null);
  const [returnsLoading, setReturnsLoading] = useState(false);
  const [returns, setReturns] = useState<any[]>([]);
  const { message } = App.useApp();
  const canCreate = hasPermission(PURCHASE_PERMISSIONS.ORDER_CREATE);
  const canUpdate = hasPermission(PURCHASE_PERMISSIONS.ORDER_UPDATE);

  useEffect(() => {
    void loadOrders();
  }, []);

  async function loadOrders(nextPageNum = pageNum, nextPageSize = pageSize) {
    setLoading(true);
    try {
      const data = await fetchPurchaseOrders({ pageNum: nextPageNum, pageSize: nextPageSize });
      setOrders(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载采购单失败");
    } finally {
      setLoading(false);
    }
  }

  async function openDetail(record: PurchaseOrderRecord) {
    try {
      setViewingOrder(await fetchPurchaseOrderDetail(record.id));
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载采购单详情失败");
    }
  }

  async function openEdit(record: PurchaseOrderRecord) {
    try {
      setEditingOrder(await fetchPurchaseOrderDetail(record.id));
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载采购单详情失败");
    }
  }

  async function openReceive(record: PurchaseOrderRecord) {
    try {
      setReceivingOrder(await fetchPurchaseOrderDetail(record.id));
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载采购单详情失败");
    }
  }

  async function handleCreate(values: PurchaseOrderUpdatePayload) {
    try {
      await createPurchaseOrder(values);
      message.success("采购单创建成功");
      setCreateOpen(false);
      await loadOrders();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "采购单创建失败");
      return false;
    }
  }

  async function handleUpdate(values: PurchaseOrderUpdatePayload) {
    if (!editingOrder) {
      return false;
    }
    try {
      await updatePurchaseOrder(editingOrder.id, values);
      message.success("采购草稿更新成功");
      setEditingOrder(null);
      await loadOrders();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "采购草稿更新失败");
      return false;
    }
  }

  async function handleReceive(values: PurchaseOrderReceivePayload) {
    if (!receivingOrder) {
      return false;
    }
    try {
      await receivePurchaseOrder(receivingOrder.id, values);
      message.success("采购收货成功");
      setReceivingOrder(null);
      await loadOrders();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "采购收货失败");
      return false;
    }
  }

  async function handleReturn(values: any) {
    if (!returningOrder) {
      return false;
    }
    try {
      await createPurchaseReturn(returningOrder.id, values);
      message.success("采购退货成功");
      setReturningOrder(null);
      await loadOrders();
      await loadReturns();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "采购退货失败");
      return false;
    }
  }

  async function loadReturns() {
    setReturnsLoading(true);
    try {
      const data = await fetchPurchaseReturns({ pageNum: 1, pageSize: 50 });
      setReturns(data.records);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载采购退货失败");
    } finally {
      setReturnsLoading(false);
    }
  }

  async function handleStatus(record: PurchaseOrderRecord, action: PurchaseOrderStatusPayload["action"]) {
    try {
      await changePurchaseOrderStatus(record.id, { action });
      message.success("采购单状态已更新");
      await loadOrders();
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "采购单状态更新失败");
    }
  }

  const columns: ColumnsType<PurchaseOrderRecord> = [
    { title: "采购单号", dataIndex: "orderNo", key: "orderNo", width: 180 },
    { title: "供应商", dataIndex: "supplierName", key: "supplierName", width: 180 },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 140,
      render: (value: string) => <Tag>{value}</Tag>
    },
    { title: "来源", dataIndex: "sourceType", key: "sourceType", width: 120 },
    { title: "总金额", dataIndex: "totalAmount", key: "totalAmount", width: 120 },
    { title: "创建时间", dataIndex: "createdAt", key: "createdAt", width: 180 },
    {
      title: "操作",
      key: "actions",
      width: 360,
      render: (_, record) => (
        <Space size="small" wrap>
          <Button type="link" onClick={() => void openDetail(record)}>
            详情
          </Button>
          {record.status === "DRAFT" ? (
            <Button type="link" icon={<EditOutlined />} disabled={!canUpdate} onClick={() => void openEdit(record)}>
              编辑
            </Button>
          ) : null}
          {record.status === "DRAFT" ? (
            <Button type="link" icon={<SendOutlined />} disabled={!canUpdate} onClick={() => void handleStatus(record, "submit")}>
              提交
            </Button>
          ) : null}
          {record.status === "PENDING_APPROVAL" ? (
            <Button type="link" icon={<CheckOutlined />} disabled={!canUpdate} onClick={() => void handleStatus(record, "approve")}>
              审批通过
            </Button>
          ) : null}
          {record.status === "PENDING_APPROVAL" ? (
            <Button type="link" disabled={!canUpdate} onClick={() => void handleStatus(record, "reject")}>
              驳回
            </Button>
          ) : null}
          {(record.status === "APPROVED" || record.status === "PARTIAL_RECEIVED") ? (
            <Button type="link" icon={<InboxOutlined />} disabled={!canUpdate} onClick={() => void openReceive(record)}>
              收货入库
            </Button>
          ) : null}
          {(record.status === "PARTIAL_RECEIVED" || record.status === "RECEIVED") ? (
            <Button type="link" disabled={!canUpdate} onClick={() => setReturningOrder(record)}>
              采购退货
            </Button>
          ) : null}
        </Space>
      )
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>采购管理 / 采购单</Title>
          <Text type="secondary">管理采购单，推进提交、审批、收货入库流程。</Text>
        </div>
        <Space>
          <Input.Search allowClear placeholder="搜索采购单号或供应商" style={{ width: 260 }} />
          <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateOpen(true)}>
            新建采购单
          </Button>
        </Space>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={orders}
        loading={loading}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadOrders(nextPageNum, nextPageSize)
        }}
      />

      <Drawer title="采购单详情" open={!!viewingOrder} width={920} onClose={() => setViewingOrder(null)} destroyOnClose>
        {viewingOrder ? (
          <>
            <Space direction="vertical" size={12} style={{ width: "100%" }}>
              <Text>采购单号：{viewingOrder.orderNo}</Text>
              <Text>供应商：{viewingOrder.supplierName}</Text>
              <Text>状态：{viewingOrder.status}</Text>
              <Text>总金额：{viewingOrder.totalAmount ?? 0}</Text>
              <Text>备注：{viewingOrder.remark || "-"}</Text>
            </Space>
            <Table
              rowKey="id"
              style={{ marginTop: 16 }}
              pagination={false}
              columns={[
                { title: "原料编码", dataIndex: "materialCode", key: "materialCode", width: 140 },
                { title: "原料名称", dataIndex: "materialName", key: "materialName", width: 180 },
                { title: "数量", dataIndex: "quantity", key: "quantity", width: 100 },
                { title: "已收货", dataIndex: "receivedQuantity", key: "receivedQuantity", width: 100 },
                { title: "已合格", dataIndex: "acceptedQuantity", key: "acceptedQuantity", width: 100 },
                { title: "已不合格", dataIndex: "rejectedQuantity", key: "rejectedQuantity", width: 110 },
                { title: "已退货", dataIndex: "returnedQuantity", key: "returnedQuantity", width: 100 },
                { title: "验收结果", dataIndex: "inspectionResult", key: "inspectionResult", width: 140 },
                { title: "报价", dataIndex: "quotePrice", key: "quotePrice", width: 100 },
                { title: "预估金额", dataIndex: "estimatedAmount", key: "estimatedAmount", width: 120 },
                { title: "交期(天)", dataIndex: "leadTimeDays", key: "leadTimeDays", width: 100 }
              ]}
              dataSource={viewingOrder.items}
            />
          </>
        ) : null}
      </Drawer>

      <ModalForm<PurchaseOrderUpdatePayload>
        title="新建采购单"
        open={createOpen}
        width={1080}
        modalProps={{ destroyOnClose: true, onCancel: () => setCreateOpen(false) }}
        onFinish={async (values) => {
          const supplierIdStr = String(values.supplierId ?? "");
          const supplierData = await fetchSuppliers({ pageNum: 1, pageSize: 200 });
          const supplier = supplierData.records.find((s) => s.id === supplierIdStr);
          const materialData = await fetchMaterials({ pageNum: 1, pageSize: 500 });
          const enrichedItems = (values.items ?? []).map((item: any) => {
            const mat = materialData.records.find((m) => m.id === item.materialId);
            const qty = item.quantity ?? 0;
            const price = item.quotePrice ?? 0;
            const estimatedAmount = item.estimatedAmount ?? (price * qty);
            return {
              ...item,
              materialCode: mat?.code ?? item.materialCode,
              materialName: mat?.name ?? item.materialName,
              unit: mat?.unit ?? item.unit,
              sourceType: "MANUAL",
              estimatedAmount
            };
          });
          return handleCreate({
            ...values,
            supplierId: supplierIdStr as any,
            supplierName: supplier?.name,
            items: enrichedItems
          });
        }}
      >
        <ProFormSelect
          name="supplierId"
          label="供应商"
          rules={[{ required: true, message: "请选择供应商" }]}
          showSearch
          request={async () => {
            const data = await fetchSuppliers({ pageNum: 1, pageSize: 200 });
            return data.records.map((s) => ({ label: `${s.name} (${s.code})`, value: s.id }));
          }}
          fieldProps={{ optionFilterProp: "label", placeholder: "搜索并选择供应商" }}
        />
        <ProFormTextArea name="remark" label="备注" fieldProps={{ autoSize: { minRows: 2, maxRows: 4 } }} />
        <ProFormList
          name="items"
          label="采购明细"
          creatorButtonProps={{ creatorButtonText: "添加物料" }}
          min={1}
        >
          <ProFormSelect
            name="materialId"
            label="原料"
            rules={[{ required: true, message: "请选择原料" }]}
            showSearch
            request={async () => {
              const data = await fetchMaterials({ pageNum: 1, pageSize: 500 });
              return data.records.map((m) => ({ label: `${m.name} (${m.code})`, value: m.id }));
            }}
            fieldProps={{ optionFilterProp: "label", placeholder: "搜索并选择原料" }}
          />
          <ProFormDigit name="quantity" label="采购数量" min={0.01} rules={[{ required: true }]} />
          <ProFormDigit name="quotePrice" label="报价" min={0} fieldProps={{ precision: 2 }} />
          <ProFormDigit name="estimatedAmount" label="预估金额(留空自动算)" min={0} fieldProps={{ precision: 2 }} />
          <ProFormDigit name="leadTimeDays" label="交期(天)" min={0} />
        </ProFormList>
      </ModalForm>

      <ModalForm<PurchaseOrderUpdatePayload>
        title="编辑采购草稿"
        open={!!editingOrder}
        width={1080}
        initialValues={
          editingOrder
            ? {
                supplierId: editingOrder.supplierId,
                supplierName: editingOrder.supplierName,
                remark: editingOrder.remark ?? "",
                items: editingOrder.items.map((item) => ({
                  id: item.id,
                  materialId: item.materialId,
                  materialCode: item.materialCode,
                  materialName: item.materialName,
                  unit: item.unit ?? undefined,
                  quantity: item.quantity ?? 0,
                  quotePrice: item.quotePrice ?? undefined,
                  estimatedAmount: item.estimatedAmount ?? undefined,
                  leadTimeDays: item.leadTimeDays ?? undefined,
                  sourceType: item.sourceType ?? undefined,
                  sourceRefId: item.sourceRefId ?? undefined,
                  receivedQuantity: item.receivedQuantity ?? undefined
                }))
              }
            : undefined
        }
        modalProps={{ destroyOnClose: true, onCancel: () => setEditingOrder(null) }}
        onFinish={handleUpdate}
      >
        <ProFormText name="supplierName" label="供应商" rules={[{ required: true }]} />
        <ProFormTextArea name="remark" label="备注" fieldProps={{ autoSize: { minRows: 2, maxRows: 4 } }} />
        <ProFormList name="items" label="采购明细" creatorButtonProps={false} itemRender={(dom) => dom.listDom}>
          <ProFormText name="materialCode" label="原料编码" readonly />
          <ProFormText name="materialName" label="原料名称" readonly />
          <ProFormText name="unit" label="单位" readonly />
          <ProFormDigit name="quantity" label="采购数量" min={0.01} rules={[{ required: true }]} />
          <ProFormDigit name="quotePrice" label="报价" min={0} />
          <ProFormDigit name="estimatedAmount" label="预估金额" min={0} />
          <ProFormDigit name="leadTimeDays" label="交期(天)" min={0} />
        </ProFormList>
      </ModalForm>

      <ModalForm<PurchaseOrderReceivePayload>
        title="采购收货入库"
        open={!!receivingOrder}
        width={960}
        initialValues={{
          items: receivingOrder?.items.map((item) => ({
            itemId: item.id,
            receivedQuantity: 0,
            acceptedQuantity: 0,
            rejectedQuantity: 0,
            inspectionResult: "PASSED",
            exceptionReason: ""
          }))
        }}
        modalProps={{ destroyOnClose: true, onCancel: () => setReceivingOrder(null) }}
        onFinish={handleReceive}
      >
        <ProFormList name="items" label="收货明细" creatorButtonProps={false} itemRender={(dom) => dom.listDom}>
          <ProFormText name="itemId" label="明细ID" readonly />
          <ProFormDigit name="receivedQuantity" label="本次收货数量" min={0} rules={[{ required: true }]} />
          <ProFormDigit name="acceptedQuantity" label="合格数量" min={0} rules={[{ required: true }]} />
          <ProFormDigit name="rejectedQuantity" label="不合格数量" min={0} />
          <ProFormText name="inspectionResult" label="验收结论" />
          <ProFormTextArea name="exceptionReason" label="差异原因" fieldProps={{ autoSize: { minRows: 2, maxRows: 3 } }} />
        </ProFormList>
      </ModalForm>

      <ModalForm<any>
        title="采购退货"
        open={!!returningOrder}
        width={960}
        initialValues={{
          items: returningOrder?.items.map((item) => ({
            itemId: item.id,
            returnQuantity: 0,
            reason: ""
          })),
          remark: ""
        }}
        modalProps={{ destroyOnClose: true, onCancel: () => setReturningOrder(null) }}
        onFinish={handleReturn}
      >
        <ProFormTextArea name="remark" label="退货备注" fieldProps={{ autoSize: { minRows: 2, maxRows: 3 } }} />
        <ProFormList name="items" label="退货明细" creatorButtonProps={false} itemRender={(dom) => dom.listDom}>
          <ProFormText name="itemId" label="明细ID" readonly />
          <ProFormDigit name="returnQuantity" label="退货数量" min={0} rules={[{ required: true }]} />
          <ProFormTextArea name="reason" label="退货原因" fieldProps={{ autoSize: { minRows: 2, maxRows: 3 } }} />
        </ProFormList>
      </ModalForm>

      <Table
        style={{ marginTop: 24 }}
        rowKey="id"
        loading={returnsLoading}
        columns={[
          { title: "退货单号", dataIndex: "returnNo", key: "returnNo", width: 180 },
          { title: "采购单号", dataIndex: "purchaseOrderNo", key: "purchaseOrderNo", width: 180 },
          { title: "供应商", dataIndex: "supplierName", key: "supplierName", width: 180 },
          { title: "状态", dataIndex: "status", key: "status", width: 100 },
          { title: "总金额", dataIndex: "totalAmount", key: "totalAmount", width: 120 },
          { title: "创建时间", dataIndex: "createdAt", key: "createdAt", width: 180 }
        ]}
        dataSource={returns}
        pagination={false}
      />
    </section>
  );
}
