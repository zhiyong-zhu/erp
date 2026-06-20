import { App, Card, Col, Row, Statistic, Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchSaleReportSummary } from "../../../api/sales";
import type { SaleCustomerRank, SaleProductRank, SaleReportSummary } from "../../../types/sales";

const { Title, Text } = Typography;

function money(value?: number | null) {
  return value != null ? `¥${Number(value).toLocaleString()}` : "¥0";
}

function numberText(value?: number | null) {
  return value != null ? Number(value).toLocaleString() : "0";
}

export function SaleReportPage() {
  const [loading, setLoading] = useState(false);
  const [summary, setSummary] = useState<SaleReportSummary | null>(null);
  const { message } = App.useApp();

  useEffect(() => {
    void loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    try {
      setSummary(await fetchSaleReportSummary());
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载销售报表失败");
    } finally {
      setLoading(false);
    }
  }

  const customerColumns: ColumnsType<SaleCustomerRank> = [
    { title: "客户", dataIndex: "customerName", key: "customerName", width: 180 },
    { title: "订单数", dataIndex: "orderCount", key: "orderCount", width: 100 },
    {
      title: "销售额",
      dataIndex: "orderAmount",
      key: "orderAmount",
      width: 130,
      render: money
    },
    {
      title: "退货额",
      dataIndex: "returnAmount",
      key: "returnAmount",
      width: 130,
      render: money
    },
    {
      title: "净销售额",
      dataIndex: "netSalesAmount",
      key: "netSalesAmount",
      width: 140,
      render: money
    }
  ];

  const productColumns: ColumnsType<SaleProductRank> = [
    { title: "SKU", dataIndex: "skuCode", key: "skuCode", width: 140 },
    { title: "产品", dataIndex: "productName", key: "productName", width: 180 },
    {
      title: "销量",
      dataIndex: "quantity",
      key: "quantity",
      width: 120,
      render: numberText
    },
    {
      title: "销售额",
      dataIndex: "salesAmount",
      key: "salesAmount",
      width: 140,
      render: money
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>销售管理 / 销售报表</Title>
          <Text type="secondary">汇总销售额、退货额、净销售额，并展示客户与 SKU 排行。</Text>
        </div>
      </div>

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} md={8}>
          <Card loading={loading}>
            <Statistic title="销售额" value={summary?.orderAmount ?? 0} precision={2} prefix="¥" />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card loading={loading}>
            <Statistic title="退货额" value={summary?.returnAmount ?? 0} precision={2} prefix="¥" />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card loading={loading}>
            <Statistic title="净销售额" value={summary?.netSalesAmount ?? 0} precision={2} prefix="¥" />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card loading={loading}>
            <Statistic title="订单数" value={summary?.orderCount ?? 0} />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card loading={loading}>
            <Statistic title="退货数" value={summary?.returnCount ?? 0} />
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card loading={loading}>
            <Statistic title="成交客户数" value={summary?.customerCount ?? 0} />
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={12}>
          <Card title="客户净销售排行" loading={loading}>
            <Table
              rowKey={(record) => record.customerName ?? "UNKNOWN"}
              columns={customerColumns}
              dataSource={summary?.topCustomers ?? []}
              pagination={false}
              size="small"
            />
          </Card>
        </Col>
        <Col xs={24} xl={12}>
          <Card title="SKU 销售排行" loading={loading}>
            <Table
              rowKey={(record) => record.skuCode ?? record.productName ?? "UNKNOWN"}
              columns={productColumns}
              dataSource={summary?.topProducts ?? []}
              pagination={false}
              size="small"
            />
          </Card>
        </Col>
      </Row>
    </section>
  );
}
