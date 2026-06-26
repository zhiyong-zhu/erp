import { PlusOutlined } from "@ant-design/icons";
import { App, Button, Card, Empty, Space, Table, Tag, Typography } from "antd";
import { CreateForm } from "../../../components/CreateForm";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { fetchLabelTemplates, fetchProductPackages, saveProductPackages } from "../../../api/product";
import type { LabelTemplateRecord, ProductPackageRecord } from "../../../types/product";

const { Text } = Typography;

export function ProductPackageTab({ productId, canUpdatePackages, height }: {
  productId: string;
  canUpdatePackages: boolean;
  height: number;
}) {
  const { message } = App.useApp();
  const [packages, setPackages] = useState<ProductPackageRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [editingPackage, setEditingPackage] = useState<ProductPackageRecord | null>(null);
  const [labelTemplates, setLabelTemplates] = useState<LabelTemplateRecord[]>([]);

  useEffect(() => {
    void loadPackages();
    void loadLabelTemplates();
  }, [productId]);

  const levelOptions = useMemo(
    () => [1, 2, 3].map((level) => ({
      label: levelLabel(level),
      value: level,
      disabled: packages.some((item) => item.level === level && item.level !== editingPackage?.level)
    })),
    [packages, editingPackage]
  );

  const packageSummary = useMemo(() => buildPackageSummary(packages), [packages]);

  async function loadPackages() {
    setLoading(true);
    try {
      setPackages(sortPackages(await fetchProductPackages(productId)));
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载包装规格失败");
    } finally {
      setLoading(false);
    }
  }

  async function loadLabelTemplates() {
    try {
      setLabelTemplates(await fetchLabelTemplates());
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载标签模板失败");
      setLabelTemplates([]);
    }
  }

  async function handleSave(values: ProductPackageRecord) {
    const normalizedPayload = {
      id: values.id,
      level: values.level,
      name: values.name,
      quantity: values.quantity,
      weight: values.weight,
      barcode: values.barcode,
      labelTemplateId: values.labelTemplateId,
      dimensions: serializeDimensions(values.dimensionsDraft)
    };
    const nextPackages = editingPackage
      ? packages.map((item) => (item.level === editingPackage.level ? { ...pickPackagePayload(editingPackage), ...normalizedPayload } : pickPackagePayload(item)))
      : [...packages.map((item) => pickPackagePayload(item)), normalizedPayload];

    if (hasDuplicateLevel(nextPackages)) {
      message.error("同一产品的包装层级不能重复");
      return false;
    }

    try {
      setPackages(sortPackages(await saveProductPackages(productId, nextPackages)));
      setOpen(false);
      setEditingPackage(null);
      message.success("包装规格保存成功");
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "包装规格保存失败");
      return false;
    }
  }

  async function handleDelete(record: ProductPackageRecord) {
    try {
      const nextPackages = packages.filter((item) => item.level !== record.level);
      setPackages(sortPackages(await saveProductPackages(productId, nextPackages)));
      message.success("包装规格删除成功");
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "包装规格删除失败");
    }
  }

  const columns: ColumnsType<ProductPackageRecord> = [
    { title: "层级", dataIndex: "level", key: "level", width: 90, render: (value) => levelLabel(value) },
    { title: "名称", dataIndex: "name", key: "name" },
    { title: "装入数量", dataIndex: "quantity", key: "quantity", width: 100 },
    { title: "重量", dataIndex: "weight", key: "weight", width: 100 },
    { title: "条码", dataIndex: "barcode", key: "barcode", width: 140 },
    {
      title: "标签模板",
      dataIndex: "labelTemplateId",
      key: "labelTemplateId",
      width: 160,
      render: (value) => labelTemplates.find((template) => template.id === value)?.name ?? "-"
    },
    {
      title: "尺寸",
      dataIndex: "dimensions",
      key: "dimensions",
      render: (value) => formatDimensions(value)
    },
    {
      title: "操作",
      key: "actions",
      width: 140,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" disabled={!canUpdatePackages} onClick={() => { setEditingPackage(record); setOpen(true); }}>编辑</Button>
          <Button type="link" danger disabled={!canUpdatePackages} onClick={() => void handleDelete(record)}>删除</Button>
        </Space>
      )
    }
  ];

  return (
    <div className="product-detail-tab-scroll" style={{ height }}>
      <Card
        size="small"
        extra={<Button type="primary" disabled={!canUpdatePackages} icon={<PlusOutlined />} onClick={() => { setEditingPackage(null); setOpen(true); }}>新增包装规格</Button>}
      >
        <Space direction="vertical" size={12} style={{ width: "100%", marginBottom: 12 }}>
          <Text type="secondary">
            包装层级规则：同一产品只允许一条“单品 / 内盒 / 外箱”记录。系统会自动显示层级换算摘要。
          </Text>
          {packageSummary.length > 0 ? (
            <Space wrap>
              {packageSummary.map((summary) => <Tag color="blue" key={summary}>{summary}</Tag>)}
            </Space>
          ) : null}
        </Space>
        <Table
          rowKey={(record) => record.id ?? `${record.level}-${record.name}`}
          columns={columns}
          dataSource={packages}
          loading={loading}
          pagination={false}
          locale={{ emptyText: <Empty description="暂无包装规格" /> }}
          scroll={{ x: 900, y: Math.max(height - 90, 120) }}
        />
      </Card>

      <CreateForm
        title={editingPackage ? "编辑包装规格" : "新增包装规格"}
        open={open}
        width={720}
        initialValues={editingPackage ? {
          ...editingPackage,
          dimensionsDraft: parseDimensions(editingPackage.dimensions)
        } : {
          level: 1,
          quantity: 1,
          dimensionsDraft: { unit: "cm" }
        }}
        onCancel={() => { setOpen(false); setEditingPackage(null); }}
        onFinish={handleSave}
        sections={[
          {
            title: "包装规格",
            fields: [
              { type: "select", name: "level", label: "包装层级", options: levelOptions, rules: [{ required: true }], colSpan: 12 },
              { type: "text", name: "name", label: "包装名称", rules: [{ required: true }], colSpan: 12 },
              { type: "digit", name: "quantity", label: "装入数量", min: 1, precision: 0, rules: [{ required: true }], colSpan: 12 },
              { type: "digit", name: "weight", label: "重量", min: 0, precision: 3, colSpan: 12 },
              { type: "text", name: "barcode", label: "条码", colSpan: 12 },
              { type: "select", name: "labelTemplateId", label: "关联标签模板", options: labelTemplates.map((template) => ({ label: template.name, value: template.id! })), colSpan: 12, fieldProps: { allowClear: true } }
            ]
          },
          {
            title: "尺寸",
            fields: [
              { type: "digit", name: ["dimensionsDraft", "length"], label: "长度", min: 0, precision: 1, colSpan: 6 },
              { type: "digit", name: ["dimensionsDraft", "width"], label: "宽度", min: 0, precision: 1, colSpan: 6 },
              { type: "digit", name: ["dimensionsDraft", "height"], label: "高度", min: 0, precision: 1, colSpan: 6 },
              { type: "select", name: ["dimensionsDraft", "unit"], label: "尺寸单位", options: [{ label: "cm", value: "cm" }, { label: "mm", value: "mm" }], colSpan: 6 }
            ]
          }
        ]}
      />
    </div>
  );
}

