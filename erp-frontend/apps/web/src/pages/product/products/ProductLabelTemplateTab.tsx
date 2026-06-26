import { PlusOutlined } from "@ant-design/icons";
import { App, Button, Card, Empty, Space, Table, Tag, Typography } from "antd";
import { CreateForm } from "../../../components/CreateForm";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchLabelTemplates, saveLabelTemplate } from "../../../api/product";
import type { LabelTemplateRecord } from "../../../types/product";

const { Text } = Typography;

export function ProductLabelTemplateTab({ height, canUpdate }: { height: number; canUpdate: boolean }) {
  const { message } = App.useApp();
  const [templates, setTemplates] = useState<LabelTemplateRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [editingTemplate, setEditingTemplate] = useState<LabelTemplateRecord | null>(null);

  useEffect(() => {
    void loadTemplates();
  }, []);

  async function loadTemplates() {
    setLoading(true);
    try {
      setTemplates(await fetchLabelTemplates());
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载标签模板失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleSave(values: LabelTemplateRecord) {
    try {
      const nextPayload = {
        ...editingTemplate,
        ...values,
        templateConfig: values.templateConfigDraft ?? values.templateConfig
      };
      if (!isValidJson(nextPayload.templateConfig)) {
        message.error("模板配置必须是合法JSON");
        return false;
      }
      await saveLabelTemplate(nextPayload);
      message.success("标签模板保存成功");
      setOpen(false);
      setEditingTemplate(null);
      await loadTemplates();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "标签模板保存失败");
      return false;
    }
  }

  const columns: ColumnsType<LabelTemplateRecord> = [
    { title: "模板名称", dataIndex: "name", key: "name" },
    { title: "宽(mm)", dataIndex: "widthMm", key: "widthMm", width: 100 },
    { title: "高(mm)", dataIndex: "heightMm", key: "heightMm", width: 100 },
    {
      title: "配置摘要",
      dataIndex: "templateConfig",
      key: "templateConfig",
      render: (value) => <Text type="secondary">{templateSummary(value)}</Text>
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 90,
      render: (value) => <Tag color={value === 1 ? "success" : "default"}>{value === 1 ? "启用" : "禁用"}</Tag>
    },
    {
      title: "操作",
      key: "actions",
      width: 120,
      render: (_, record) => <Button type="link" disabled={!canUpdate} onClick={() => { setEditingTemplate(record); setOpen(true); }}>编辑</Button>
    }
  ];

  return (
    <div className="product-detail-tab-scroll" style={{ height }}>
      <Card
        size="small"
        extra={<Button type="primary" disabled={!canUpdate} icon={<PlusOutlined />} onClick={() => { setEditingTemplate(null); setOpen(true); }}>新增标签模板</Button>}
      >
        <Space direction="vertical" size={12} style={{ width: "100%", marginBottom: 12 }}>
          <Text type="secondary">
            标签模板当前先以 JSON 配置为主，后续再扩展可视化设计器。配置中建议至少包含 `elements` 数组。
          </Text>
        </Space>
        <Table
          rowKey={(record) => record.id ?? record.name}
          columns={columns}
          dataSource={templates}
          loading={loading}
          pagination={false}
          locale={{ emptyText: <Empty description="暂无标签模板" /> }}
          scroll={{ x: 760, y: Math.max(height - 90, 120) }}
        />
      </Card>

      <CreateForm
        title={editingTemplate ? "编辑标签模板" : "新增标签模板"}
        open={open}
        width={720}
        initialValues={editingTemplate ? {
          ...editingTemplate,
          templateConfigDraft: editingTemplate.templateConfig
        } : {
          widthMm: 50,
          heightMm: 30,
          status: 1,
          templateConfigDraft: "{\"elements\":[]}"
        }}
        onCancel={() => { setOpen(false); setEditingTemplate(null); }}
        onFinish={handleSave}
        sections={[
          {
            title: "模板信息",
            fields: [
              { type: "text", name: "name", label: "模板名称", rules: [{ required: true }], colSpan: 24 },
              { type: "digit", name: "widthMm", label: "宽度(mm)", min: 1, precision: 1, rules: [{ required: true }], colSpan: 8 },
              { type: "digit", name: "heightMm", label: "高度(mm)", min: 1, precision: 1, rules: [{ required: true }], colSpan: 8 },
              { type: "select", name: "status", label: "状态", options: [{ label: "启用", value: 1 }, { label: "禁用", value: 0 }], colSpan: 8 },
              { type: "textarea", name: "templateConfigDraft", label: "模板配置(JSON字符串)", autoSize: { minRows: 3, maxRows: 8 }, rules: [{ required: true }], colSpan: 24 },
              { type: "text", name: "previewImage", label: "预览图地址", colSpan: 24 }
            ]
          }
        ]}
      />
    </div>
  );
}

function isValidJson(raw?: string) {
  if (!raw) {
    return false;
  }
  try {
    JSON.parse(raw);
    return true;
  } catch {
    return false;
  }
}

function templateSummary(raw?: string) {
  if (!raw) {
    return "无配置";
  }
  try {
    const parsed = JSON.parse(raw) as { elements?: unknown[]; width?: number; height?: number; unit?: string };
    const count = Array.isArray(parsed.elements) ? parsed.elements.length : 0;
    const size = parsed.width && parsed.height ? `${parsed.width}×${parsed.height}${parsed.unit ?? "mm"}` : "未定义尺寸";
    return `${size} · ${count}个元素`;
  } catch {
    return "配置格式异常";
  }
}
