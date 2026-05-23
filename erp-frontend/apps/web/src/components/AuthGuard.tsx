import { Result, Spin } from "antd";
import { Navigate } from "react-router-dom";
import { getAuthState, hasPermission } from "../store/auth";

export function RequireAuth({ bootstrapping, children }: { bootstrapping?: boolean; children: JSX.Element }) {
  if (bootstrapping) {
    return <div className="auth-loading-shell"><Spin size="large" /></div>;
  }
  return getAuthState().accessToken ? children : <Navigate to="/login" replace />;
}

export function PermissionGuard({ bootstrapping, permission, children }: { bootstrapping?: boolean; permission?: string; children: JSX.Element }) {
  if (!permission) {
    return children;
  }
  if (bootstrapping) {
    return <div className="auth-loading-shell"><Spin size="large" /></div>;
  }
  if (!getAuthState().accessToken) {
    return <Navigate to="/login" replace />;
  }
  if (!getAuthState().user) {
    return <div className="auth-loading-shell"><Spin size="large" /></div>;
  }
  if (!hasPermission(permission)) {
    return <Result status="403" title="403" subTitle="当前账号没有访问该页面的权限。" />;
  }
  return children;
}
