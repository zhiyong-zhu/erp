import { App as AntApp } from "antd";
import { useEffect, useState } from "react";
import { Navigate, Route, BrowserRouter as Router, Routes } from "react-router-dom";
import { MATERIAL_PERMISSIONS, PRODUCT_PERMISSIONS, PRODUCTION_PERMISSIONS, PURCHASE_PERMISSIONS, SALES_PERMISSIONS, SYSTEM_PERMISSIONS } from "@erp/shared";
import { fetchUserInfo, refreshTokens } from "../api/auth";
import { PermissionGuard, RequireAuth } from "../components/AuthGuard";
import { AppLayout } from "../layouts/AppLayout";
import { InventoryCheckPage } from "../pages/inventory/checks/InventoryCheckPage";
import { InventoryIssuePage } from "../pages/inventory/issues/InventoryIssuePage";
import { InventoryReceiptPage } from "../pages/inventory/receipts/InventoryReceiptPage";
import { InventoryTransactionPage } from "../pages/inventory/transactions/InventoryTransactionPage";
import { InventoryTransferPage } from "../pages/inventory/transfers/InventoryTransferPage";
import { LoginPage } from "../pages/login/LoginPage";
import { MaterialAlertPage } from "../pages/material/alerts/MaterialAlertPage";
import { MaterialCategoryPage } from "../pages/material/categories/MaterialCategoryPage";
import { MaterialInventoryPage } from "../pages/material/inventory/MaterialInventoryPage";
import { MaterialManagementPage } from "../pages/material/materials/MaterialManagementPage";
import { SupplierQuotePage } from "../pages/material/quotes/SupplierQuotePage";
import { MaterialReplenishmentPage } from "../pages/material/replenishment/MaterialReplenishmentPage";
import { MaterialSafetyStockPage } from "../pages/material/safety-stock/MaterialSafetyStockPage";
import { SupplierManagementPage } from "../pages/material/suppliers/SupplierManagementPage";
import { ProductCategoryPage } from "../pages/product/categories/ProductCategoryPage";
import { ProductManagementPage } from "../pages/product/products/ProductManagementPage";
import { ProductionBatchPage } from "../pages/production/batches/ProductionBatchPage";
import { ProductionBomPage } from "../pages/production/boms/ProductionBomPage";
import { ProductionProcessPage } from "../pages/production/processes/ProductionProcessPage";
import { ProductionReportPage } from "../pages/production/reports/ProductionReportPage";
import { SerialNumberPage } from "../pages/production/serials/SerialNumberPage";
import { PurchaseExceptionPage } from "../pages/purchase/exceptions/PurchaseExceptionPage";
import { PurchasePayablePage } from "../pages/purchase/payables/PurchasePayablePage";
import { PurchaseOrderPage } from "../pages/purchase/orders/PurchaseOrderPage";
import { DepartmentManagementPage } from "../pages/system/departments/DepartmentManagementPage";
import { DictManagementPage } from "../pages/system/dicts/DictManagementPage";
import { OperationLogPage } from "../pages/system/logs/OperationLogPage";
import { RoleManagementPage } from "../pages/system/roles/RoleManagementPage";
import { UserManagementPage } from "../pages/system/users/UserManagementPage";
import { clearAuth, getAuthState, saveTokens, saveUser, subscribeAuth } from "../store/auth";
import { CustomerPage } from "../pages/sales/customers/CustomerPage";
import { SaleOrderPage } from "../pages/sales/orders/SaleOrderPage";
import { SaleReturnPage } from "../pages/sales/returns/SaleReturnPage";
import { ShippingPage } from "../pages/sales/shipping/ShippingPage";
import { SaleReceivablePage } from "../pages/sales/receivables/SaleReceivablePage";
import { SaleExceptionPage } from "../pages/sales/exceptions/SaleExceptionPage";
import { EcommerceShopPage } from "../pages/sales/ecommerce/EcommerceShopPage";

