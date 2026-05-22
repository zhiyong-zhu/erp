import React, { FormEvent, useEffect, useState } from "react";
import ReactDOM from "react-dom/client";
import { APP_NAME, formatTitle } from "@erp/shared";
import { fetchUserInfo, login, logout } from "./api/auth";
import { saveAccessToken, saveUser } from "./store/auth";
import type { UserInfo } from "./types/auth";
import "./styles.css";

document.title = formatTitle(`${APP_NAME} Desktop`);

function App() {
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("password");
  const [user, setUser] = useState<UserInfo | null>(null);
  const [loading, setLoading] = useState(false);
  const [booting, setBooting] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchUserInfo()
      .then((info) => {
        saveUser(info);
        setUser(info);
      })
      .catch(() => {
        saveAccessToken(null);
        saveUser(null);
      })
      .finally(() => setBooting(false));
  }, []);

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
      setLoading(false);
    }
  }

  if (booting) {
    return <main className="desktop-shell"><div className="desktop-card">正在检查登录状态...</div></main>;
  }

  if (user) {
    return (
      <main className="desktop-shell">
        <section className="desktop-card desktop-dashboard">
          <div>
            <span className="desktop-badge">ERP Desktop</span>
            <h1>欢迎回来，{user.realName || user.username}</h1>
            <p>桌面端登录已接入后端鉴权服务，可继续扩展本地打印、通知与离线能力。</p>
          </div>
          <div className="desktop-user-panel">
            <div><strong>用户 ID</strong><span>{user.userId}</span></div>
            <div><strong>用户名</strong><span>{user.username}</span></div>
            <div><strong>角色</strong><span>{user.roles.length ? user.roles.join("、") : "暂无"}</span></div>
            <div><strong>权限</strong><span>{user.permissions.length} 项</span></div>
          </div>
          <button className="desktop-secondary-button" disabled={loading} onClick={() => void handleLogout()}>
            {loading ? "退出中..." : "退出登录"}
          </button>
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
          <label>
            用户名
            <input value={username} onChange={(event) => setUsername(event.target.value)} placeholder="请输入用户名" required />
          </label>
          <label>
            密码
            <input value={password} onChange={(event) => setPassword(event.target.value)} placeholder="请输入密码" required type="password" />
          </label>
          <button className="desktop-primary-button" disabled={loading} type="submit">
            {loading ? "登录中..." : "登录"}
          </button>
        </form>
      </section>
    </main>
  );
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
