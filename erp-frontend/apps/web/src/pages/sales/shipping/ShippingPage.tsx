import { SALES_PERMISSIONS } from "@erp/shared";
import { App, Button, Descriptions, Drawer, Modal, Space, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { exportShippingOrders, fetchShippingOrderDetail, fetchShippingOrders, reviewShippingOrder } from "../../../api/sales";
import { hasPermission } from "../../../store/auth";
import type { ShippingItemRecord, ShippingRecord } from "../../../types/sales";
import { downloadBlob } from "../../../utils/export";

const { Title, Text } = Typography;

const STATUS_MAP: Record<string, { label: string; color: string }> = {
  PENDING_REVIEW: { label: "待复核", color: "orange" },
  PENDING: { label: "待发货", color: "orange" },
  SHIPPED: { label: "已发货", color: "blue" },
  CANCELLED: { label: "已取消", color: "default" },
  DELIVERED: { label: "已签收", color: "green" }
};

export function ShippingPage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<ShippingRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [detailOpen, setDetailOpen] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [selectedShipping, setSelectedShipping] = useState<ShippingRecord | null>(null);
  const { message } = App.useApp();
  const canReview = hasPermission(SALES_PERMISSIONS.SHIPPING_UPDATE);

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

  async function handleReview(record: ShippingRecord) {
    try {
      const reviewed = await reviewShippingOrder(record.id);
      message.success("复核出库完成");
      if (selectedShipping?.id === record.id) {
        setSelectedShipping(reviewed);
      }
      await loadData();
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "复核失败");
    }
  }

  async function openDetail(record: ShippingRecord) {
    setSelectedShipping(record);
    setDetailOpen(true);
    setDetailLoading(true);
    try {
      setSelectedShipping(await fetchShippingOrderDetail(record.id));
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载发货明细失败");
    } finally {
      setDetailLoading(false);
    }
  }

  async function handleExport() {
    try {
      const blob = await exportShippingOrders();
      downloadBlob("shipping-orders.xlsx", blob);
      message.success("发货单导出成功");
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "发货单导出失败");
    }
  }

  function confirmReview(record: ShippingRecord) {
    Modal.confirm({
      title: "确认复核出库？",
      content: `运单号：${record.trackingNumber ?? record.id}`,
      okText: "确认",
      cancelText: "取消",
      onOk: () => handleReview(record)
    });
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
    { title: "备注", dataIndex: "remark", key: "remark" },
    {
      title: "操作", key: "actions", width: 120,
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => void openDetail(record)}>明细</Button>
          {record.status === "PENDING_REVIEW" && (
            <Button type="link" disabled={!canReview} onClick={() => confirmReview(record)}>复核出库</Button>
          )}
        </Space>
      )
    }
  ];

  const itemColumns: ColumnsType<ShippingItemRecord> = [
    { title: "SKU", dataIndex: "skuCode", key: "skuCode", width: 160, render: formatEmpty },
    { title: "产品", dataIndex: "productName", key: "productName", width: 220, render: formatEmpty },
    { title: "发货数量", dataIndex: "quantity", key: "quantity", width: 120 },
    {
      title: "序列号", dataIndex: "serialNos", key: "serialNos", width: 260,
      render: (value?: string[]) => value && value.length > 0 ? value.join("，") : "-"
    },
    { title: "订单明细ID", dataIndex: "saleOrderItemId", key: "saleOrderItemId", width: 240 }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>销售管理 / 发货管理</Title>
          <Text type="secondary">查看所有发货/物流单据及状态。</Text>
        </div>
        <Button onClick={() => void handleExport()}>导出发货Excel</Button>
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
      <Drawer
        title="发货单明细"
        width={900}
        open={detailOpen}
        onClose={() => setDetailOpen(false)}
        destroyOnClose
      >
        <Descriptions bordered size="small" column={2} style={{ marginBottom: 16 }}>
          <Descriptions.Item label="销售订单ID">{selectedShipping?.saleOrderId ?? "-"}</Descriptions.Item>
          <Descriptions.Item label="状态">{selectedShipping ? renderStatus(selectedShipping.status) : "-"}</Descriptions.Item>
          <Descriptions.Item label="承运商">{selectedShipping?.carrierName || "-"}</Descriptions.Item>
          <Descriptions.Item label="运单号">{selectedShipping?.trackingNumber || "-"}</Descriptions.Item>
          <Descriptions.Item label="发货时间">{selectedShipping?.shippedAt || "-"}</Descriptions.Item>
          <Descriptions.Item label="复核/更新时间">{selectedShipping?.updatedAt || "-"}</Descriptions.Item>
          <Descriptions.Item label="备注" span={2}>{selectedShipping?.remark || "-"}</Descriptions.Item>
        </Descriptions>
        <Space style={{ marginBottom: 16 }}>
          {selectedShipping?.status === "PENDING_REVIEW" && (
            <Button type="primary" disabled={!canReview} onClick={() => confirmReview(selectedShipping)}>复核出库</Button>
          )}
        </Space>
        <Table
          rowKey="id"
          columns={itemColumns}
          dataSource={selectedShipping?.items ?? []}
          loading={detailLoading}
          pagination={false}
        />
      </Drawer>
    </section>
  );
}

function renderStatus(status: string) {
  const statusMeta = STATUS_MAP[status];
  return statusMeta ? <Tag color={statusMeta.color}>{statusMeta.label}</Tag> : status;
}

function formatEmpty(value?: string | null) {
  return value || "-";
}
