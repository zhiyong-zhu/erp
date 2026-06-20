import React, { FormEvent, useEffect, useState } from "react";
import ReactDOM from "react-dom/client";
import { APP_NAME, formatTitle } from "@erp/shared";
import { fetchUserInfo, login, logout } from "./api/auth";
import {
  createDepartment,
  createRole,
  fetchDepartments,
  fetchRoles,
  updateDepartment,
  updateDepartmentStatus,
  updateRole,
  updateRoleStatus
} from "./api/system";
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
  const [editingDepartment, setEditingDepartment] = useState<DepartmentRecord | null>(null);
  const [name, setName] = useState("");
  const [code, setCode] = useState("");
  const [leader, setLeader] = useState("");
  const [phone, setPhone] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function startEdit(department: DepartmentRecord) {
    setEditingDepartment(department);
    setName(department.name);
    setCode(department.code);
    setLeader(department.leader ?? "");
    setPhone(department.phone ?? "");
    setError(null);
  }

  function resetForm() {
    setEditingDepartment(null);
    setName("");
    setCode("");
    setLeader("");
    setPhone("");
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      if (editingDepartment) {
        await updateDepartment(editingDepartment.id, {
          parentId: editingDepartment.parentId ?? null,
          name,
          code,
          leader,
          phone,
          sortOrder: editingDepartment.sortOrder
        });
      } else {
        await createDepartment({ name, code, leader, phone, sortOrder: flattenDepartments(departments).length + 1 });
      }
      resetForm();
      await onReload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "部门保存失败");
    } finally {
      setSaving(false);
    }
  }

  async function handleToggleStatus(department: DepartmentRecord) {
    setSaving(true);
    setError(null);
    try {
      await updateDepartmentStatus(department.id, department.status === 1 ? 0 : 1);
      await onReload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "部门状态更新失败");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="desktop-management-grid">
      <form className="desktop-mini-form" onSubmit={(event) => void handleSubmit(event)}>
        <div className="desktop-form-title-row">
          <h2>{editingDepartment ? "编辑部门" : "新建部门"}</h2>
          {editingDepartment ? <button className="desktop-link-button" type="button" onClick={resetForm}>取消</button> : null}
        </div>
        {error ? <div className="desktop-alert">{error}</div> : null}
        <input value={name} onChange={(event) => setName(event.target.value)} placeholder="部门名称" required />
        <input value={code} onChange={(event) => setCode(event.target.value)} placeholder="部门编码" required />
        <input value={leader} onChange={(event) => setLeader(event.target.value)} placeholder="负责人" />
        <input value={phone} onChange={(event) => setPhone(event.target.value)} placeholder="电话" />
        <button className="desktop-primary-button" disabled={saving} type="submit">{saving ? "保存中..." : editingDepartment ? "更新部门" : "保存部门"}</button>
      </form>
      <div className="desktop-list-card">
        <h2>部门列表</h2>
        {flattenDepartments(departments).map((department) => (
          <ListItem
            key={department.id}
            title={department.name}
            meta={`${department.code} · ${department.leader || "未设置负责人"} · ${department.status === 1 ? "启用" : "禁用"}`}
            status={department.status}
            disabled={saving}
            onEdit={() => startEdit(department)}
            onToggleStatus={() => void handleToggleStatus(department)}
          />
        ))}
      </div>
    </div>
  );
}

function RolePanel({ roles, onReload }: { roles: RoleRecord[]; onReload: () => Promise<void> }) {
  const [editingRole, setEditingRole] = useState<RoleRecord | null>(null);
  const [name, setName] = useState("");
  const [code, setCode] = useState("");
  const [description, setDescription] = useState("");
  const [dataScope, setDataScope] = useState(1);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function startEdit(role: RoleRecord) {
    setEditingRole(role);
    setName(role.name);
    setCode(role.code);
    setDescription(role.description ?? "");
    setDataScope(role.dataScope);
    setError(null);
  }

  function resetForm() {
    setEditingRole(null);
    setName("");
    setCode("");
    setDescription("");
    setDataScope(1);
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setError(null);
    try {
      if (editingRole) {
        await updateRole(editingRole.id, { name, code, description, dataScope, permissionIds: editingRole.permissionIds ?? [] });
      } else {
        await createRole({ name, code, description, dataScope });
      }
      resetForm();
      await onReload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "角色保存失败");
    } finally {
      setSaving(false);
    }
  }

  async function handleToggleStatus(role: RoleRecord) {
    setSaving(true);
    setError(null);
    try {
      await updateRoleStatus(role.id, role.status === 1 ? 0 : 1);
      await onReload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "角色状态更新失败");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="desktop-management-grid">
      <form className="desktop-mini-form" onSubmit={(event) => void handleSubmit(event)}>
        <div className="desktop-form-title-row">
          <h2>{editingRole ? "编辑角色" : "新建角色"}</h2>
          {editingRole ? <button className="desktop-link-button" type="button" onClick={resetForm}>取消</button> : null}
        </div>
        {error ? <div className="desktop-alert">{error}</div> : null}
        <input value={name} onChange={(event) => setName(event.target.value)} placeholder="角色名称" required />
        <input value={code} onChange={(event) => setCode(event.target.value)} placeholder="角色编码" required />
        <input value={description} onChange={(event) => setDescription(event.target.value)} placeholder="描述" />
        <select value={dataScope} onChange={(event) => setDataScope(Number(event.target.value))}>
          <option value={1}>全部数据</option>
          <option value={2}>部门数据</option>
          <option value={3}>本人数据</option>
        </select>
        <button className="desktop-primary-button" disabled={saving} type="submit">{saving ? "保存中..." : editingRole ? "更新角色" : "保存角色"}</button>
      </form>
      <div className="desktop-list-card">
        <h2>角色列表</h2>
        {roles.map((role) => (
          <ListItem
            key={role.id}
            title={role.name}
            meta={`${role.code} · ${renderDataScope(role.dataScope)} · ${role.status === 1 ? "启用" : "禁用"}`}
            status={role.status}
            disabled={saving}
            onEdit={() => startEdit(role)}
            onToggleStatus={() => void handleToggleStatus(role)}
          />
        ))}
      </div>
    </div>
  );
}

function ListItem({ title, meta, status, disabled, onEdit, onToggleStatus }: {
  title: string;
  meta: string;
  status: number;
  disabled: boolean;
  onEdit: () => void;
  onToggleStatus: () => void;
}) {
  return (
    <div className="desktop-list-item">
      <div>
        <strong>{title}</strong>
        <span>{meta}</span>
      </div>
      <div className="desktop-list-actions">
        <button className="desktop-link-button" disabled={disabled} type="button" onClick={onEdit}>编辑</button>
        <button className="desktop-link-button" disabled={disabled} type="button" onClick={onToggleStatus}>{status === 1 ? "禁用" : "启用"}</button>
      </div>
    </div>
  );
}

function flattenDepartments(departments: DepartmentRecord[]): DepartmentRecord[] {
  return departments.flatMap((department) => [department, ...flattenDepartments(department.children ?? [])]);
}

function renderDataScope(dataScope: number) {
  if (dataScope === 1) return "全部数据";
  if (dataScope === 2) return "部门数据";
  if (dataScope === 3) return "本人数据";
  return `数据范围 ${dataScope}`;
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
