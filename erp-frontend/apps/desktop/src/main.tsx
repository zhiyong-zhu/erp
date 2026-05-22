import React from "react";
import ReactDOM from "react-dom/client";
import { APP_NAME, formatTitle } from "@erp/shared";

document.title = formatTitle(`${APP_NAME} Desktop`);

function App() {
  return (
    <main style={{ padding: 32, fontFamily: '"Segoe UI", "PingFang SC", sans-serif' }}>
      <h1>{APP_NAME} Desktop</h1>
      <p>Tauri 2.x 桌面端骨架已创建，后续可接入本地打印、通知与离线能力。</p>
      <ul>
        <li>本地打印：预留 Tauri command</li>
        <li>桌面通知：预留官方插件接入</li>
        <li>离线缓存：预留本地 SQLite / store</li>
      </ul>
    </main>
  );
}

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
