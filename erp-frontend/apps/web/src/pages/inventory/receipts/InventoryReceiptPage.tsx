import { App, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchInventoryReceipts } from "../../../api/inventory";
import type { InventoryReceiptRecord } from "../../../types/inventory";

const { Title, Text } = Typography;

export function InventoryReceiptPage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<InventoryReceiptRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
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

  const columns: ColumnsType<InventoryReceiptRecord> = [
    { title: "入库单号", dataIndex: "receiptNo", key: "receiptNo", width: 180 },
    { title: "来源类型", dataIndex: "sourceType", key: "sourceType", width: 120 },
    { title: "来源单号", dataIndex: "sourceOrderNo", key: "sourceOrderNo", width: 180 },
    { title: "供应商", dataIndex: "supplierName", key: "supplierName", width: 180 },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 120,
      render: (value: string) => <Tag color={value === "COMPLETED" ? "green" : "default"}>{value}</Tag>
    },
    { title: "备注", dataIndex: "remark", key: "remark" },
    { title: "创建时间", dataIndex: "createdAt", key: "createdAt", width: 180 }
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
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadData(nextPageNum, nextPageSize)
        }}
      />
    </section>
  );
}
