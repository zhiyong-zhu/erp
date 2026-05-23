import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormSelect, ProFormText } from "@ant-design/pro-components";
import { App, Button, Card, Empty, Space, Table, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { fetchProductBom, saveProductBom } from "../../../api/product";
import type { ProductBomItemRecord, ProductBomRecord } from "../../../types/product";

type EditableBomItem = ProductBomItemRecord & { rowId: string };

export function ProductBomTab({ productId, canUpdate, height }: {
  productId: string;
  canUpdate: boolean;
  height: number;
}) {
  const { message } = App.useApp();
  const [bom, setBom] = useState<ProductBomRecord | null>(null);
  const [items, setItems] = useState<EditableBomItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<EditableBomItem | null>(null);

  useEffect(() => {
    void loadBom();
  }, [productId]);

  async function loadBom() {
    setLoading(true);
    try {
      const data = await fetchProductBom(productId);
      setBom(data);
      setItems((data.items ?? []).map((item) => ({ ...item, rowId: item.id ?? createRowId() })));
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载BOM失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleSave(values: EditableBomItem) {
    const nextRow: EditableBomItem = {
      ...values,
      rowId: editingItem?.rowId ?? createRowId()
    };
    const nextItems = editingItem
      ? items.map((item) => (item.rowId === editingItem.rowId ? nextRow : item))
      : [...items, nextRow];
    try {
      const saved = await saveProductBom(productId, {
        version: bom?.version ?? "V1.0",
        status: bom?.status ?? 1,
        effectiveDate: bom?.effectiveDate,
        items: nextItems.map(({ rowId, ...item }) => item)
      });
      setBom(saved);
      setItems((saved.items ?? []).map((item) => ({ ...item, rowId: item.id ?? createRowId() })));
      setOpen(false);
      setEditingItem(null);
      message.success("BOM已保存");
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "BOM保存失败");
      return false;
    }
  }

  async function handleDelete(rowId: string) {
    const nextItems = items.filter((item) => item.rowId !== rowId);
    try {
      const saved = await saveProductBom(productId, {
        version: bom?.version ?? "V1.0",
        status: bom?.status ?? 1,
        effectiveDate: bom?.effectiveDate,
        items: nextItems.map(({ rowId: _rowId, ...item }) => item)
      });
      setBom(saved);
      setItems((saved.items ?? []).map((item) => ({ ...item, rowId: item.id ?? createRowId() })));
      message.success("BOM项已删除");
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "BOM删除失败");
    }
  }

  const columns: ColumnsType<EditableBomItem> = [
    { title: "物料ID", dataIndex: "materialId", key: "materialId" },
    {
      title: "类型",
      dataIndex: "materialType",
      key: "materialType",
      width: 90,
      render: (value) => <Tag>{value === 2 ? "半成品" : "原料"}</Tag>
    },
    { title: "数量", dataIndex: "quantity", key: "quantity", width: 90 },
    { title: "单位", dataIndex: "unit", key: "unit", width: 90 },
    { title: "损耗率%", dataIndex: "lossRate", key: "lossRate", width: 100 },
    { title: "备注", dataIndex: "remark", key: "remark" },
    {
      title: "操作",
      key: "actions",
      width: 140,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" disabled={!canUpdate} onClick={() => { setEditingItem(record); setOpen(true); }}>编辑</Button>
          <Button type="link" danger disabled={!canUpdate} onClick={() => void handleDelete(record.rowId)}>删除</Button>
        </Space>
      )
    }
  ];

  return (
    <div className="product-detail-tab-scroll" style={{ height }}>
      <Card
        size="small"
        extra={<Button type="primary" disabled={!canUpdate} icon={<PlusOutlined />} onClick={() => { setEditingItem(null); setOpen(true); }}>新增BOM项</Button>}
      >
        <Space direction="vertical" size={12} style={{ width: "100%", marginBottom: 12 }}>
          <Tag color="blue">版本：{bom?.version ?? "V1.0"}</Tag>
          <Tag color={bom?.status === 1 ? "success" : "default"}>{bom?.status === 1 ? "有效" : "无效"}</Tag>
        </Space>
        <Table
          rowKey="rowId"
          columns={columns}
          dataSource={items}
          loading={loading}
          pagination={false}
          locale={{ emptyText: <Empty description="暂无BOM" /> }}
          scroll={{ x: 960, y: Math.max(height - 90, 120) }}
        />
      </Card>

      <ModalForm<EditableBomItem>
        title={editingItem ? "编辑BOM项" : "新增BOM项"}
        open={open}
        initialValues={editingItem ?? { materialType: 1, quantity: 1, lossRate: 0, sortOrder: 0 }}
        modalProps={{ destroyOnClose: true, onCancel: () => { setOpen(false); setEditingItem(null); } }}
        onFinish={handleSave}
      >
        <ProFormText name="materialId" label="物料ID" rules={[{ required: true }]} />
        <ProFormSelect name="materialType" label="物料类型" options={[{ label: "原料", value: 1 }, { label: "半成品", value: 2 }]} />
        <ProFormDigit name="quantity" label="数量" min={0.0001} fieldProps={{ precision: 4 }} rules={[{ required: true }]} />
        <ProFormText name="unit" label="单位" />
        <ProFormDigit name="lossRate" label="损耗率%" min={0} fieldProps={{ precision: 2 }} />
        <ProFormText name="remark" label="备注" />
        <ProFormDigit name="sortOrder" label="排序" min={0} fieldProps={{ precision: 0 }} />
      </ModalForm>
    </div>
  );
}

function createRowId() {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return crypto.randomUUID();
  }
  return `bom-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}
