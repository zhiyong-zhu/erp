import React from "react";
import ReactDOM from "react-dom/client";
import { ConfigProvider } from "antd";
import { formatTitle } from "@erp/shared";
import { AppRouter } from "./router/AppRouter";
import "antd/dist/reset.css";
import "./styles.css";

document.title = formatTitle("全渠道 ERP");

ReactDOM.createRoot(document.getElementById("root")!).render(
  <React.StrictMode>
    <ConfigProvider
      theme={{
        token: {
          colorPrimary: "#0f766e",
          borderRadius: 14,
          colorBgLayout: "#eef4f8"
        }
      }}
    >
      <AppRouter />
    </ConfigProvider>
  </React.StrictMode>
);
