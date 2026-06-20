import React, { FormEvent, useEffect, useState } from "react";
import ReactDOM from "react-dom/client";
import { APP_NAME, formatTitle } from "@erp/shared";
import { fetchUserInfo, login, logout } from "./api/auth";
import { saveAccessToken, saveUser } from "./store/auth";
import type { UserInfo } from "./types/auth";
import "./styles.css";

document.title = formatTitle(`${APP_NAME} Desktop`);

type OperationKey = "batch" | "production" | "receipt" | "issue" | "check" | "boxPrint" | "documentPrint";

const operationCards: Array<{ key: OperationKey; title: string; description: string; tag: string }> = [
  { key: "batch", title: "创建工单", description: "按产品、数量、计划时间创建生产工单。", tag: "生产准备" },
  { key: "production", title: "生产执行", description: "记录报工、良品数、不良数和生产进度。", tag: "车间操作" },
  { key: "receipt", title: "扫码入库", description: "扫描物料码或箱码完成入库确认。", tag: "仓库操作" },
  { key: "issue", title: "扫码出库", description: "扫描出库单、物料码或箱码完成发料/出库。", tag: "仓库操作" },
  { key: "check", title: "仓库盘点", description: "按库位或物料发起盘点并录入实盘数量。", tag: "库存校准" },
  { key: "boxPrint", title: "箱码打印", description: "按工单和装箱数量生成并打印箱码。", tag: "标签打印" },
  { key: "documentPrint", title: "出入库单据打印", description: "打印入库单、出库单和盘点差异单。", tag: "单据打印" }
];

function App() {
  const [username, setUsername] = useState("admin");
  const [password, setPassword] = useState("password");
  const [user, setUser] = useState<UserInfo | null>(null);
  const [activeOperation, setActiveOperation] = useState<OperationKey>("production");
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
    const selectedOperation = operationCards.find((operation) => operation.key === activeOperation) ?? operationCards[0];

    return (
      <main className="desktop-shell desktop-workspace-shell">
        <section className="desktop-card desktop-workspace">
          <div className="desktop-workspace-header">
            <div>
              <span className="desktop-badge">ERP Desktop</span>
              <h1>生产仓储作业台</h1>
              <p>欢迎回来，{user.realName || user.username}。桌面端聚焦工单、生产执行、扫码出入库、盘点和打印。</p>
            </div>
            <button className="desktop-secondary-button" disabled={loading} onClick={() => void handleLogout()}>{loading ? "退出中..." : "退出登录"}</button>
          </div>
          <div className="desktop-operation-layout">
            <div className="desktop-operation-grid">
              {operationCards.map((operation) => (
                <button
                  key={operation.key}
                  className={activeOperation === operation.key ? "desktop-operation-card active" : "desktop-operation-card"}
                  type="button"
                  onClick={() => setActiveOperation(operation.key)}
                >
                  <span>{operation.tag}</span>
                  <strong>{operation.title}</strong>
                  <small>{operation.description}</small>
                </button>
              ))}
            </div>
            <OperationPanel operation={selectedOperation} />
          </div>
        </section>
      </main>
    );
  }

  return (
    <main className="desktop-shell">
      <section className="desktop-card desktop-login-card">
        <span className="desktop-badge">ERP Desktop</span>
        <h1>全渠道 ERP 桌面端登录</h1>
        <p>连接本机后端服务，使用管理员账号进入桌面作业台。</p>
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

function OperationPanel({ operation }: { operation: (typeof operationCards)[number] }) {
  return (
    <div className="desktop-operation-panel">
      <span>{operation.tag}</span>
      <h2>{operation.title}</h2>
      <p>{operation.description}</p>
      <div className="desktop-operation-steps">
        {renderOperationSteps(operation.key).map((step) => <div key={step}>{step}</div>)}
      </div>
      <button className="desktop-primary-button" type="button">进入{operation.title}</button>
    </div>
  );
}

function renderOperationSteps(operationKey: OperationKey) {
  const stepMap: Record<OperationKey, string[]> = {
    batch: ["选择产品与 BOM", "录入计划数量", "生成生产工单"],
    production: ["扫描/选择工单", "录入生产数量", "提交生产执行记录"],
    receipt: ["扫描入库单或箱码", "校验物料与数量", "确认入库"],
    issue: ["扫描出库单或领料单", "校验库存批次", "确认出库"],
    check: ["选择仓库/库位", "扫码盘点库存", "提交盈亏差异"],
    boxPrint: ["选择工单", "生成箱码", "连接打印机输出标签"],
    documentPrint: ["选择单据类型", "预览单据", "连接打印机输出"]
  };
  return stepMap[operationKey];
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
