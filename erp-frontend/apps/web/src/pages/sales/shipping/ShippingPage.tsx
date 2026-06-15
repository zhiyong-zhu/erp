import { SALES_PERMISSIONS } from "@erp/shared";
import { App, Space, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchShippingOrders } from "../../../api/sales";
import { hasPermission } from "../../../store/auth";
import type { ShippingRecord } from "../../../types/sales";

const { Title, Text } = Typography;

const STATUS_MAP: Record<string, { label: string; color: string }> = {
  PENDING: { label: "待发货", color: "orange" },
  SHIPPED: { label: "已发货", color: "blue" },
  DELIVERED: { label: "已签收", color: "green" }
};

export function ShippingPage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<ShippingRecord[]>([]);
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
      const data = await fetchShippingOrders({ pageNum: nextPageNum, pageSize: nextPageSize });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载发货单失败");
    } finally {
      setLoading(false);
    }
  }

  const columns: ColumnsType<ShippingRecord> = [
    { title: "承运商", dataIndex: "carrierName", key: "carrierName", width: 140 },
    { title: "运单号", dataIndex: "trackingNumber", key: "trackingNumber", width: 180 },
    {
      title: "状态", dataIndex: "status", key: "status", width: 100,
      render: (v: string) => { const s = STATUS_MAP[v]; return s ? <Tag color={s.color}>{s.label}</Tag> : v; }
    },
    { title: "发货时间", dataIndex: "shippedAt", key: "shippedAt", width: 180 },
    { title: "签收时间", dataIndex: "receivedAt", key: "receivedAt", width: 180 },
    { title: "备注", dataIndex: "remark", key: "remark" }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>销售管理 / 发货管理</Title>
          <Text type="secondary">查看所有发货/物流单据及状态。</Text>
        </div>
      </div>
      <Table
        rowKey="id"
        columns={columns}
        dataSource={records}
        loading={loading}
        pagination={{
          current: pageNum, pageSize, total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadData(nextPageNum, nextPageSize)
        }}
      />
    </section>
  );
}
