import { App as AntApp } from "antd";
import { useEffect, useState } from "react";
import { Navigate, Route, BrowserRouter as Router, Routes } from "react-router-dom";
import { fetchUserInfo } from "../api/auth";
import { PermissionGuard, RequireAuth } from "../components/AuthGuard";
import { AppLayout } from "../layouts/AppLayout";
import { LoginPage } from "../pages/login/LoginPage";
import { ProductCategoryPage } from "../pages/product/categories/ProductCategoryPage";
import { ProductManagementPage } from "../pages/product/products/ProductManagementPage";
import { DepartmentManagementPage } from "../pages/system/departments/DepartmentManagementPage";
import { DictManagementPage } from "../pages/system/dicts/DictManagementPage";
import { OperationLogPage } from "../pages/system/logs/OperationLogPage";
import { RoleManagementPage } from "../pages/system/roles/RoleManagementPage";
import { UserManagementPage } from "../pages/system/users/UserManagementPage";
import { PRODUCT_PERMISSIONS, SYSTEM_PERMISSIONS } from "@erp/shared";
import { clearAuth, getAuthState, saveUser, subscribeAuth } from "../store/auth";

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
    void fetchUserInfo()
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
          </Route>
        </Routes>
      </Router>
    </AntApp>
  );
}
