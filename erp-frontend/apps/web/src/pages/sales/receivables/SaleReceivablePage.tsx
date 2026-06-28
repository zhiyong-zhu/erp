import { App, Button, Modal, Space, Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchSaleReceivables, fetchSaleOrders, createSalePayment, fetchSalePayments } from "../../../api/sales";
import type { SaleReceivableStatRecord, SalePaymentRecord, SaleOrderRecord } from "../../../types/sales";
import { CreateForm } from "../../../components/CreateForm";

const { Title, Text } = Typography;

const paymentMethodOptions = [
  { label: "银行转账", value: "BANK_TRANSFER" },
  { label: "现金", value: "CASH" },
  { label: "支付宝", value: "ALIPAY" },
  { label: "微信", value: "WECHAT" },
  { label: "其他", value: "OTHER" }
];

export function SaleReceivablePage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<SaleReceivableStatRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [paymentOpen, setPaymentOpen] = useState(false);
  const [selectedCustomer, setSelectedCustomer] = useState<SaleReceivableStatRecord | null>(null);
  const [customerOrders, setCustomerOrders] = useState<SaleOrderRecord[]>([]);
  const [orderPayments, setOrderPayments] = useState<Record<string, SalePaymentRecord[]>>({});
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

  async function openPayment(stat: SaleReceivableStatRecord) {
    setSelectedCustomer(stat);
    setPaymentOpen(true);
    try {
      const orderData = await fetchSaleOrders({ pageNum: 1, pageSize: 100 });
      const orders = orderData.records.filter(
        (o) => o.customerId === stat.customerId && !["PENDING_CONFIRM", "CANCELLED"].includes(o.status)
      );
      setCustomerOrders(orders);

      const paymentPromises = orders.map((o) =>
        fetchSalePayments({ pageNum: 1, pageSize: 50, saleOrderId: o.id })
          .then((res) => [o.id, res.records] as const)
          .catch(() => [o.id, [] as SalePaymentRecord[]] as const)
      );
      const results = await Promise.all(paymentPromises);
      const map: Record<string, SalePaymentRecord[]> = {};
      for (const [orderId, payments] of results) {
        map[orderId] = payments;
      }
      setOrderPayments(map);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载销售单失败");
    }
  }

  async function handlePayment(values: any) {
    const orderId = values.saleOrderId;
    if (!orderId) {
      message.error("请选择销售单");
      return false;
    }
    try {
      await createSalePayment(orderId, {
        receivedAmount: values.receivedAmount,
        paymentMethod: values.paymentMethod,
        remark: values.remark
      });
      message.success("收款登记成功");
      setPaymentOpen(false);
      await loadData();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "收款登记失败");
      return false;
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
    { title: "已收金额", dataIndex: "receivedAmount", key: "receivedAmount", width: 120, render: (v) => v ?? 0 },
    {
      title: "未收金额",
      dataIndex: "unreceivedAmount",
      key: "unreceivedAmount",
      width: 120,
      render: (v) => <Text type={v && v > 0 ? "danger" : undefined}>{v ?? 0}</Text>
    },
    { title: "订单数", dataIndex: "orderCount", key: "orderCount", width: 100 },
    { title: "退货数", dataIndex: "returnCount", key: "returnCount", width: 100 },
    {
      title: "操作",
      key: "actions",
      width: 100,
      render: (_, record) => (
        <Button type="link" onClick={() => void openPayment(record)}>
          收款登记
        </Button>
      )
    }
  ];

  const orderColumns: ColumnsType<SaleOrderRecord> = [
    { title: "销售单号", dataIndex: "orderNo", key: "orderNo", width: 180 },
    { title: "状态", dataIndex: "status", key: "status", width: 120 },
    { title: "应收", dataIndex: "payableAmount", key: "payableAmount", width: 120 },
    { title: "已收", dataIndex: "paidAmount", key: "paidAmount", width: 120, render: (v: any) => v ?? 0 },
    {
      title: "收款记录",
      key: "payments",
      render: (_, record) => {
        const payments = orderPayments[record.id] ?? [];
        if (payments.length === 0) return <Text type="secondary">无</Text>;
        return payments.map((p) => (
          <div key={p.id}>
            <Text>{p.receivedAmount}</Text>
            <Text type="secondary" style={{ fontSize: 12, marginLeft: 8 }}>{p.paymentNo}</Text>
          </div>
        ));
      }
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>销售管理 / 应收统计</Title>
          <Text type="secondary">按客户汇总销售订单和退货金额，展示应收净额，支持登记收款。</Text>
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

      <Modal
        title={selectedCustomer ? `${selectedCustomer.customerName} - 收款管理` : "收款管理"}
        open={paymentOpen}
        onCancel={() => setPaymentOpen(false)}
        footer={null}
        width={900}
        destroyOnClose
      >
        {selectedCustomer && (
          <div style={{ marginBottom: 16 }}>
            <Space>
              <Text>应收：<Text strong>{selectedCustomer.netReceivableAmount ?? 0}</Text></Text>
              <Text>已收：<Text strong>{selectedCustomer.receivedAmount ?? 0}</Text></Text>
              <Text>未收：<Text strong type="danger">{selectedCustomer.unreceivedAmount ?? 0}</Text></Text>
            </Space>
          </div>
        )}

        <Table
          rowKey="id"
          columns={orderColumns}
          dataSource={customerOrders}
          pagination={false}
          size="small"
          style={{ marginBottom: 16 }}
        />

        <CreateForm
          title="登记收款"
          open={paymentOpen}
          width={520}
          onCancel={() => setPaymentOpen(false)}
          onFinish={handlePayment}
          sections={[
            {
              title: "收款信息",
              fields: [
                {
                  type: "select",
                  name: "saleOrderId",
                  label: "销售单",
                  rules: [{ required: true, message: "请选择销售单" }],
                  options: customerOrders.map((o) => ({ label: `${o.orderNo}（应收 ${o.payableAmount ?? 0}）`, value: o.id })),
                  colSpan: 24
                },
                {
                  type: "digit",
                  name: "receivedAmount",
                  label: "收款金额",
                  min: 0.01,
                  precision: 2,
                  rules: [{ required: true, message: "请输入收款金额" }],
                  colSpan: 12
                },
                {
                  type: "select",
                  name: "paymentMethod",
                  label: "收款方式",
                  options: paymentMethodOptions,
                  colSpan: 12
                },
                { type: "textarea", name: "remark", label: "备注", colSpan: 24 }
              ]
            }
          ]}
        />
      </Modal>
    </section>
  );
}
