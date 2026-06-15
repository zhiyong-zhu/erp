import { App, Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchSaleReceivables } from "../../../api/sales";
import type { SaleReceivableStatRecord } from "../../../types/sales";

const { Title, Text } = Typography;

export function SaleReceivablePage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<SaleReceivableStatRecord[]>([]);
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
      const data = await fetchSaleReceivables({ pageNum: nextPageNum, pageSize: nextPageSize });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载应收统计失败");
    } finally {
      setLoading(false);
    }
  }

  const columns: ColumnsType<SaleReceivableStatRecord> = [
    { title: "客户", dataIndex: "customerName", key: "customerName", width: 200 },
    {
      title: "订单金额", dataIndex: "orderAmount", key: "orderAmount", width: 140,
      render: (v: number) => v != null ? `¥${Number(v).toLocaleString()}` : "-"
    },
    {
      title: "退货金额", dataIndex: "returnAmount", key: "returnAmount", width: 140,
      render: (v: number) => v != null ? `¥${Number(v).toLocaleString()}` : "-"
    },
    {
      title: "应收净额", dataIndex: "netReceivableAmount", key: "netReceivableAmount", width: 140,
      render: (v: number) => v != null ? `¥${Number(v).toLocaleString()}` : "-"
    },
    { title: "订单数", dataIndex: "orderCount", key: "orderCount", width: 100 },
    { title: "退货数", dataIndex: "returnCount", key: "returnCount", width: 100 }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>销售管理 / 应收统计</Title>
          <Text type="secondary">按客户汇总销售订单和退货金额，展示应收净额。</Text>
        </div>
      </div>
      <Table
        rowKey="customerId"
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
