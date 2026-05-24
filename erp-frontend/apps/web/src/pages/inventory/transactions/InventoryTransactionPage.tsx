import { App, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchInventoryTransactions } from "../../../api/inventory";
import type { InventoryTransactionRecord } from "../../../types/inventory";

const { Title, Text } = Typography;

export function InventoryTransactionPage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<InventoryTransactionRecord[]>([]);
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
      const data = await fetchInventoryTransactions({ pageNum: nextPageNum, pageSize: nextPageSize });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载库存流水失败");
    } finally {
      setLoading(false);
    }
  }

  const columns: ColumnsType<InventoryTransactionRecord> = [
    { title: "原料编码", dataIndex: "materialCode", key: "materialCode", width: 140 },
    { title: "原料名称", dataIndex: "materialName", key: "materialName", width: 180 },
    {
      title: "流水类型",
      dataIndex: "transactionType",
      key: "transactionType",
      width: 140,
      render: (value: string) => <Tag>{value}</Tag>
    },
    { title: "变化数量", dataIndex: "quantity", key: "quantity", width: 120 },
    { title: "结存数量", dataIndex: "balanceAfter", key: "balanceAfter", width: 120 },
    { title: "来源类型", dataIndex: "sourceType", key: "sourceType", width: 120 },
    { title: "来源单号", dataIndex: "sourceOrderNo", key: "sourceOrderNo", width: 180 },
    { title: "备注", dataIndex: "remark", key: "remark" },
    { title: "创建时间", dataIndex: "createdAt", key: "createdAt", width: 180 }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>库存管理 / 库存流水</Title>
          <Text type="secondary">查看采购入库、采购退货等业务形成的库存流水，追踪库存变化过程。</Text>
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
