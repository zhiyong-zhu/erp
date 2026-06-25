import {
  ApartmentOutlined,
  CarOutlined,
  FileSearchOutlined,
  HomeOutlined,
  LogoutOutlined,
  ProfileOutlined,
  SafetyCertificateOutlined,
  SettingOutlined,
  ShoppingOutlined,
  TeamOutlined,
  ToolOutlined,
  WarningOutlined,
  WalletOutlined
} from "@ant-design/icons";
import { Layout, Menu, Typography, App as AntApp, Button } from "antd";
import { useEffect, useMemo, useState } from "react";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { INVENTORY_PERMISSIONS, MATERIAL_PERMISSIONS, PRODUCT_PERMISSIONS, PRODUCTION_PERMISSIONS, PURCHASE_PERMISSIONS, SALES_PERMISSIONS, SYSTEM_PERMISSIONS } from "@erp/shared";
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
        key: "/dashboard",
        icon: <HomeOutlined />,
        label: "运营首页",
        onClick: () => navigate("/dashboard")
      },
      {
        key: "/system",
        icon: <SettingOutlined />,
        label: "系统管理",
        children: [
          hasPermission(SYSTEM_PERMISSIONS.USER_LIST)
            ? { key: "/system/users", icon: <TeamOutlined />, label: "用户管理", onClick: () => navigate("/system/users") }
            : null,
          hasPermission(SYSTEM_PERMISSIONS.DEPT_LIST)
            ? { key: "/system/departments", icon: <ApartmentOutlined />, label: "部门管理", onClick: () => navigate("/system/departments") }
            : null,
          hasPermission(SYSTEM_PERMISSIONS.ROLE_LIST)
            ? { key: "/system/roles", icon: <SafetyCertificateOutlined />, label: "角色管理", onClick: () => navigate("/system/roles") }
            : null,
          hasPermission(SYSTEM_PERMISSIONS.DICT_LIST)
            ? { key: "/system/dict", icon: <ProfileOutlined />, label: "数据字典", onClick: () => navigate("/system/dict") }
            : null,
          hasPermission(SYSTEM_PERMISSIONS.LOG_LIST)
            ? { key: "/system/logs", icon: <FileSearchOutlined />, label: "操作日志", onClick: () => navigate("/system/logs") }
            : null
        ].filter(Boolean)
      },
      {
        key: "/product",
        icon: <ProfileOutlined />,
        label: "产品管理",
        children: [
          hasPermission(PRODUCT_PERMISSIONS.CATEGORY_LIST)
            ? { key: "/product/categories", icon: <ProfileOutlined />, label: "分类管理", onClick: () => navigate("/product/categories") }
            : null,
          hasPermission(PRODUCT_PERMISSIONS.PRODUCT_LIST)
            ? { key: "/product/products", icon: <TeamOutlined />, label: "产品列表", onClick: () => navigate("/product/products") }
            : null
        ].filter(Boolean)
      },
      {
        key: "/production",
        icon: <SettingOutlined />,
        label: "生产管理",
        children: [
          hasPermission(PRODUCTION_PERMISSIONS.PROCESS_LIST)
            ? { key: "/production/processes", icon: <ProfileOutlined />, label: "工艺路线", onClick: () => navigate("/production/processes") }
            : null,
          hasPermission(PRODUCTION_PERMISSIONS.BOM_LIST)
            ? { key: "/production/boms", icon: <ProfileOutlined />, label: "生产BOM", onClick: () => navigate("/production/boms") }
            : null,
          hasPermission(PRODUCTION_PERMISSIONS.BATCH_LIST)
            ? { key: "/production/batches", icon: <ApartmentOutlined />, label: "生产工单", onClick: () => navigate("/production/batches") }
            : null,
          hasPermission(PRODUCTION_PERMISSIONS.REPORT_LIST)
            ? { key: "/production/reports", icon: <ToolOutlined />, label: "生产执行", onClick: () => navigate("/production/reports") }
            : null,
          hasPermission(PRODUCTION_PERMISSIONS.SERIAL_LIST)
            ? { key: "/production/serials", icon: <FileSearchOutlined />, label: "序列号追溯", onClick: () => navigate("/production/serials") }
            : null
        ].filter(Boolean)
      },
      {
        key: "/purchase",
        icon: <ProfileOutlined />,
        label: "采购管理",
        children: [
          hasPermission(PURCHASE_PERMISSIONS.ORDER_LIST)
            ? { key: "/purchase/orders", icon: <ProfileOutlined />, label: "采购单", onClick: () => navigate("/purchase/orders") }
            : null,
          hasPermission(PURCHASE_PERMISSIONS.PAYABLE_LIST)
            ? { key: "/purchase/payables", icon: <ProfileOutlined />, label: "应付统计", onClick: () => navigate("/purchase/payables") }
            : null,
          hasPermission(PURCHASE_PERMISSIONS.EXCEPTION_LIST)
            ? { key: "/purchase/exceptions", icon: <FileSearchOutlined />, label: "采购异常", onClick: () => navigate("/purchase/exceptions") }
            : null
        ].filter(Boolean)
      },
      {
        key: "/material",
        icon: <ApartmentOutlined />,
        label: "原料管理",
        children: [
          hasPermission(MATERIAL_PERMISSIONS.CATEGORY_LIST)
            ? { key: "/material/categories", icon: <ProfileOutlined />, label: "分类管理", onClick: () => navigate("/material/categories") }
            : null,
          hasPermission(MATERIAL_PERMISSIONS.MATERIAL_LIST)
            ? { key: "/material/materials", icon: <TeamOutlined />, label: "原料列表", onClick: () => navigate("/material/materials") }
            : null,
          hasPermission(INVENTORY_PERMISSIONS.TRANSACTION_LIST)
            ? { key: "/material/inventory", icon: <FileSearchOutlined />, label: "库存台账", onClick: () => navigate("/material/inventory") }
            : null,
          hasPermission(MATERIAL_PERMISSIONS.MATERIAL_LIST)
            ? { key: "/material/safety-stock", icon: <SafetyCertificateOutlined />, label: "安全库存设置", onClick: () => navigate("/material/safety-stock") }
            : null,
          hasPermission(MATERIAL_PERMISSIONS.ALERT_LIST)
            ? { key: "/material/alerts", icon: <FileSearchOutlined />, label: "安全库存预警", onClick: () => navigate("/material/alerts") }
            : null,
          hasPermission(MATERIAL_PERMISSIONS.REPLENISH_LIST)
            ? { key: "/material/replenishment", icon: <ProfileOutlined />, label: "补货建议", onClick: () => navigate("/material/replenishment") }
            : null,
          hasPermission(MATERIAL_PERMISSIONS.QUOTE_LIST)
            ? { key: "/material/quotes", icon: <ProfileOutlined />, label: "供应商报价", onClick: () => navigate("/material/quotes") }
            : null,
          hasPermission(MATERIAL_PERMISSIONS.SUPPLIER_LIST)
            ? { key: "/material/suppliers", icon: <SafetyCertificateOutlined />, label: "供应商管理", onClick: () => navigate("/material/suppliers") }
            : null
        ].filter(Boolean)
      },
      {
        key: "/inventory",
        icon: <FileSearchOutlined />,
        label: "库存管理",
        children: [
          hasPermission(INVENTORY_PERMISSIONS.WAREHOUSE_LIST)
            ? { key: "/inventory/warehouses", icon: <ProfileOutlined />, label: "仓库管理", onClick: () => navigate("/inventory/warehouses") }
            : null,
          hasPermission(INVENTORY_PERMISSIONS.LOCATION_LIST)
            ? { key: "/inventory/locations", icon: <ProfileOutlined />, label: "库位管理", onClick: () => navigate("/inventory/locations") }
            : null,
          hasPermission(INVENTORY_PERMISSIONS.RECEIPT_LIST)
            ? { key: "/inventory/receipts", icon: <ProfileOutlined />, label: "正式入库单", onClick: () => navigate("/inventory/receipts") }
            : null,
          hasPermission(INVENTORY_PERMISSIONS.ISSUE_LIST)
            ? { key: "/inventory/issues", icon: <ProfileOutlined />, label: "出库管理", onClick: () => navigate("/inventory/issues") }
            : null,
          hasPermission(INVENTORY_PERMISSIONS.TRANSFER_LIST)
            ? { key: "/inventory/transfers", icon: <ProfileOutlined />, label: "调拨管理", onClick: () => navigate("/inventory/transfers") }
            : null,
          hasPermission(INVENTORY_PERMISSIONS.CHECK_LIST)
            ? { key: "/inventory/checks", icon: <ProfileOutlined />, label: "盘点管理", onClick: () => navigate("/inventory/checks") }
            : null,
          hasPermission(INVENTORY_PERMISSIONS.TRANSACTION_LIST)
            ? { key: "/inventory/transactions", icon: <ProfileOutlined />, label: "库存流水", onClick: () => navigate("/inventory/transactions") }
            : null
        ].filter(Boolean)
      },
      {
        key: "/sales",
        icon: <ShoppingOutlined />,
        label: "销售管理",
        children: [
          hasPermission(SALES_PERMISSIONS.CUSTOMER_LIST)
            ? { key: "/sales/customers", icon: <TeamOutlined />, label: "客户管理", onClick: () => navigate("/sales/customers") }
            : null,
          hasPermission(SALES_PERMISSIONS.ORDER_LIST)
            ? { key: "/sales/orders", icon: <ProfileOutlined />, label: "销售订单", onClick: () => navigate("/sales/orders") }
            : null,
          hasPermission(SALES_PERMISSIONS.RETURN_LIST)
            ? { key: "/sales/returns", icon: <ProfileOutlined />, label: "销售退货", onClick: () => navigate("/sales/returns") }
            : null,
          hasPermission(SALES_PERMISSIONS.SHIPPING_LIST)
            ? { key: "/sales/shipping", icon: <CarOutlined />, label: "发货管理", onClick: () => navigate("/sales/shipping") }
            : null,
          hasPermission(SALES_PERMISSIONS.RECEIVABLE_LIST)
            ? { key: "/sales/receivables", icon: <WalletOutlined />, label: "应收统计", onClick: () => navigate("/sales/receivables") }
            : null,
          hasPermission(SALES_PERMISSIONS.REPORT_LIST)
            ? { key: "/sales/reports", icon: <FileSearchOutlined />, label: "销售报表", onClick: () => navigate("/sales/reports") }
            : null,
          hasPermission(SALES_PERMISSIONS.EXCEPTION_LIST)
            ? { key: "/sales/exceptions", icon: <WarningOutlined />, label: "销售异常", onClick: () => navigate("/sales/exceptions") }
            : null,
          hasPermission(SALES_PERMISSIONS.ORDER_LIST)
            ? { key: "/sales/ecommerce", icon: <ShoppingOutlined />, label: "电商平台", onClick: () => navigate("/sales/ecommerce") }
            : null
        ].filter(Boolean)
      }
    ].filter((item) => !("children" in item) || (Array.isArray(item.children) && item.children.length > 0)),
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
  } else if (location.pathname.startsWith("/production/processes")) {
    selectedKeys = ["/production/processes"];
  } else if (location.pathname.startsWith("/production/boms")) {
    selectedKeys = ["/production/boms"];
  } else if (location.pathname.startsWith("/production/batches")) {
    selectedKeys = ["/production/batches"];
  } else if (location.pathname.startsWith("/production/reports")) {
    selectedKeys = ["/production/reports"];
  } else if (location.pathname.startsWith("/production/serials")) {
    selectedKeys = ["/production/serials"];
  } else if (location.pathname.startsWith("/purchase/orders")) {
    selectedKeys = ["/purchase/orders"];
  } else if (location.pathname.startsWith("/purchase/payables")) {
    selectedKeys = ["/purchase/payables"];
  } else if (location.pathname.startsWith("/purchase/exceptions")) {
    selectedKeys = ["/purchase/exceptions"];
  } else if (location.pathname.startsWith("/material/categories")) {
    selectedKeys = ["/material/categories"];
  } else if (location.pathname.startsWith("/material/materials")) {
    selectedKeys = ["/material/materials"];
  } else if (location.pathname.startsWith("/material/inventory")) {
    selectedKeys = ["/material/inventory"];
  } else if (location.pathname.startsWith("/material/safety-stock")) {
    selectedKeys = ["/material/safety-stock"];
  } else if (location.pathname.startsWith("/material/alerts")) {
    selectedKeys = ["/material/alerts"];
  } else if (location.pathname.startsWith("/material/replenishment")) {
    selectedKeys = ["/material/replenishment"];
  } else if (location.pathname.startsWith("/material/quotes")) {
    selectedKeys = ["/material/quotes"];
  } else if (location.pathname.startsWith("/material/suppliers")) {
    selectedKeys = ["/material/suppliers"];
  } else if (location.pathname.startsWith("/inventory/receipts")) {
    selectedKeys = ["/inventory/receipts"];
  } else if (location.pathname.startsWith("/inventory/issues")) {
    selectedKeys = ["/inventory/issues"];
  } else if (location.pathname.startsWith("/inventory/transfers")) {
    selectedKeys = ["/inventory/transfers"];
  } else if (location.pathname.startsWith("/inventory/checks")) {
    selectedKeys = ["/inventory/checks"];
  } else if (location.pathname.startsWith("/inventory/transactions")) {
    selectedKeys = ["/inventory/transactions"];
  } else if (location.pathname.startsWith("/sales/customers")) {
    selectedKeys = ["/sales/customers"];
  } else if (location.pathname.startsWith("/sales/orders")) {
    selectedKeys = ["/sales/orders"];
  } else if (location.pathname.startsWith("/sales/returns")) {
    selectedKeys = ["/sales/returns"];
  } else if (location.pathname.startsWith("/sales/shipping")) {
    selectedKeys = ["/sales/shipping"];
  } else if (location.pathname.startsWith("/sales/receivables")) {
    selectedKeys = ["/sales/receivables"];
  } else if (location.pathname.startsWith("/sales/reports")) {
    selectedKeys = ["/sales/reports"];
  } else if (location.pathname.startsWith("/sales/exceptions")) {
    selectedKeys = ["/sales/exceptions"];
  } else if (location.pathname.startsWith("/sales/ecommerce")) {
    selectedKeys = ["/sales/ecommerce"];
  } else if (location.pathname.startsWith("/dashboard")) {
    selectedKeys = ["/dashboard"];
  }

  return (
    <Layout className="erp-app-shell">
      <Sider breakpoint="lg" collapsedWidth={72} width={240} theme="light" className="erp-sider">
        <div className="erp-brand">
          <div className="erp-brand-mark">ERP</div>
          <div className="erp-brand-copy">
            <Title level={5}>全渠道 ERP</Title>
            <Text type="secondary">Management Console</Text>
          </div>
        </div>
        <Menu className="erp-menu-scroll" mode="inline" defaultOpenKeys={["/system"]} selectedKeys={selectedKeys} items={menuItems} />
      </Sider>
      <Layout className="erp-main-shell">
        <Header className="erp-header erp-header-between">
          <div>
            <Title level={4} style={{ margin: 0 }}>ERP 管理后台</Title>
            <Text type="secondary">
              {currentUser ? `${currentUser.realName || currentUser.username} 已登录` : "请先登录系统"}
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
