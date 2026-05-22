import { LogoutOutlined, TeamOutlined } from "@ant-design/icons";
import { Layout, Menu, Typography, App as AntApp, Button } from "antd";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { logout } from "../api/auth";
import { saveAccessToken, saveUser } from "../store/auth";

const { Header, Content, Sider } = Layout;
const { Title, Text } = Typography;

export function AppLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { message } = AntApp.useApp();

  async function handleLogout() {
    try {
      await logout();
    } finally {
      saveAccessToken(null);
      saveUser(null);
      message.success("已退出登录");
      navigate("/login", { replace: true });
    }
  }

  return (
    <Layout style={{ minHeight: "100vh" }}>
      <Sider breakpoint="lg" collapsedWidth={72} width={240} theme="light" className="erp-sider">
        <div className="erp-brand">
          <div className="erp-brand-mark">ERP</div>
          <div className="erp-brand-copy">
            <Title level={5}>全渠道 ERP</Title>
            <Text type="secondary">Management Console</Text>
          </div>
        </div>
        <Menu
          mode="inline"
          selectedKeys={[location.pathname]}
          items={[
            {
              key: "/system/users",
              icon: <TeamOutlined />,
              label: "系统管理 / 用户管理",
              onClick: () => navigate("/system/users")
            }
          ]}
        />
      </Sider>
      <Layout>
        <Header className="erp-header erp-header-between">
          <div>
            <Title level={4} style={{ margin: 0 }}>ERP 管理后台</Title>
            <Text type="secondary">登录与用户管理功能已接通</Text>
          </div>
          <Button icon={<LogoutOutlined />} onClick={() => void handleLogout()}>
            退出登录
          </Button>
        </Header>
        <Content className="erp-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
