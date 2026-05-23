import { ApartmentOutlined, FileSearchOutlined, LogoutOutlined, ProfileOutlined, SafetyCertificateOutlined, SettingOutlined, TeamOutlined } from "@ant-design/icons";
import { Layout, Menu, Typography, App as AntApp, Button } from "antd";
import { useEffect, useMemo, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { MATERIAL_PERMISSIONS, PRODUCT_PERMISSIONS, SYSTEM_PERMISSIONS } from "@erp/shared";
import { logout } from "../api/auth";
import { clearAuth, getAuthState, hasPermission, subscribeAuth } from "../store/auth";

const { Header, Content, Sider } = Layout;
const { Title, Text } = Typography;

export function AppLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { message } = AntApp.useApp();
  const [, forceUpdate] = useState(0);

  useEffect(() => subscribeAuth(() => forceUpdate((value) => value + 1)), []);

  async function handleLogout() {
    try {
      await logout();
    } finally {
      clearAuth();
      message.success("已退出登录");
      navigate("/login", { replace: true });
    }
  }

  const currentUser = getAuthState().user;
  const menuItems = useMemo(
    () => [
      {
        key: "/system",
        icon: <SettingOutlined />,
        label: "系统管理",
        children: [
          hasPermission(SYSTEM_PERMISSIONS.USER_LIST)
            ? {
                key: "/system/users",
                icon: <TeamOutlined />,
                label: "用户管理",
                onClick: () => navigate("/system/users")
              }
            : null,
          hasPermission(SYSTEM_PERMISSIONS.DEPT_LIST)
            ? {
                key: "/system/departments",
                icon: <ApartmentOutlined />,
                label: "部门管理",
                onClick: () => navigate("/system/departments")
              }
            : null,
          hasPermission(SYSTEM_PERMISSIONS.ROLE_LIST)
            ? {
                key: "/system/roles",
                icon: <SafetyCertificateOutlined />,
                label: "角色管理",
                onClick: () => navigate("/system/roles")
              }
            : null,
          hasPermission(SYSTEM_PERMISSIONS.DICT_LIST)
            ? {
                key: "/system/dict",
                icon: <ProfileOutlined />,
                label: "数据字典",
                onClick: () => navigate("/system/dict")
              }
            : null,
          hasPermission(SYSTEM_PERMISSIONS.LOG_LIST)
            ? {
                key: "/system/logs",
                icon: <FileSearchOutlined />,
                label: "操作日志",
                onClick: () => navigate("/system/logs")
              }
            : null
        ].filter(Boolean)
      },
      {
        key: "/product",
        icon: <ProfileOutlined />,
        label: "产品管理",
        children: [
          hasPermission(PRODUCT_PERMISSIONS.CATEGORY_LIST)
            ? {
                key: "/product/categories",
                icon: <ProfileOutlined />,
                label: "分类管理",
                onClick: () => navigate("/product/categories")
              }
            : null,
          hasPermission(PRODUCT_PERMISSIONS.PRODUCT_LIST)
            ? {
                key: "/product/products",
                icon: <TeamOutlined />,
                label: "产品列表",
                onClick: () => navigate("/product/products")
              }
            : null
        ].filter(Boolean)
      },
      {
        key: "/material",
        icon: <ApartmentOutlined />,
        label: "原料管理",
        children: [
          hasPermission(MATERIAL_PERMISSIONS.CATEGORY_LIST)
            ? {
                key: "/material/categories",
                icon: <ProfileOutlined />,
                label: "分类管理",
                onClick: () => navigate("/material/categories")
              }
            : null,
          hasPermission(MATERIAL_PERMISSIONS.MATERIAL_LIST)
            ? {
                key: "/material/materials",
                icon: <TeamOutlined />,
                label: "原料列表",
                onClick: () => navigate("/material/materials")
              }
            : null,
          hasPermission(MATERIAL_PERMISSIONS.SUPPLIER_LIST)
            ? {
                key: "/material/suppliers",
                icon: <SafetyCertificateOutlined />,
                label: "供应商管理",
                onClick: () => navigate("/material/suppliers")
              }
            : null
        ].filter(Boolean)
      }
    ].filter((item) => Array.isArray(item.children) && item.children.length > 0),
    [navigate, currentUser]
  );

  let selectedKeys = [location.pathname];
  if (location.pathname.startsWith("/system/roles/")) {
    selectedKeys = ["/system/roles"];
  } else if (location.pathname.startsWith("/system/dict")) {
    selectedKeys = ["/system/dict"];
  } else if (location.pathname.startsWith("/system/logs")) {
    selectedKeys = ["/system/logs"];
  } else if (location.pathname.startsWith("/product/categories")) {
    selectedKeys = ["/product/categories"];
  } else if (location.pathname.startsWith("/product/products")) {
    selectedKeys = ["/product/products"];
  } else if (location.pathname.startsWith("/material/categories")) {
    selectedKeys = ["/material/categories"];
  } else if (location.pathname.startsWith("/material/materials")) {
    selectedKeys = ["/material/materials"];
  } else if (location.pathname.startsWith("/material/suppliers")) {
    selectedKeys = ["/material/suppliers"];
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
          defaultOpenKeys={["/system"]}
          selectedKeys={selectedKeys}
          items={menuItems}
        />
      </Sider>
      <Layout>
        <Header className="erp-header erp-header-between">
          <div>
            <Title level={4} style={{ margin: 0 }}>ERP 管理后台</Title>
            <Text type="secondary">
              {currentUser ? `${currentUser.realName || currentUser.username} 已登录，当前开放系统管理模块。` : "登录与用户管理功能已接通"}
            </Text>
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
