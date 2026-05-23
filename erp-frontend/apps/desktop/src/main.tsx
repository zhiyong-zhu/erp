import React, { FormEvent, useEffect, useState } from "react";
import ReactDOM from "react-dom/client";
import { APP_NAME, formatTitle } from "@erp/shared";
import { fetchUserInfo, login, logout } from "./api/auth";
import { createDepartment, createRole, fetchDepartments, fetchRoles } from "./api/system";
import { saveAccessToken, saveUser } from "./store/auth";
import type { UserInfo } from "./types/auth";
import type { DepartmentRecord, RoleRecord } from "./types/system";
import "./styles.css";

document.title = formatTitle(`${APP_NAME} Desktop`);

type WorkspaceTab = "departments" | "roles";

function App() {
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("password");
  const [user, setUser] = useState<UserInfo | null>(null);
  const [departments, setDepartments] = useState<DepartmentRecord[]>([]);
  const [roles, setRoles] = useState<RoleRecord[]>([]);
  const [activeTab, setActiveTab] = useState<WorkspaceTab>("departments");
  const [loading, setLoading] = useState(false);
  const [booting, setBooting] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchUserInfo()
      .then((info) => {
        saveUser(info);
        setUser(info);
        return loadSystemData();
      })
      .catch(() => {
        saveAccessToken(null);
        saveUser(null);
      })
      .finally(() => setBooting(false));
  }, []);

  async function loadSystemData() {
    const [departmentData, roleData] = await Promise.all([fetchDepartments(), fetchRoles()]);
    setDepartments(departmentData);
    setRoles(roleData);
  }

  async function handleLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setLoading(true);
    setError(null);
    try {
      const result = await login({ username, password });
      saveAccessToken(result.accessToken);
      const info = await fetchUserInfo();
      saveUser(info);
      setUser(info);
      await loadSystemData();
    } catch (err) {
      setError(err instanceof Error ? err.message : "登录失败，请检查用户名和密码");
    } finally {
      setLoading(false);
    }
  }

  async function handleLogout() {
    setLoading(true);
    try {
      await logout();
    } finally {
      saveAccessToken(null);
      saveUser(null);
      setUser(null);
      setDepartments([]);
      setRoles([]);
      setLoading(false);
    }
  }

  if (booting) {
    return <main className="desktop-shell"><div className="desktop-card">正在检查登录状态...</div></main>;
  }

  if (user) {
    return (
      <main className="desktop-shell desktop-workspace-shell">
        <section className="desktop-card desktop-workspace">
          <div className="desktop-workspace-header">
            <div>
              <span className="desktop-badge">ERP Desktop</span>
              <h1>系统管理工作台</h1>
              <p>欢迎回来，{user.realName || user.username}。部门与角色数据已和后端同步。</p>
            </div>
            <button className="desktop-secondary-button" disabled={loading} onClick={() => void handleLogout()}>{loading ? "退出中..." : "退出登录"}</button>
          </div>
          <div className="desktop-tabs">
            <button className={activeTab === "departments" ? "active" : ""} onClick={() => setActiveTab("departments")}>部门管理</button>
            <button className={activeTab === "roles" ? "active" : ""} onClick={() => setActiveTab("roles")}>角色管理</button>
          </div>
          {activeTab === "departments" ? <DepartmentPanel departments={departments} onReload={loadSystemData} /> : <RolePanel roles={roles} onReload={loadSystemData} />}
        </section>
      </main>
    );
  }

  return (
    <main className="desktop-shell">
      <section className="desktop-card desktop-login-card">
        <span className="desktop-badge">ERP Desktop</span>
        <h1>全渠道 ERP 桌面端登录</h1>
        <p>连接本机后端服务，使用管理员账号进入桌面控制台。</p>
        {error ? <div className="desktop-alert">{error}</div> : null}
        <form className="desktop-form" onSubmit={(event) => void handleLogin(event)}>
          <label>用户名<input value={username} onChange={(event) => setUsername(event.target.value)} placeholder="请输入用户名" required /></label>
          <label>密码<input value={password} onChange={(event) => setPassword(event.target.value)} placeholder="请输入密码" required type="password" /></label>
          <button className="desktop-primary-button" disabled={loading} type="submit">{loading ? "登录中..." : "登录"}</button>
        </form>
      </section>
    </main>
  );
}

function DepartmentPanel({ departments, onReload }: { departments: DepartmentRecord[]; onReload: () => Promise<void> }) {
  const [name, setName] = useState("");
  const [code, setCode] = useState("");
  const [leader, setLeader] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      await createDepartment({ name, code, leader, sortOrder: flattenDepartments(departments).length + 1 });
      setName("");
      setCode("");
      setLeader("");
      await onReload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "部门创建失败");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="desktop-management-grid">
      <form className="desktop-mini-form" onSubmit={(event) => void handleCreate(event)}>
        <h2>新建部门</h2>
        {error ? <div className="desktop-alert">{error}</div> : null}
        <input value={name} onChange={(event) => setName(event.target.value)} placeholder="部门名称" required />
        <input value={code} onChange={(event) => setCode(event.target.value)} placeholder="部门编码" required />
        <input value={leader} onChange={(event) => setLeader(event.target.value)} placeholder="负责人" />
        <button className="desktop-primary-button" disabled={saving} type="submit">{saving ? "保存中..." : "保存部门"}</button>
      </form>
      <div className="desktop-list-card">
        <h2>部门列表</h2>
        {flattenDepartments(departments).map((department) => <ListItem key={department.id} title={department.name} meta={`${department.code} · ${department.status === 1 ? "启用" : "禁用"}`} />)}
      </div>
    </div>
  );
}

function RolePanel({ roles, onReload }: { roles: RoleRecord[]; onReload: () => Promise<void> }) {
  const [name, setName] = useState("");
  const [code, setCode] = useState("");
  const [description, setDescription] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      await createRole({ name, code, description, dataScope: 1 });
      setName("");
      setCode("");
      setDescription("");
      await onReload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "角色创建失败");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="desktop-management-grid">
      <form className="desktop-mini-form" onSubmit={(event) => void handleCreate(event)}>
        <h2>新建角色</h2>
        {error ? <div className="desktop-alert">{error}</div> : null}
        <input value={name} onChange={(event) => setName(event.target.value)} placeholder="角色名称" required />
        <input value={code} onChange={(event) => setCode(event.target.value)} placeholder="角色编码" required />
        <input value={description} onChange={(event) => setDescription(event.target.value)} placeholder="描述" />
        <button className="desktop-primary-button" disabled={saving} type="submit">{saving ? "保存中..." : "保存角色"}</button>
      </form>
      <div className="desktop-list-card">
        <h2>角色列表</h2>
        {roles.map((role) => <ListItem key={role.id} title={role.name} meta={`${role.code} · 数据范围 ${role.dataScope} · ${role.status === 1 ? "启用" : "禁用"}`} />)}
      </div>
    </div>
  );
}

function ListItem({ title, meta }: { title: string; meta: string }) {
  return <div className="desktop-list-item"><strong>{title}</strong><span>{meta}</span></div>;
}

function flattenDepartments(departments: DepartmentRecord[]): DepartmentRecord[] {
  return departments.flatMap((department) => [department, ...flattenDepartments(department.children ?? [])]);
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
