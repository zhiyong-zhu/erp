import { EnvironmentOutlined } from "@ant-design/icons";
import { Tag } from "antd";
import { useEffect, useRef, useState } from "react";
import type { CustomerAddressRecord } from "../../types/sales";

/**
 * 地址选择器：大号输入框 + 卡片式下拉列表。
 * - 选中客户后传入该客户的历史地址列表
 * - 点击输入框展开卡片下拉，展示每个地址（收件人/电话/地址/默认标记）
 * - 选中某卡片填入输入框；也可直接手动输入自定义地址
 */
export function AddressPicker({
  value,
  onChange,
  options,
  loading
}: {
  value?: string;
  onChange?: (value: string) => void;
  options: CustomerAddressRecord[];
  loading?: boolean;
}) {
  const [open, setOpen] = useState(false);
  const containerRef = useRef<HTMLDivElement>(null);

  // 点击外部关闭下拉
  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  function selectAddress(addr: CustomerAddressRecord) {
    onChange?.(addr.address);
    setOpen(false);
  }

  return (
    <div ref={containerRef} style={{ position: "relative", width: "100%" }}>
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: 6,
          padding: "4px 11px",
          minHeight: 32,
          border: "1px solid #d9d9d9",
          borderRadius: 6,
          background: "#fff",
          cursor: "text"
        }}
        onClick={() => setOpen((v) => !v)}
      >
        <EnvironmentOutlined style={{ color: "#8c8c8c", flexShrink: 0 }} />
        <input
          type="text"
          value={value ?? ""}
          placeholder={options.length > 0 ? "选择历史地址或输入新地址" : "请输入收货地址"}
          onChange={(e) => onChange?.(e.target.value)}
          onFocus={() => setOpen(true)}
          style={{
            border: "none", outline: "none", flex: 1, fontSize: 14, background: "transparent"
          }}
        />
        {options.length > 0 && (
          <span style={{ color: "#8c8c8c", fontSize: 12, flexShrink: 0 }}>{options.length} 条历史</span>
        )}
      </div>

      {open && (
        <div
          style={{
            position: "absolute",
            top: "100%",
            left: 0,
            right: 0,
            marginTop: 4,
            zIndex: 1050,
            maxHeight: 360,
            overflowY: "auto",
            background: "#fff",
            border: "1px solid #e8e8e8",
            borderRadius: 8,
            boxShadow: "0 6px 16px rgba(0,0,0,0.12)"
          }}
        >
          {loading && <div style={{ padding: 16, textAlign: "center", color: "#8c8c8c" }}>加载中...</div>}
          {!loading && options.length === 0 && (
            <div style={{ padding: 16, textAlign: "center", color: "#8c8c8c" }}>暂无历史地址，请直接输入</div>
          )}
          {options.map((addr) => (
            <div
              key={addr.id}
              onClick={(e) => { e.stopPropagation(); selectAddress(addr); }}
              style={{
                padding: "10px 12px",
                borderBottom: "1px solid #f5f5f5",
                cursor: "pointer",
                transition: "background 0.2s"
              }}
              onMouseEnter={(e) => { e.currentTarget.style.background = "#f0f5ff"; }}
              onMouseLeave={(e) => { e.currentTarget.style.background = "#fff"; }}
            >
              <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 4 }}>
                <div style={{ display: "flex", alignItems: "center", gap: 8 }}>
                  {addr.recipient && <span style={{ fontWeight: 500 }}>{addr.recipient}</span>}
                  {addr.phone && <span style={{ color: "#8c8c8c", fontSize: 12 }}>{addr.phone}</span>}
                </div>
                {addr.isDefault && <Tag color="green" style={{ margin: 0 }}>默认</Tag>}
              </div>
              <div style={{ color: "#595959", fontSize: 13, lineHeight: 1.5 }}>{addr.address}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
