import { App, Button, Descriptions, Drawer, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchInventoryReceipts, fetchInventoryTransactions } from "../../../api/inventory";
import type { InventoryReceiptRecord, InventoryTransactionRecord } from "../../../types/inventory";
import { printInventoryDocument } from "../../../utils/documentPrint";

const { Title, Text } = Typography;

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
  const { message } = App.useApp();

  useEffect(() => {
    void loadData();
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
      message.error(err?.response?.data?.message ?? err?.message ?? "加载正式入库单失败");
    } finally {
      setLoading(false);
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
          <Title level={3} style={{ margin: 0 }}>库存管理 / 正式入库单</Title>
          <Text type="secondary">查看由采购收货生成的正式入库单记录，追踪入库来源和供应商。</Text>
        </div>
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
    </section>
  );
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
