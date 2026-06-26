import { DeleteOutlined, PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormSelect, ProFormText } from "@ant-design/pro-components";
import { App, Button, Card, Col, Row, Space, Table, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useState } from "react";
import { AttributeEditor } from "./AttributeEditor";
import {
  createEmptySkuRow,
  parseAttributes,
  serializeAttributes,
  createRowId,
  type EditableSkuRow
} from "./types";

const skuStatusOptions = [
  { label: "启用", value: 1 },
  { label: "禁用", value: 0 }
];

export function SkuListField({ value, onChange, canViewCost }: {
  value: EditableSkuRow[];
  onChange: (rows: EditableSkuRow[]) => void;
  canViewCost?: boolean;
}) {
  const { message } = App.useApp();
  const [modalOpen, setModalOpen] = useState(false);
  const [editingRowId, setEditingRowId] = useState<string | null>(null);
  const [attributeRows, setAttributeRows] = useState(() => [createRowId()].map((rowId) => ({ rowId, key: "", value: "" })));

  const editingSku = editingRowId ? value.find((row) => row.rowId === editingRowId) ?? null : null;

  const columns: ColumnsType<EditableSkuRow> = [
    { title: "SKU编码", dataIndex: "skuCode", key: "skuCode", width: 160 },
    { title: "属性", dataIndex: "attributes", key: "attributes", render: (v) => <code>{v}</code> },
    { title: "条码", dataIndex: "barcode", key: "barcode", width: 140 },
    { title: "售价", dataIndex: "price", key: "price", width: 90 },
    ...(canViewCost ? [{ title: "成本价", dataIndex: "costPrice", key: "costPrice", width: 90 } as ColumnsType<EditableSkuRow>[number]] : []),
    { title: "重量", dataIndex: "weight", key: "weight", width: 90 },
    {
      title: "状态", dataIndex: "status", key: "status", width: 90,
      render: (v) => <Tag color={v === 1 ? "success" : "default"}>{v === 1 ? "启用" : "禁用"}</Tag>
    },
    {
      title: "操作", key: "actions", width: 140,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" onClick={() => openEdit(record.rowId)}>编辑</Button>
          <Button type="link" danger onClick={() => onChange(value.filter((r) => r.rowId !== record.rowId))}>删除</Button>
        </Space>
      )
    }
  ];

  function openCreate() {
    setEditingRowId(null);
    setAttributeRows([{ rowId: createRowId(), key: "", value: "" }]);
    setModalOpen(true);
  }

  function openEdit(rowId: string) {
    setEditingRowId(rowId);
    const row = value.find((r) => r.rowId === rowId);
    setAttributeRows(parseAttributes(row?.attributes));
    setModalOpen(true);
  }

  async function handleSaveSku(values: any) {
    const attributes = serializeAttributes(attributeRows);
    if (!attributes) {
      message.error("至少需要维护一个SKU属性");
      return false;
    }
    const nextRow: EditableSkuRow = {
      ...values,
      attributes,
      rowId: editingRowId ?? createRowId()
    };
    onChange(editingRowId
      ? value.map((r) => (r.rowId === editingRowId ? nextRow : r))
      : [...value, nextRow]);
    setModalOpen(false);
    setEditingRowId(null);
    return true;
  }

  return (
    <>
      <Table
        rowKey="rowId"
        columns={columns}
        dataSource={value}
        pagination={false}
        size="small"
        locale={{ emptyText: "暂无SKU，请新增" }}
        scroll={{ x: 1000 }}
      />
      <Button type="primary" icon={<PlusOutlined />} onClick={openCreate} style={{ marginTop: 12 }}>
        新增SKU
      </Button>

      <ModalForm
        title={editingSku ? "编辑SKU" : "新增SKU"}
        open={modalOpen}
        width={720}
        grid
        rowProps={{ gutter: 16 }}
        initialValues={editingSku ?? createEmptySkuRow()}
        modalProps={{
          destroyOnClose: true,
          onCancel: () => { setModalOpen(false); setEditingRowId(null); }
        }}
        onFinish={handleSaveSku}
      >
        <ProFormText name="skuCode" label="SKU编码" rules={[{ required: true }]} colProps={{ xs: 24, md: 12 }} />
        <ProFormText name="barcode" label="条码" colProps={{ xs: 24, md: 12 }} />
        <Card size="small" title="SKU属性" className="form-section-card" styles={{ body: { padding: 16 } }} style={{ gridColumn: "1 / -1" }}>
          <AttributeEditor value={attributeRows} onChange={setAttributeRows} />
        </Card>
        <ProFormDigit name="price" label="售价" min={0} fieldProps={{ precision: 2 }} colProps={{ xs: 24, md: 6 }} />
        {canViewCost ? <ProFormDigit name="costPrice" label="成本价" min={0} fieldProps={{ precision: 2 }} colProps={{ xs: 24, md: 6 }} /> : null}
        <ProFormDigit name="weight" label="重量" min={0} fieldProps={{ precision: 3 }} colProps={{ xs: 24, md: 6 }} />
        <ProFormSelect name="status" label="状态" options={skuStatusOptions} colProps={{ xs: 24, md: 6 }} />
      </ModalForm>
    </>
  );
}