function resolveDefaultRoute() {
  const permissions = getAuthState().user?.permissions ?? [];
  if (permissions.includes(SYSTEM_PERMISSIONS.USER_LIST)) {
    return "/system/users";
  }
  if (permissions.includes(SYSTEM_PERMISSIONS.DEPT_LIST)) {
    return "/system/departments";
  }
  if (permissions.includes(SYSTEM_PERMISSIONS.ROLE_LIST)) {
    return "/system/roles";
  }
  if (permissions.includes(SYSTEM_PERMISSIONS.DICT_LIST)) {
    return "/system/dict";
  }
  if (permissions.includes(SYSTEM_PERMISSIONS.LOG_LIST)) {
    return "/system/logs";
  }
  if (permissions.includes(PRODUCT_PERMISSIONS.PRODUCT_LIST)) {
    return "/product/products";
  }
  if (permissions.includes(PRODUCT_PERMISSIONS.CATEGORY_LIST)) {
    return "/product/categories";
  }
  if (permissions.includes(PRODUCTION_PERMISSIONS.PROCESS_LIST)) {
    return "/production/processes";
  }
  if (permissions.includes(PRODUCTION_PERMISSIONS.BOM_LIST)) {
    return "/production/boms";
  }
  if (permissions.includes(PRODUCTION_PERMISSIONS.BATCH_LIST)) {
    return "/production/batches";
  }
  if (permissions.includes(PRODUCTION_PERMISSIONS.REPORT_LIST)) {
    return "/production/reports";
  }
  if (permissions.includes(PRODUCTION_PERMISSIONS.SERIAL_LIST)) {
    return "/production/serials";
  }
  if (permissions.includes(PURCHASE_PERMISSIONS.ORDER_LIST)) {
    return "/purchase/orders";
  }
  if (permissions.includes(PURCHASE_PERMISSIONS.PAYABLE_LIST)) {
    return "/purchase/payables";
  }
  if (permissions.includes(PURCHASE_PERMISSIONS.EXCEPTION_LIST)) {
    return "/purchase/exceptions";
  }
  if (permissions.includes(SALES_PERMISSIONS.ORDER_LIST)) {
    return "/sales/orders";
  }
  if (permissions.includes(SALES_PERMISSIONS.CUSTOMER_LIST)) {
    return "/sales/customers";
  }
  if (permissions.includes(SALES_PERMISSIONS.RETURN_LIST)) {
    return "/sales/returns";
  }
  if (permissions.includes(SALES_PERMISSIONS.SHIPPING_LIST)) {
    return "/sales/shipping";
  }
  if (permissions.includes(SALES_PERMISSIONS.RECEIVABLE_LIST)) {
    return "/sales/receivables";
  }
  if (permissions.includes(SALES_PERMISSIONS.EXCEPTION_LIST)) {
    return "/sales/exceptions";
  }
  if (permissions.includes(MATERIAL_PERMISSIONS.MATERIAL_LIST)) {
    return "/material/materials";
  }
  if (permissions.includes(MATERIAL_PERMISSIONS.ALERT_LIST)) {
    return "/material/alerts";
  }
  if (permissions.includes(MATERIAL_PERMISSIONS.REPLENISH_LIST)) {
    return "/material/replenishment";
  }
  return "/login";
}

