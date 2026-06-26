import { DeleteOutlined, PlusOutlined } from "@ant-design/icons";
import { Button, Input, Typography } from "antd";
import { createEmptyAttributeRow, type EditableAttributeRow } from "./types";

const { Text } = Typography;

export function AttributeEditor({ value, onChange }: {
  value: EditableAttributeRow[];
  onChange: (rows: EditableAttributeRow[]) => void;
}) {
  function updateRow(rowId: string, field: "key" | "value", newValue: string) {
    onChange(value.map((row) => (row.rowId === rowId ? { ...row, [field]: newValue } : row)));
  }

  function addRow() {
    onChange([...value, createEmptyAttributeRow()]);
  }

  function removeRow(rowId: string) {
    onChange(value.length > 1 ? value.filter((row) => row.rowId !== rowId) : [createEmptyAttributeRow()]);
  }

  return (
    <div>
      <div className="form-subsection-head" style={{ marginBottom: 8 }}>
        <Text strong>SKU属性</Text>
        <Button size="small" icon={<PlusOutlined />} onClick={addRow}>新增属性</Button>
      </div>
      <Text type="secondary" className="product-sku-hint" style={{ fontSize: 12 }}>
        使用键值对维护 SKU 属性，提交时会自动转换为 JSON。示例：<code>{"{\"颜色\":\"银色\"}"}</code>
      </Text>
      {value.map((row) => (
        <div key={row.rowId} className="product-spec-row">
          <Input
            value={row.key}
            placeholder="属性名，如 颜色"
            onChange={(event) => updateRow(row.rowId, "key", event.target.value)}
          />
          <Input
            value={row.value}
            placeholder="属性值，如 银色"
            onChange={(event) => updateRow(row.rowId, "value", event.target.value)}
          />
          <Button danger icon={<DeleteOutlined />} onClick={() => removeRow(row.rowId)} />
        </div>
      ))}
    </div>
  );
}
