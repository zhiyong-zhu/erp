import { Alert, Card, Col, Row, Statistic, Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchInventoryBalances } from "../../api/inventory";
import { fetchProductionProductStock, fetchProductionReports } from "../../api/production";
import { fetchSaleReportSummary } from "../../api/sales";
import type { InventoryBalanceRecord } from "../../types/inventory";
import type { ProductionProductStockRecord, ProductionReportRecord } from "../../types/production";
import type { SaleReportSummary } from "../../types/sales";

const { Title, Text } = Typography;

export function DashboardPage() {
  const [loading, setLoading] = useState(false);
  const [warnings, setWarnings] = useState<string[]>([]);
  const [saleSummary, setSaleSummary] = useState<SaleReportSummary | null>(null);
  const [inventoryBalances, setInventoryBalances] = useState<InventoryBalanceRecord[]>([]);
  const [productionReports, setProductionReports] = useState<ProductionReportRecord[]>([]);
  const [productStocks, setProductStocks] = useState<ProductionProductStockRecord[]>([]);

  useEffect(() => {
    void loadDashboard();
  }, []);

  async function loadDashboard() {
    setLoading(true);
    const nextWarnings: string[] = [];
    const [salesResult, inventoryResult, reportResult, stockResult] = await Promise.allSettled([
      fetchSaleReportSummary(),
      fetchInventoryBalances({ pageNum: 1, pageSize: 5 }),
      fetchProductionReports({ pageNum: 1, pageSize: 5 }),
      fetchProductionProductStock({ pageNum: 1, pageSize: 5 })
    ]);

    if (salesResult.status === "fulfilled") {
      setSaleSummary(salesResult.value);
    } else {
      nextWarnings.push("销售报表暂无权限或接口不可用");
    }
    if (inventoryResult.status === "fulfilled") {
      setInventoryBalances(inventoryResult.value.records);
    } else {
      nextWarnings.push("库存余额暂无权限或接口不可用");
    }
    if (reportResult.status === "fulfilled") {
      setProductionReports(reportResult.value.records);
    } else {
      nextWarnings.push("生产报工暂无权限或接口不可用");
    }
    if (stockResult.status === "fulfilled") {
      setProductStocks(stockResult.value.records);
    } else {
      nextWarnings.push("成品库存暂无权限或接口不可用");
    }
    setWarnings(nextWarnings);
    setLoading(false);
  }

  const inventoryColumns: ColumnsType<InventoryBalanceRecord> = [
    { title: "原料", dataIndex: "materialName", key: "materialName" },
    { title: "仓库", dataIndex: "warehouseName", key: "warehouseName", width: 140 },
    { title: "库位", dataIndex: "locationName", key: "locationName", width: 140 },
    { title: "可用", dataIndex: "availableQuantity", key: "availableQuantity", width: 100 }
  ];

  const reportColumns: ColumnsType<ProductionReportRecord> = [
    { title: "批次", dataIndex: "batchNo", key: "batchNo", width: 150 },
    { title: "产品", dataIndex: "productName", key: "productName" },
    { title: "良品", dataIndex: "goodQuantity", key: "goodQuantity", width: 90 },
    { title: "不良", dataIndex: "defectQuantity", key: "defectQuantity", width: 90 }
  ];

  const stockColumns: ColumnsType<ProductionProductStockRecord> = [
    { title: "产品", dataIndex: "productName", key: "productName" },
    { title: "库存", dataIndex: "currentStock", key: "currentStock", width: 100 }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>运营 Dashboard</Title>
          <Text type="secondary">聚合销售、库存、生产执行的基础指标，作为 Web 后台入口。</Text>
        </div>
      </div>

      {warnings.length > 0 ? (
        <Alert type="warning" showIcon style={{ marginBottom: 16 }} message="部分数据未加载" description={warnings.join("；")} />
      ) : null}

      <Row gutter={[16, 16]} style={{ marginBottom: 16 }}>
        <Col xs={24} md={6}>
          <Card loading={loading}><Statistic title="销售额" value={saleSummary?.orderAmount ?? 0} precision={2} /></Card>
        </Col>
        <Col xs={24} md={6}>
          <Card loading={loading}><Statistic title="退货额" value={saleSummary?.returnAmount ?? 0} precision={2} /></Card>
        </Col>
        <Col xs={24} md={6}>
          <Card loading={loading}><Statistic title="净销售额" value={saleSummary?.netSalesAmount ?? 0} precision={2} /></Card>
        </Col>
        <Col xs={24} md={6}>
          <Card loading={loading}><Statistic title="订单数" value={saleSummary?.orderCount ?? 0} /></Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={10}>
          <Card title="库存余额 Top 5" loading={loading}>
            <Table rowKey="id" columns={inventoryColumns} dataSource={inventoryBalances} pagination={false} size="small" />
          </Card>
        </Col>
        <Col xs={24} xl={8}>
          <Card title="最近报工" loading={loading}>
            <Table rowKey="id" columns={reportColumns} dataSource={productionReports} pagination={false} size="small" />
          </Card>
        </Col>
        <Col xs={24} xl={6}>
          <Card title="成品库存" loading={loading}>
            <Table rowKey="id" columns={stockColumns} dataSource={productStocks} pagination={false} size="small" />
          </Card>
        </Col>
      </Row>
    </section>
  );
}
