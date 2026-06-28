import { App, Button, Modal, Space, Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchPurchaseOrders, fetchPurchasePayables, createPurchasePayment, fetchPurchasePayments } from "../../../api/purchase";
import type {
  PurchasePayableStatRecord,
  PurchasePaymentRecord,
  PurchaseOrderRecord
} from "../../../types/purchase";
import { CreateForm } from "../../../components/CreateForm";

const { Title, Text } = Typography;

const paymentMethodOptions = [
  { label: "银行转账", value: "BANK_TRANSFER" },
  { label: "现金", value: "CASH" },
  { label: "支票", value: "CHECK" },
  { label: "其他", value: "OTHER" }
];

export function PurchasePayablePage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<PurchasePayableStatRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [paymentOpen, setPaymentOpen] = useState(false);
  const [selectedSupplier, setSelectedSupplier] = useState<PurchasePayableStatRecord | null>(null);
  const [supplierOrders, setSupplierOrders] = useState<PurchaseOrderRecord[]>([]);
  const [orderPayments, setOrderPayments] = useState<Record<string, PurchasePaymentRecord[]>>({});
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

  async function openPayment(stat: PurchasePayableStatRecord) {
    setSelectedSupplier(stat);
    setPaymentOpen(true);
    try {
      const orderData = await fetchPurchaseOrders({ pageNum: 1, pageSize: 100 });
      const orders = orderData.records.filter(
        (o) => o.supplierId === stat.supplierId && !["DRAFT", "CANCELLED", "REJECTED"].includes(o.status)
      );
      setSupplierOrders(orders);

      const paymentPromises = orders.map((o) =>
        fetchPurchasePayments({ pageNum: 1, pageSize: 50, purchaseOrderId: o.id })
          .then((res) => [o.id, res.records] as const)
          .catch(() => [o.id, [] as PurchasePaymentRecord[]] as const)
      );
      const results = await Promise.all(paymentPromises);
      const map: Record<string, PurchasePaymentRecord[]> = {};
      for (const [orderId, payments] of results) {
        map[orderId] = payments;
      }
      setOrderPayments(map);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载采购单失败");
    }
  }

  async function handlePayment(values: any) {
    const orderId = values.purchaseOrderId;
    if (!orderId) {
      message.error("请选择采购单");
      return false;
    }
    try {
      await createPurchasePayment(orderId, {
        paidAmount: values.paidAmount,
        paymentMethod: values.paymentMethod,
        remark: values.remark
      });
      message.success("付款登记成功");
      setPaymentOpen(false);
      await loadData();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "付款登记失败");
      return false;
    }
  }

  const columns: ColumnsType<PurchasePayableStatRecord> = [
    { title: "供应商", dataIndex: "supplierName", key: "supplierName", width: 200 },
    { title: "采购单数", dataIndex: "orderCount", key: "orderCount", width: 100 },
    { title: "退货单数", dataIndex: "returnCount", key: "returnCount", width: 100 },
    { title: "采购总金额", dataIndex: "orderAmount", key: "orderAmount", width: 140 },
    { title: "退货总金额", dataIndex: "returnAmount", key: "returnAmount", width: 140 },
    { title: "净应付金额", dataIndex: "netPayableAmount", key: "netPayableAmount", width: 140 },
    { title: "已付金额", dataIndex: "paidAmount", key: "paidAmount", width: 120, render: (v) => v ?? 0 },
    {
      title: "未付金额",
      dataIndex: "unpaidAmount",
      key: "unpaidAmount",
      width: 120,
      render: (v) => <Text type={v && v > 0 ? "danger" : undefined}>{v ?? 0}</Text>
    },
    {
      title: "操作",
      key: "actions",
      width: 100,
      render: (_, record) => (
        <Button type="link" onClick={() => void openPayment(record)}>
          付款登记
        </Button>
      )
    }
  ];

  const orderColumns: ColumnsType<PurchaseOrderRecord> = [
    { title: "采购单号", dataIndex: "orderNo", key: "orderNo", width: 180 },
    { title: "状态", dataIndex: "status", key: "status", width: 120 },
    { title: "总金额", dataIndex: "totalAmount", key: "totalAmount", width: 120 },
    { title: "已付", dataIndex: "paidAmount", key: "paidAmount", width: 120, render: (v: any) => v ?? 0 },
    {
      title: "付款记录",
      key: "payments",
      render: (_, record) => {
        const payments = orderPayments[record.id] ?? [];
        if (payments.length === 0) return <Text type="secondary">无</Text>;
        return payments.map((p) => (
          <div key={p.id}>
            <Text>{p.paidAmount}</Text>
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
          <Title level={3} style={{ margin: 0 }}>采购管理 / 应付统计</Title>
          <Text type="secondary">按供应商汇总采购金额、退货金额和净应付金额，支持登记付款。</Text>
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

      <Modal
        title={selectedSupplier ? `${selectedSupplier.supplierName} - 付款管理` : "付款管理"}
        open={paymentOpen}
        onCancel={() => setPaymentOpen(false)}
        footer={null}
        width={900}
        destroyOnClose
      >
        {selectedSupplier && (
          <div style={{ marginBottom: 16 }}>
            <Space>
              <Text>净应付：<Text strong>{selectedSupplier.netPayableAmount ?? 0}</Text></Text>
              <Text>已付：<Text strong>{selectedSupplier.paidAmount ?? 0}</Text></Text>
              <Text>未付：<Text strong type="danger">{selectedSupplier.unpaidAmount ?? 0}</Text></Text>
            </Space>
          </div>
        )}

        <Table
          rowKey="id"
          columns={orderColumns}
          dataSource={supplierOrders}
          pagination={false}
          size="small"
          style={{ marginBottom: 16 }}
        />

        <CreateForm
          title="登记付款"
          open={paymentOpen}
          width={520}
          onCancel={() => setPaymentOpen(false)}
          onFinish={handlePayment}
          sections={[
            {
              title: "付款信息",
              fields: [
                {
                  type: "select",
                  name: "purchaseOrderId",
                  label: "采购单",
                  rules: [{ required: true, message: "请选择采购单" }],
                  options: supplierOrders.map((o) => ({ label: `${o.orderNo}（总金额 ${o.totalAmount ?? 0}）`, value: o.id })),
                  colSpan: 24
                },
                {
                  type: "digit",
                  name: "paidAmount",
                  label: "付款金额",
                  min: 0.01,
                  precision: 2,
                  rules: [{ required: true, message: "请输入付款金额" }],
                  colSpan: 12
                },
                {
                  type: "select",
                  name: "paymentMethod",
                  label: "付款方式",
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