export function AppRouter() {
  const [, forceUpdate] = useState(0);
  const [bootstrapping, setBootstrapping] = useState(Boolean(getAuthState().accessToken && !getAuthState().user));

  useEffect(() => subscribeAuth(() => forceUpdate((value) => value + 1)), []);
  useEffect(() => {
    if (!getAuthState().accessToken || getAuthState().user) {
      setBootstrapping(false);
      return;
    }
    let active = true;
    setBootstrapping(true);
    const refreshToken = getAuthState().refreshToken;
    void (refreshToken
      ? refreshTokens({ refreshToken }).catch(() => {
          saveTokens(getAuthState().accessToken, refreshToken);
          return null;
        })
      : Promise.resolve(null))
      .then(() => fetchUserInfo())
      .then((user) => {
        if (!active) {
          return;
        }
        saveUser(user);
      })
      .catch(() => {
        if (!active) {
          return;
        }
        clearAuth();
      })
      .finally(() => {
        if (active) {
          setBootstrapping(false);
        }
      });
    return () => {
      active = false;
    };
  }, [getAuthState().accessToken, getAuthState().user]);

  return (
    <AntApp>
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/"
            element={
              <RequireAuth bootstrapping={bootstrapping}>
                <AppLayout />
              </RequireAuth>
            }
          >
            <Route index element={<Navigate to={resolveDefaultRoute()} replace />} />
            <Route path="/system/users" element={<PermissionGuard bootstrapping={bootstrapping} permission={SYSTEM_PERMISSIONS.USER_LIST}><UserManagementPage /></PermissionGuard>} />
            <Route path="/system/departments" element={<PermissionGuard bootstrapping={bootstrapping} permission={SYSTEM_PERMISSIONS.DEPT_LIST}><DepartmentManagementPage /></PermissionGuard>} />
            <Route path="/system/roles" element={<PermissionGuard bootstrapping={bootstrapping} permission={SYSTEM_PERMISSIONS.ROLE_LIST}><RoleManagementPage /></PermissionGuard>} />
            <Route path="/system/dict" element={<PermissionGuard bootstrapping={bootstrapping} permission={SYSTEM_PERMISSIONS.DICT_LIST}><DictManagementPage /></PermissionGuard>} />
            <Route path="/system/logs" element={<PermissionGuard bootstrapping={bootstrapping} permission={SYSTEM_PERMISSIONS.LOG_LIST}><OperationLogPage /></PermissionGuard>} />
            <Route path="/product/categories" element={<PermissionGuard bootstrapping={bootstrapping} permission={PRODUCT_PERMISSIONS.CATEGORY_LIST}><ProductCategoryPage /></PermissionGuard>} />
            <Route path="/product/products" element={<PermissionGuard bootstrapping={bootstrapping} permission={PRODUCT_PERMISSIONS.PRODUCT_LIST}><ProductManagementPage /></PermissionGuard>} />
            <Route path="/production/processes" element={<PermissionGuard bootstrapping={bootstrapping} permission={PRODUCTION_PERMISSIONS.PROCESS_LIST}><ProductionProcessPage /></PermissionGuard>} />
            <Route path="/production/boms" element={<PermissionGuard bootstrapping={bootstrapping} permission={PRODUCTION_PERMISSIONS.BOM_LIST}><ProductionBomPage /></PermissionGuard>} />
            <Route path="/production/batches" element={<PermissionGuard bootstrapping={bootstrapping} permission={PRODUCTION_PERMISSIONS.BATCH_LIST}><ProductionBatchPage /></PermissionGuard>} />
            <Route path="/production/reports" element={<PermissionGuard bootstrapping={bootstrapping} permission={PRODUCTION_PERMISSIONS.REPORT_LIST}><ProductionReportPage /></PermissionGuard>} />
            <Route path="/production/serials" element={<PermissionGuard bootstrapping={bootstrapping} permission={PRODUCTION_PERMISSIONS.SERIAL_LIST}><SerialNumberPage /></PermissionGuard>} />
            <Route path="/purchase/orders" element={<PermissionGuard bootstrapping={bootstrapping} permission={PURCHASE_PERMISSIONS.ORDER_LIST}><PurchaseOrderPage /></PermissionGuard>} />
            <Route path="/purchase/payables" element={<PermissionGuard bootstrapping={bootstrapping} permission={PURCHASE_PERMISSIONS.PAYABLE_LIST}><PurchasePayablePage /></PermissionGuard>} />
            <Route path="/purchase/exceptions" element={<PermissionGuard bootstrapping={bootstrapping} permission={PURCHASE_PERMISSIONS.EXCEPTION_LIST}><PurchaseExceptionPage /></PermissionGuard>} />
            <Route path="/material/categories" element={<PermissionGuard bootstrapping={bootstrapping} permission={MATERIAL_PERMISSIONS.CATEGORY_LIST}><MaterialCategoryPage /></PermissionGuard>} />
            <Route path="/material/materials" element={<PermissionGuard bootstrapping={bootstrapping} permission={MATERIAL_PERMISSIONS.MATERIAL_LIST}><MaterialManagementPage /></PermissionGuard>} />
            <Route path="/material/inventory" element={<PermissionGuard bootstrapping={bootstrapping} permission={MATERIAL_PERMISSIONS.MATERIAL_LIST}><MaterialInventoryPage /></PermissionGuard>} />
            <Route path="/material/safety-stock" element={<PermissionGuard bootstrapping={bootstrapping} permission={MATERIAL_PERMISSIONS.MATERIAL_LIST}><MaterialSafetyStockPage /></PermissionGuard>} />
            <Route path="/material/alerts" element={<PermissionGuard bootstrapping={bootstrapping} permission={MATERIAL_PERMISSIONS.ALERT_LIST}><MaterialAlertPage /></PermissionGuard>} />
            <Route path="/material/replenishment" element={<PermissionGuard bootstrapping={bootstrapping} permission={MATERIAL_PERMISSIONS.REPLENISH_LIST}><MaterialReplenishmentPage /></PermissionGuard>} />
            <Route path="/material/quotes" element={<PermissionGuard bootstrapping={bootstrapping} permission={MATERIAL_PERMISSIONS.QUOTE_LIST}><SupplierQuotePage /></PermissionGuard>} />
            <Route path="/material/suppliers" element={<PermissionGuard bootstrapping={bootstrapping} permission={MATERIAL_PERMISSIONS.SUPPLIER_LIST}><SupplierManagementPage /></PermissionGuard>} />
            <Route path="/inventory/receipts" element={<PermissionGuard bootstrapping={bootstrapping} permission={MATERIAL_PERMISSIONS.MATERIAL_LIST}><InventoryReceiptPage /></PermissionGuard>} />
            <Route path="/inventory/issues" element={<PermissionGuard bootstrapping={bootstrapping} permission={MATERIAL_PERMISSIONS.MATERIAL_UPDATE}><InventoryIssuePage /></PermissionGuard>} />
            <Route path="/inventory/transfers" element={<PermissionGuard bootstrapping={bootstrapping} permission={MATERIAL_PERMISSIONS.MATERIAL_UPDATE}><InventoryTransferPage /></PermissionGuard>} />
            <Route path="/inventory/checks" element={<PermissionGuard bootstrapping={bootstrapping} permission={MATERIAL_PERMISSIONS.MATERIAL_UPDATE}><InventoryCheckPage /></PermissionGuard>} />
            <Route path="/inventory/transactions" element={<PermissionGuard bootstrapping={bootstrapping} permission={MATERIAL_PERMISSIONS.MATERIAL_LIST}><InventoryTransactionPage /></PermissionGuard>} />
            <Route path="/sales/customers" element={<PermissionGuard bootstrapping={bootstrapping} permission={SALES_PERMISSIONS.CUSTOMER_LIST}><CustomerPage /></PermissionGuard>} />
            <Route path="/sales/orders" element={<PermissionGuard bootstrapping={bootstrapping} permission={SALES_PERMISSIONS.ORDER_LIST}><SaleOrderPage /></PermissionGuard>} />
            <Route path="/sales/returns" element={<PermissionGuard bootstrapping={bootstrapping} permission={SALES_PERMISSIONS.RETURN_LIST}><SaleReturnPage /></PermissionGuard>} />
            <Route path="/sales/shipping" element={<PermissionGuard bootstrapping={bootstrapping} permission={SALES_PERMISSIONS.SHIPPING_LIST}><ShippingPage /></PermissionGuard>} />
            <Route path="/sales/receivables" element={<PermissionGuard bootstrapping={bootstrapping} permission={SALES_PERMISSIONS.RECEIVABLE_LIST}><SaleReceivablePage /></PermissionGuard>} />
            <Route path="/sales/exceptions" element={<PermissionGuard bootstrapping={bootstrapping} permission={SALES_PERMISSIONS.EXCEPTION_LIST}><SaleExceptionPage /></PermissionGuard>} />
            <Route path="/sales/ecommerce" element={<PermissionGuard bootstrapping={bootstrapping} permission={SALES_PERMISSIONS.ORDER_LIST}><EcommerceShopPage /></PermissionGuard>} />
          </Route>
        </Routes>
      </Router>
    </AntApp>
  );
}
