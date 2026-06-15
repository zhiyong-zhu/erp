import { SALES_PERMISSIONS } from "@erp/shared";
import { App, Button, Drawer, Space, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { changeSaleReturnStatus, fetchSaleReturnDetail, fetchSaleReturns } from "../../../api/sales";
import { hasPermission } from "../../../store/auth";
import type { SaleReturnRecord } from "../../../types/sales";

const { Title, Text } = Typography;

const STATUS_CONFIG: Record<string, { label: string; color: string }> = {
  PENDING_REVIEW: { label: "待审核", color: "orange" },
  APPROVED: { label: "已审核", color: "blue" },
  INSPECTED: { label: "已检验", color: "cyan" },
  REFUNDED: { label: "已退款", color: "purple" },
  COMPLETED: { label: "已完成", color: "green" },
  REJECTED: { label: "已驳回", color: "red" }
};

export function SaleReturnPage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<SaleReturnRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [viewingReturn, setViewingReturn] = useState<SaleReturnRecord | null>(null);
  const { message } = App.useApp();
  const canUpdate = hasPermission(SALES_PERMISSIONS.RETURN_UPDATE);

  useEffect(() => {
    void loadData();
  }, []);

  async function loadData(nextPageNum = pageNum, nextPageSize = pageSize) {
    setLoading(true);
    try {
      const data = await fetchSaleReturns({ pageNum: nextPageNum, pageSize: nextPageSize });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载退货单失败");
    } finally {
      setLoading(false);
    }
  }

  async function openDetail(record: SaleReturnRecord) {
    try {
      setViewingReturn(await fetchSaleReturnDetail(record.id));
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载退货详情失败");
    }
  }

  async function handleStatus(id: string, action: "approve" | "reject" | "complete" | "inspect" | "refund") {
    try {
      await changeSaleReturnStatus(id, { action });
      message.success("状态已更新");
      await loadData();
      if (viewingReturn?.id === id) {
        setViewingReturn(await fetchSaleReturnDetail(id));
      }
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "操作失败");
    }
  }

  const columns: ColumnsType<SaleReturnRecord> = [
    { title: "退货单号", dataIndex: "returnNo", key: "returnNo", width: 170 },
    { title: "关联订单", dataIndex: "saleOrderNo", key: "saleOrderNo", width: 170 },
    { title: "客户", dataIndex: "customerName", key: "customerName", width: 140 },
    {
      title: "状态", dataIndex: "status", key: "status", width: 100,
      render: (v: string) => { const c = STATUS_CONFIG[v]; return c ? <Tag color={c.color}>{c.label}</Tag> : v; }
    },
    {
      title: "退货金额", dataIndex: "totalAmount", key: "totalAmount", width: 120,
      render: (v: number) => v != null ? `¥${Number(v).toLocaleString()}` : "-"
    },
    { title: "原因", dataIndex: "reason", key: "reason", ellipsis: true },
    { title: "创建时间", dataIndex: "createdAt", key: "createdAt", width: 170 },
    {
      title: "操作", key: "actions", width: 300,
      render: (_, record) => (
        <Space size="small" wrap>
          <Button type="link" onClick={() => void openDetail(record)}>详情</Button>
          {record.status === "PENDING_REVIEW" && (
            <>
              <Button type="link" disabled={!canUpdate} onClick={() => void handleStatus(record.id, "approve")}>审核通过</Button>
              <Button type="link" disabled={!canUpdate} onClick={() => void handleStatus(record.id, "reject")}>驳回</Button>
            </>
          )}
          {record.status === "APPROVED" && (
            <Button type="link" disabled={!canUpdate} onClick={() => void handleStatus(record.id, "inspect")}>检验入库</Button>
          )}
          {record.status === "INSPECTED" && (
            <Button type="link" disabled={!canUpdate} onClick={() => void handleStatus(record.id, "refund")}>确认退款</Button>
          )}
          {record.status === "REFUNDED" && (
            <Button type="link" disabled={!canUpdate} onClick={() => void handleStatus(record.id, "complete")}>完成</Button>
          )}
        </Space>
      )
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>销售管理 / 销售退货</Title>
          <Text type="secondary">管理销售退货的审核、检验、退款和完成流程。</Text>
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

      <Drawer title="退货单详情" open={!!viewingReturn} width={920} onClose={() => setViewingReturn(null)} destroyOnClose>
        {viewingReturn ? (
          <>
            <Space direction="vertical" size={8} style={{ width: "100%" }}>
              <Text strong>退货单号：{viewingReturn.returnNo}</Text>
              <Text>关联订单：{viewingReturn.saleOrderNo}</Text>
              <Text>客户：{viewingReturn.customerName}</Text>
              <Text>状态：{STATUS_CONFIG[viewingReturn.status]?.label ?? viewingReturn.status}</Text>
              <Text>退货金额：¥{Number(viewingReturn.totalAmount ?? 0).toLocaleString()}</Text>
              {viewingReturn.reason && <Text>原因：{viewingReturn.reason}</Text>}
              {viewingReturn.remark && <Text>备注：{viewingReturn.remark}</Text>}
            </Space>
            <Table
              rowKey="id"
              style={{ marginTop: 16 }}
              pagination={false}
              size="small"
              columns={[
                { title: "SKU编码", dataIndex: "skuCode", key: "skuCode", width: 130 },
                { title: "品名", dataIndex: "productName", key: "productName", width: 160 },
                { title: "数量", dataIndex: "quantity", key: "quantity", width: 80 },
                { title: "单价", dataIndex: "unitPrice", key: "unitPrice", width: 90 },
                { title: "退货金额", dataIndex: "returnAmount", key: "returnAmount", width: 100 },
                { title: "原因", dataIndex: "reason", key: "reason" }
              ]}
              dataSource={viewingReturn.items}
            />
          </>
        ) : null}
      </Drawer>
    </section>
  );
}
