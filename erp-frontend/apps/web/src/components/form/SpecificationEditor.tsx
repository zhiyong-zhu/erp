import { DeleteOutlined, PlusOutlined } from "@ant-design/icons";
import { Button, Input, Popconfirm, Space, Typography } from "antd";
import {
  createEmptySpecificationRow,
  type EditableSpecificationRow
} from "./types";

const { Text } = Typography;

export interface SpecificationEditorProps {
  value: EditableSpecificationRow[];
  onChange: (rows: EditableSpecificationRow[]) => void;
  /** 补全 SKU 候选（追加模式） */
  onAppendSkus?: () => void;
  /** 重建 SKU 候选（覆盖模式，需二次确认） */
  onRebuildSkus?: () => void;
}

export function SpecificationEditor({ value, onChange, onAppendSkus, onRebuildSkus }: SpecificationEditorProps) {
  function updateRow(rowId: string, field: "key" | "valuesText", newValue: string) {
    onChange(value.map((row) => (row.rowId === rowId ? { ...row, [field]: newValue } : row)));
  }

  function addRow() {
    onChange([...value, createEmptySpecificationRow()]);
  }

  function removeRow(rowId: string) {
    onChange(value.filter((row) => row.rowId !== rowId));
  }

  return (
    <div className="form-subsection">
      <div className="form-subsection-head">
        <Text strong>规格定义</Text>
        <Space size="small">
          <Button size="small" icon={<PlusOutlined />} onClick={addRow}>新增规格</Button>
          {onAppendSkus ? <Button size="small" onClick={onAppendSkus}>补全SKU</Button> : null}
          {onRebuildSkus ? (
            <Popconfirm
              title="重建SKU候选"
              description="会按当前规格组合重建SKU列表，已手工维护的价格、条码、重量会被覆盖，确定继续吗？"
              okText="重建"
              cancelText="取消"
              onConfirm={onRebuildSkus}
            >
              <Button size="small" danger>重建</Button>
            </Popconfirm>
          ) : null}
        </Space>
      </div>
      <Text type="secondary" className="product-sku-hint" style={{ fontSize: 12 }}>
        维护规格名称和可选值，提交时转为 JSON。示例：<code>{"{\"颜色\":[\"银色\",\"黑色\"]}"}</code>
      </Text>
      {value.length === 0 ? (
        <div className="product-spec-empty">暂无规格定义，可按需新增。</div>
      ) : (
        value.map((row) => (
          <div key={row.rowId} className="product-spec-row">
            <Input
              value={row.key}
              placeholder="规格名称，如 颜色"
              onChange={(event) => updateRow(row.rowId, "key", event.target.value)}
            />
            <Input
              value={row.valuesText}
              placeholder="可选值，用逗号分隔，如 银色, 黑色"
              onChange={(event) => updateRow(row.rowId, "valuesText", event.target.value)}
            />
            <Button danger icon={<DeleteOutlined />} onClick={() => removeRow(row.rowId)} />
          </div>
        ))
      )}
    </div>
  );
}