function levelLabel(level: number) {
  if (level === 1) {
    return "单品";
  }
  if (level === 2) {
    return "内盒";
  }
  return "外箱";
}

function pickPackagePayload(record: ProductPackageRecord): ProductPackageRecord {
  return {
    id: record.id,
    level: record.level,
    name: record.name,
    quantity: record.quantity,
    weight: record.weight,
    dimensions: record.dimensions,
    barcode: record.barcode,
    labelTemplateId: record.labelTemplateId
  };
}

function sortPackages(records: ProductPackageRecord[]) {
  return [...records].sort((a, b) => a.level - b.level);
}

function hasDuplicateLevel(records: ProductPackageRecord[]) {
  const set = new Set<number>();
  for (const record of records) {
    if (set.has(record.level)) {
      return true;
    }
    set.add(record.level);
  }
  return false;
}

function parseDimensions(raw?: string | null) {
  if (!raw) {
    return { unit: "cm" };
  }
  try {
    return JSON.parse(raw);
  } catch {
    return { unit: "cm" };
  }
}

function serializeDimensions(draft?: ProductPackageRecord["dimensionsDraft"]) {
  if (!draft) {
    return undefined;
  }
  const payload = {
    length: draft.length ?? undefined,
    width: draft.width ?? undefined,
    height: draft.height ?? undefined,
    unit: draft.unit ?? "cm"
  };
  if (payload.length == null && payload.width == null && payload.height == null) {
    return undefined;
  }
  return JSON.stringify(payload);
}

function formatDimensions(raw?: string | null) {
  if (!raw) {
    return "-";
  }
  try {
    const parsed = JSON.parse(raw) as { length?: number; width?: number; height?: number; unit?: string };
    if (parsed.length == null && parsed.width == null && parsed.height == null) {
      return "-";
    }
    return `${parsed.length ?? "-"} × ${parsed.width ?? "-"} × ${parsed.height ?? "-"} ${parsed.unit ?? "cm"}`;
  } catch {
    return raw;
  }
}

function buildPackageSummary(packages: ProductPackageRecord[]) {
  const sorted = sortPackages(packages);
  const single = sorted.find((item) => item.level === 1);
  const inner = sorted.find((item) => item.level === 2);
  const outer = sorted.find((item) => item.level === 3);
  const summary: string[] = [];

  if (inner && single) {
    summary.push(`1${inner.name} = ${inner.quantity}${single.name}`);
  }
  if (outer && inner && single) {
    summary.push(`1${outer.name} = ${outer.quantity}${inner.name} = ${outer.quantity * inner.quantity}${single.name}`);
  } else if (outer && single) {
    summary.push(`1${outer.name} = ${outer.quantity}${single.name}`);
  }
  return summary;
}
