import { App, Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchPurchasePayables } from "../../../api/purchase";
import type { PurchasePayableStatRecord } from "../../../types/purchase";

const { Title, Text } = Typography;

export function PurchasePayablePage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<PurchasePayableStatRecord[]>([]);
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
      const data = await fetchPurchasePayables({ pageNum: nextPageNum, pageSize: nextPageSize });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载应付统计失败");
    } finally {
      setLoading(false);
    }
  }

  const columns: ColumnsType<PurchasePayableStatRecord> = [
    { title: "供应商", dataIndex: "supplierName", key: "supplierName", width: 200 },
    { title: "采购单数", dataIndex: "orderCount", key: "orderCount", width: 100 },
    { title: "退货单数", dataIndex: "returnCount", key: "returnCount", width: 100 },
    { title: "采购总金额", dataIndex: "orderAmount", key: "orderAmount", width: 140 },
    { title: "退货总金额", dataIndex: "returnAmount", key: "returnAmount", width: 140 },
    { title: "净应付金额", dataIndex: "netPayableAmount", key: "netPayableAmount", width: 140 }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>采购管理 / 应付统计</Title>
          <Text type="secondary">按供应商汇总采购金额、退货金额和净应付金额，用于财务对账和付款计划。</Text>
        </div>
      </div>

      <Table
        rowKey="supplierId"
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
