import { App as AntApp } from "antd";
import { Navigate, Route, BrowserRouter as Router, Routes } from "react-router-dom";
import { AppLayout } from "../layouts/AppLayout";
import { LoginPage } from "../pages/login/LoginPage";
import { DepartmentManagementPage } from "../pages/system/departments/DepartmentManagementPage";
import { RoleManagementPage } from "../pages/system/roles/RoleManagementPage";
import { UserManagementPage } from "../pages/system/users/UserManagementPage";
import { authStore } from "../store/auth";

function ProtectedRoute({ children }: { children: JSX.Element }) {
  return authStore.accessToken ? children : <Navigate to="/login" replace />;
}

export function AppRouter() {
  return (
    <AntApp>
      <Router>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <AppLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/system/users" replace />} />
            <Route path="/system/users" element={<UserManagementPage />} />
            <Route path="/system/departments" element={<DepartmentManagementPage />} />
            <Route path="/system/roles" element={<RoleManagementPage />} />
          </Route>
        </Routes>
      </Router>
    </AntApp>
  );
}
