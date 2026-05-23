import { ModalForm, ProFormDigit, ProFormSelect } from "@ant-design/pro-components";
import { App, Button, Card, Descriptions, Empty, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { fetchLabelTemplates, fetchProductPackages, previewLabelPrint } from "../../../api/product";
import type { LabelPrintResult, LabelTemplateRecord, ProductPackageRecord, ProductRecord } from "../../../types/product";

const { Text } = Typography;

export function ProductLabelPrintTab({ product, height, canPrint }: {
  product: ProductRecord;
  height: number;
  canPrint: boolean;
}) {
  const { message } = App.useApp();
  const [packages, setPackages] = useState<ProductPackageRecord[]>([]);
  const [templates, setTemplates] = useState<LabelTemplateRecord[]>([]);
  const [result, setResult] = useState<LabelPrintResult | null>(null);
  const [open, setOpen] = useState(false);

  useEffect(() => {
    void Promise.all([
      fetchProductPackages(product.id).then(setPackages).catch(() => setPackages([])),
      fetchLabelTemplates().then(setTemplates).catch(() => setTemplates([]))
    ]);
  }, [product.id]);

  const skuOptions = useMemo(
    () => (product.skus ?? []).map((sku) => ({ label: sku.skuCode, value: sku.id ?? sku.skuCode })),
    [product.skus]
  );

  const packageOptions = useMemo(
    () => packages.map((pack) => ({ label: `${levelLabel(pack.level)} / ${pack.name}`, value: pack.level })),
    [packages]
  );

  const templateOptions = useMemo(
    () => templates.map((template) => ({ label: template.name, value: template.id! })),
    [templates]
  );

  const templateColumns: ColumnsType<LabelTemplateRecord> = [
    { title: "模板名称", dataIndex: "name", key: "name" },
    { title: "尺寸", key: "size", render: (_, record) => `${record.widthMm} × ${record.heightMm} mm` },
    { title: "状态", dataIndex: "status", key: "status", render: (value) => <Tag color={value === 1 ? "success" : "default"}>{value === 1 ? "启用" : "禁用"}</Tag> }
  ];

  async function handlePreview(values: { skuId: string; packageLevel: number; labelTemplateId: string; quantity: number; printMode: string }) {
    try {
      const preview = await previewLabelPrint({
        items: [{
          skuId: values.skuId,
          packageLevel: values.packageLevel,
          labelTemplateId: values.labelTemplateId,
          quantity: values.quantity
        }],
        printMode: values.printMode,
        printerId: undefined
      });
      setResult(preview);
      setOpen(false);
      message.success("标签预览生成成功");
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "标签预览生成失败");
      return false;
    }
  }

  return (
    <div className="product-detail-tab-scroll" style={{ height }}>
      <Card
        size="small"
        extra={<Button type="primary" disabled={!canPrint} onClick={() => setOpen(true)}>生成标签预览</Button>}
      >
        <Descriptions column={2} bordered size="small" style={{ marginBottom: 16 }}>
          <Descriptions.Item label="当前产品">{product.name}</Descriptions.Item>
          <Descriptions.Item label="SKU数量">{product.skus?.length ?? 0}</Descriptions.Item>
          <Descriptions.Item label="包装层级数量">{packages.length}</Descriptions.Item>
          <Descriptions.Item label="标签模板数量">{templates.length}</Descriptions.Item>
        </Descriptions>

        <Table
          rowKey={(record) => record.id ?? record.name}
          columns={templateColumns}
          dataSource={templates}
          pagination={false}
          locale={{ emptyText: <Empty description="暂无标签模板" /> }}
          scroll={{ x: 680, y: Math.max(height - 240, 120) }}
        />

        {result ? (
          <Card size="small" title="最近一次预览结果" className="product-spec-card" style={{ marginTop: 16 }}>
            <Text>总数量：{result.totalCount}</Text>
            <br />
            <Text>预览地址：<code>{result.pdfUrl}</code></Text>
            <br />
            <Text type="secondary">摘要：{result.summary}</Text>
            {result.previewHtml ? (
              <div
                style={{ marginTop: 16, padding: 12, borderRadius: 12, background: "#f8fafc" }}
                dangerouslySetInnerHTML={{ __html: result.previewHtml }}
              />
            ) : null}
          </Card>
        ) : null}
      </Card>

      <ModalForm<{ skuId: string; packageLevel: number; labelTemplateId: string; quantity: number; printMode: string }>
        title="标签预览"
        open={open}
        initialValues={{ quantity: 1, printMode: "pdf" }}
        modalProps={{ destroyOnClose: true, onCancel: () => setOpen(false) }}
        onFinish={handlePreview}
      >
        <ProFormSelect name="skuId" label="SKU" options={skuOptions} rules={[{ required: true }]} />
        <ProFormSelect name="packageLevel" label="包装层级" options={packageOptions} rules={[{ required: true }]} />
        <ProFormSelect name="labelTemplateId" label="标签模板" options={templateOptions} rules={[{ required: true }]} />
        <ProFormDigit name="quantity" label="打印数量" min={1} fieldProps={{ precision: 0 }} rules={[{ required: true }]} />
        <ProFormSelect name="printMode" label="打印模式" options={[{ label: "PDF预览", value: "pdf" }]} rules={[{ required: true }]} />
      </ModalForm>
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
