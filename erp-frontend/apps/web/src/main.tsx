import React from "react";
import ReactDOM from "react-dom/client";
import { ConfigProvider, Card, Col, Layout, Menu, Row, Statistic, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { AppstoreOutlined, DatabaseOutlined, DollarOutlined, ShoppingCartOutlined } from "@ant-design/icons";
import { PageContainer, ProCard } from "@ant-design/pro-components";
import { APP_NAME, formatTitle } from "@erp/shared";
import "antd/dist/reset.css";
import "./styles.css";

document.title = formatTitle(APP_NAME);

const { Header, Content, Sider } = Layout;
const { Title, Text } = Typography;

interface TodoItem {
  key: string;
  module: string;
  title: string;
  status: "待处理" | "处理中" | "已完成";
}

const todoColumns: ColumnsType<TodoItem> = [
  { title: "模块", dataIndex: "module", key: "module" },
  { title: "事项", dataIndex: "title", key: "title" },
  {
    title: "状态",
    dataIndex: "status",
    key: "status",
    render: (value: TodoItem["status"]) => {
      const colorMap: Record<TodoItem["status"], string> = {
        待处理: "gold",
        处理中: "blue",
        已完成: "green"
      };
      return <Tag color={colorMap[value]}>{value}</Tag>;
    }
  }
];

const todoData: TodoItem[] = [
  { key: "1", module: "采购", title: "采购单待审批 3 条", status: "待处理" },
  { key: "2", module: "库存", title: "库存预警 5 条", status: "处理中" },
  { key: "3", module: "财务", title: "本月税额汇总待确认", status: "待处理" }
];

function App() {
  return (
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: "#0f766e",
          borderRadius: 14,
          colorBgLayout: "#eef4f8"
        }
      }}
    >
      <Layout style={{ minHeight: "100vh" }}>
        <Sider breakpoint="lg" collapsedWidth={72} width={240} theme="light" className="erp-sider">
          <div className="erp-brand">
            <div className="erp-brand-mark">ERP</div>
            <div className="erp-brand-copy">
              <Title level={5}>{APP_NAME}</Title>
              <Text type="secondary">Management Console</Text>
            </div>
          </div>
          <Menu
            mode="inline"
            defaultSelectedKeys={["dashboard"]}
            items={[
              { key: "dashboard", icon: <AppstoreOutlined />, label: "仪表盘" },
              { key: "product", icon: <DatabaseOutlined />, label: "产品管理" },
              { key: "sales", icon: <ShoppingCartOutlined />, label: "销售管理" },
              { key: "finance", icon: <DollarOutlined />, label: "财务管理" }
            ]}
          />
        </Sider>
        <Layout>
          <Header className="erp-header">
            <div>
              <Title level={4} style={{ margin: 0 }}>全渠道 ERP 后台骨架</Title>
              <Text type="secondary">Ant Design + ProComponents 已接入，可继续扩展路由与业务模块</Text>
            </div>
          </Header>
          <Content className="erp-content">
            <PageContainer
              header={{
                title: "经营驾驶舱",
                subTitle: "先搭好后台通用壳，再逐个落产品、库存、采购、财务模块"
              }}
            >
              <Row gutter={[16, 16]}>
                <Col xs={24} md={12} xl={6}>
                  <Card>
                    <Statistic title="本月营收" value={1250000} precision={2} suffix="CNY" />
                  </Card>
                </Col>
                <Col xs={24} md={12} xl={6}>
                  <Card>
                    <Statistic title="库存金额" value={2850000} precision={2} suffix="CNY" />
                  </Card>
                </Col>
                <Col xs={24} md={12} xl={6}>
                  <Card>
                    <Statistic title="待审批事项" value={8} suffix="条" />
                  </Card>
                </Col>
                <Col xs={24} md={12} xl={6}>
                  <Card>
                    <Statistic title="电商同步成功率" value={99.2} precision={1} suffix="%" />
                  </Card>
                </Col>
              </Row>

              <Row gutter={[16, 16]} style={{ marginTop: 8 }}>
                <Col xs={24} xl={16}>
                  <ProCard title="待处理事项" bordered>
                    <Table columns={todoColumns} dataSource={todoData} pagination={false} />
                  </ProCard>
                </Col>
                <Col xs={24} xl={8}>
                  <ProCard title="模块接入进度" bordered direction="column" gutter={16}>
                    <Card size="small">
                      <Title level={5}>系统管理</Title>
                      <Text type="secondary">下一步接入用户、角色、权限、字典、日志页面。</Text>
                    </Card>
                    <Card size="small">
                      <Title level={5}>业务主链路</Title>
                      <Text type="secondary">产品、库存、采购、销售、财务页面可逐个替换为真实模块。</Text>
                    </Card>
                    <Card size="small">
                      <Title level={5}>桌面端复用</Title>
                      <Text type="secondary">这套 React 页面可直接给 Tauri 桌面端复用。</Text>
                    </Card>
                  </ProCard>
                </Col>
              </Row>
            </PageContainer>
          </Content>
        </Layout>
      </Layout>
    </ConfigProvider>
  );
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
