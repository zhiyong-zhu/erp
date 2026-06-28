import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDependency, ProFormDigit, ProFormList, ProFormSelect, ProFormText } from "@ant-design/pro-components";
import { App, Button, Descriptions, Drawer, Space, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { createInventoryReceipt, fetchInventoryReceipts, fetchInventoryTransactions } from "../../../api/inventory";
import { fetchMaterials } from "../../../api/material";
import type { InventoryReceiptPayload, InventoryReceiptRecord, InventoryTransactionRecord } from "../../../types/inventory";
import type { MaterialRecord } from "../../../types/material";
import { printInventoryDocument } from "../../../utils/documentPrint";
import { useInventoryPositions } from "../components/useInventoryPositions";

const { Title, Text } = Typography;

const receiptTypeOptions = [
  { label: "手工入库", value: "MANUAL_IN" },
  { label: "生产退料", value: "PRODUCTION_RETURN" },
  { label: "采购退货回收", value: "PURCHASE_RETURN" },
  { label: "其他入库", value: "OTHER_IN" }
];

export function InventoryReceiptPage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<InventoryReceiptRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [selectedReceipt, setSelectedReceipt] = useState<InventoryReceiptRecord | null>(null);
  const [detailTransactions, setDetailTransactions] = useState<InventoryTransactionRecord[]>([]);
  const [createOpen, setCreateOpen] = useState(false);
  const [materials, setMaterials] = useState<MaterialRecord[]>([]);
  const { message } = App.useApp();
  const { locationOptions, loadPositions, applyLocation } = useInventoryPositions();

  const materialOptions = useMemo(
    () => materials.map((item) => ({
      label: `${item.code} · ${item.name}（库存 ${item.currentStock ?? 0}${item.unit ? ` ${item.unit}` : ""}）`,
      value: item.id
    })),
    [materials]
  );

  useEffect(() => {
    void loadData();
    void loadMaterials();
    void loadPositions();
  }, []);

  async function loadData(nextPageNum = pageNum, nextPageSize = pageSize) {
    setLoading(true);
    try {
      const data = await fetchInventoryReceipts({ pageNum: nextPageNum, pageSize: nextPageSize });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载入库单失败");
    } finally {
      setLoading(false);
    }
  }

  async function loadMaterials(keyword?: string) {
    try {
      const data = await fetchMaterials({ pageNum: 1, pageSize: 50, name: keyword, status: 1 });
      setMaterials(data.records);
    } catch {
      setMaterials([]);
    }
  }

  async function openDetail(record: InventoryReceiptRecord) {
    setSelectedReceipt(record);
    setDetailOpen(true);
    setDetailLoading(true);
    try {
      const data = await fetchInventoryTransactions({ pageNum: 1, pageSize: 200, receiptId: record.id });
      setDetailTransactions(data.records);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载入库明细失败");
      setDetailTransactions([]);
    } finally {
      setDetailLoading(false);
    }
  }

  async function handleCreate(values: InventoryReceiptPayload & { locationId?: string; receiptType?: string }) {
    try {
      const payload = normalizeReceiptPayload(applyLocation(values, values.locationId));
      await createInventoryReceipt(payload);
      message.success("入库成功");
      setCreateOpen(false);
      await loadData(1, pageSize);
      await loadMaterials();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "入库失败");
      return false;
    }
  }

  const columns: ColumnsType<InventoryReceiptRecord> = [
    { title: "入库单号", dataIndex: "receiptNo", key: "receiptNo", width: 180 },
    { title: "来源类型", dataIndex: "sourceType", key: "sourceType", width: 120 },
    { title: "来源单号", dataIndex: "sourceOrderNo", key: "sourceOrderNo", width: 180 },
    { title: "供应商", dataIndex: "supplierName", key: "supplierName", width: 180 },
    { title: "幂等键", dataIndex: "idempotencyKey", key: "idempotencyKey", width: 200, render: formatEmpty },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 120,
      render: (value: string) => <Tag color={value === "COMPLETED" ? "green" : "default"}>{value}</Tag>
    },
    { title: "备注", dataIndex: "remark", key: "remark" },
    { title: "创建时间", dataIndex: "createdAt", key: "createdAt", width: 180 },
    {
      title: "操作",
      key: "actions",
      width: 140,
      fixed: "right",
      render: (_, record) => (
        <>
          <Button type="link" onClick={() => void openDetail(record)}>明细</Button>
          <Button type="link" onClick={() => printInventoryDocument("receipt", record)}>打印</Button>
        </>
      )
    }
  ];

  const detailColumns: ColumnsType<InventoryTransactionRecord> = [
    { title: "原料编码", dataIndex: "materialCode", key: "materialCode", width: 140 },
    { title: "原料名称", dataIndex: "materialName", key: "materialName", width: 180 },
    { title: "入库数量", dataIndex: "quantity", key: "quantity", width: 120 },
    { title: "变更前", dataIndex: "balanceBefore", key: "balanceBefore", width: 120, render: formatQuantity },
    { title: "结存", dataIndex: "balanceAfter", key: "balanceAfter", width: 120 },
    {
      title: "仓库",
      dataIndex: "warehouseName",
      key: "warehouseName",
      width: 160,
      render: (value: string | null | undefined, record) => formatPosition(value, record.warehouseCode)
    },
    {
      title: "库位",
      dataIndex: "locationName",
      key: "locationName",
      width: 160,
      render: (value: string | null | undefined, record) => formatPosition(value, record.locationCode)
    },
    { title: "批次", dataIndex: "batchNo", key: "batchNo", width: 140 },
    { title: "来源明细ID", dataIndex: "sourceItemId", key: "sourceItemId", width: 220, render: formatEmpty }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>库存管理 / 入库管理</Title>
          <Text type="secondary">创建手工入库，或查看由采购收货生成的入库单记录。</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
          新建入库
        </Button>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={records}
        loading={loading}
        scroll={{ x: 1300 }}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadData(nextPageNum, nextPageSize)
        }}
      />

      <Drawer
        title="入库单明细"
        width={960}
        open={detailOpen}
        onClose={() => setDetailOpen(false)}
        destroyOnClose
      >
        {selectedReceipt && (
          <Descriptions bordered size="small" column={2} style={{ marginBottom: 16 }}>
            <Descriptions.Item label="入库单号">{selectedReceipt.receiptNo}</Descriptions.Item>
            <Descriptions.Item label="来源单号">{selectedReceipt.sourceOrderNo || "-"}</Descriptions.Item>
            <Descriptions.Item label="供应商">{selectedReceipt.supplierName || "-"}</Descriptions.Item>
            <Descriptions.Item label="幂等键">{selectedReceipt.idempotencyKey || "-"}</Descriptions.Item>
          </Descriptions>
        )}
        <Table
          rowKey="id"
          columns={detailColumns}
          dataSource={detailTransactions}
          loading={detailLoading}
          pagination={false}
          scroll={{ x: 1300 }}
        />
      </Drawer>

      <ModalForm<InventoryReceiptPayload & { locationId?: string; receiptType?: string }>
        title="新建入库"
        open={createOpen}
        modalProps={{ destroyOnHidden: true, onCancel: () => setCreateOpen(false), width: 760 }}
        initialValues={{
          sourceType: "MANUAL_IN",
          batchNo: "DEFAULT",
          idempotencyKey: `receipt-${Date.now()}`,
          items: [{}]
        }}
        onFinish={handleCreate}
      >
        <ProFormSelect
          name="sourceType"
          label="入库类型"
          options={receiptTypeOptions}
          rules={[{ required: true, message: "请选择入库类型" }]}
        />
        <ProFormText name="sourceOrderNo" label="来源单号" placeholder="可选，如采购单号/生产工单号" />
        <ProFormText name="idempotencyKey" label="幂等键" tooltip="重复提交时用于避免重复增加库存" rules={[{ required: true, message: "请输入幂等键" }]} />
        <ProFormSelect
          name="locationId"
          label="入库库位"
          options={locationOptions}
          rules={[{ required: true, message: "请选择入库库位" }]}
        />
        <ProFormText name="batchNo" label="批次" rules={[{ required: true, message: "请输入批次" }]} />
        <ProFormText name="remark" label="备注" />
        <ProFormList
          name="items"
          label="入库明细"
          creatorButtonProps={{ creatorButtonText: "添加明细" }}
          rules={[
            {
              validator: async (_, value) => {
                if (!value || value.length === 0) {
                  throw new Error("至少添加一条入库明细");
                }
              }
            }
          ]}
        >
          <ProFormSelect
            name="materialId"
            label="原料"
            showSearch
            options={materialOptions}
            fieldProps={{
              filterOption: false,
              onSearch: (value) => void loadMaterials(value)
            }}
            rules={[{ required: true, message: "请选择原料" }]}
          />
          <ProFormDependency name={["materialId"]}>
            {({ materialId }) => {
              const material = materials.find((item) => item.id === materialId);
              return (
                <Text type="secondary">
                  当前库存：{material?.currentStock ?? "-"} {material?.unit ?? ""}
                </Text>
              );
            }}
          </ProFormDependency>
          <ProFormDigit
            name="quantity"
            label="入库数量"
            min={0.01}
            fieldProps={{ precision: 2 }}
            rules={[{ required: true, message: "请输入入库数量" }]}
          />
          <ProFormText name="remark" label="明细备注" />
        </ProFormList>
      </ModalForm>
    </section>
  );
}

function normalizeReceiptPayload(values: Record<string, any>): InventoryReceiptPayload {
  const { locationId: _locationId, warehouseId: _warehouseId, items, ...rest } = values;
  return {
    ...rest,
    items: items ?? []
  } as InventoryReceiptPayload;
}

function formatQuantity(value?: number | null) {
  return value ?? 0;
}

function formatPosition(name?: string | null, code?: string | null) {
  if (name && code) {
    return `${name}（${code}）`;
  }
  return name || code || "-";
}

function formatEmpty(value?: string | null) {
  return value || "-";
}
