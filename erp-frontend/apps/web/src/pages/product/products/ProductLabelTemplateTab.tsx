import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormSelect, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { App, Button, Card, Empty, Space, Table, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchLabelTemplates, saveLabelTemplate } from "../../../api/product";
import type { LabelTemplateRecord } from "../../../types/product";

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
      await saveLabelTemplate({ ...editingTemplate, ...values });
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

      <ModalForm<LabelTemplateRecord>
        title={editingTemplate ? "编辑标签模板" : "新增标签模板"}
        open={open}
        initialValues={editingTemplate ?? { widthMm: 50, heightMm: 30, status: 1, templateConfig: "{\"elements\":[]}" }}
        modalProps={{ destroyOnClose: true, onCancel: () => { setOpen(false); setEditingTemplate(null); } }}
        onFinish={handleSave}
      >
        <ProFormText name="name" label="模板名称" rules={[{ required: true }]} />
        <ProFormDigit name="widthMm" label="宽度(mm)" min={1} fieldProps={{ precision: 1 }} rules={[{ required: true }]} />
        <ProFormDigit name="heightMm" label="高度(mm)" min={1} fieldProps={{ precision: 1 }} rules={[{ required: true }]} />
        <ProFormSelect name="status" label="状态" options={[{ label: "启用", value: 1 }, { label: "禁用", value: 0 }]} />
        <ProFormTextArea
          name="templateConfig"
          label="模板配置(JSON字符串)"
          fieldProps={{ autoSize: { minRows: 3, maxRows: 8 } }}
          extra={<span>示例：<code>{"{\"elements\":[]}"}</code></span>}
          rules={[{ required: true }]}
        />
        <ProFormText name="previewImage" label="预览图地址" />
      </ModalForm>
    </div>
  );
}
