import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormList, ProFormSelect, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { App, Button, Input, Space, Table, Tag, Typography } from "antd";
import { PRODUCTION_PERMISSIONS } from "@erp/shared";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { createProductionBom, fetchProductionBoms, updateProductionBom } from "../../../api/production";
import { hasPermission } from "../../../store/auth";
import type { ProductionBomItemRecord, ProductionBomPayload, ProductionBomRecord } from "../../../types/production";

const { Title, Text } = Typography;

export function ProductionBomPage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<ProductionBomRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [productId, setProductId] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<ProductionBomRecord | null>(null);
  const { message } = App.useApp();
  const canCreate = hasPermission(PRODUCTION_PERMISSIONS.BOM_CREATE);
  const canUpdate = hasPermission(PRODUCTION_PERMISSIONS.BOM_UPDATE);

  useEffect(() => {
    void loadData();
  }, []);

  async function loadData(nextPageNum = pageNum, nextPageSize = pageSize, nextProductId = productId) {
    setLoading(true);
    try {
      const data = await fetchProductionBoms({ pageNum: nextPageNum, pageSize: nextPageSize, productId: nextProductId || undefined });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载生产BOM失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(values: ProductionBomPayload) {
    try {
      await createProductionBom(normalizePayload(values));
      message.success("生产BOM已创建");
      setCreateOpen(false);
      await loadData(1, pageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "创建生产BOM失败");
      return false;
    }
  }

  async function handleUpdate(values: ProductionBomPayload) {
    if (!editingRecord?.id) return false;
    try {
      await updateProductionBom(editingRecord.id, normalizePayload(values));
      message.success("生产BOM已更新");
      setEditingRecord(null);
      await loadData();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "更新生产BOM失败");
      return false;
    }
  }

  const columns: ColumnsType<ProductionBomRecord> = [
    { title: "BOM编码", dataIndex: "code", key: "code", width: 150 },
    { title: "产品", dataIndex: "productName", key: "productName", render: (value, record) => value ?? record.productCode ?? record.productId },
    { title: "版本", dataIndex: "version", key: "version", width: 100 },
    { title: "物料项", key: "items", width: 90, render: (_, record) => record.items?.length ?? 0 },
    { title: "生效日期", dataIndex: "effectiveDate", key: "effectiveDate", width: 140 },
    { title: "状态", dataIndex: "status", key: "status", width: 110, render: (value: number) => <Tag color={value === 1 ? "success" : "default"}>{value === 1 ? "启用" : "停用"}</Tag> },
    {
      title: "操作",
      key: "actions",
      width: 110,
      render: (_, record) => (
        <Button type="link" disabled={!canUpdate} onClick={() => setEditingRecord(record)}>
          编辑
        </Button>
      )
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>生产管理 / 生产BOM</Title>
          <Text type="secondary">维护产品生产用料、消耗数量、损耗率和对应工序。</Text>
        </div>
        <Space>
          <Input.Search
            allowClear
            placeholder="产品ID"
            value={productId}
            onChange={(event) => setProductId(event.target.value)}
            onSearch={(value) => {
              setProductId(value);
              void loadData(1, pageSize, value);
            }}
            style={{ width: 220 }}
          />
          <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateOpen(true)}>
            新建BOM
          </Button>
        </Space>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={records}
        loading={loading}
        expandable={{
          expandedRowRender: (record) => (
            <Table
              rowKey={(item) => String(item.id ?? item.materialId)}
              size="small"
              pagination={false}
              columns={[
                { title: "物料", dataIndex: "materialName", key: "materialName", render: (value, item) => value ?? item.materialCode ?? item.materialId },
                { title: "数量", dataIndex: "quantity", key: "quantity", width: 110 },
                { title: "单位", dataIndex: "unit", key: "unit", width: 90 },
                { title: "损耗率%", dataIndex: "lossRate", key: "lossRate", width: 120 },
                { title: "工序号", dataIndex: "processStepNo", key: "processStepNo", width: 110 },
                { title: "备注", dataIndex: "remark", key: "remark" }
              ]}
              dataSource={record.items ?? []}
            />
          )
        }}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadData(nextPageNum, nextPageSize)
        }}
      />

      <BomForm title="新建生产BOM" open={createOpen} onCancel={() => setCreateOpen(false)} onFinish={handleCreate} />
      <BomForm title="编辑生产BOM" open={!!editingRecord} initialValues={editingRecord ? toBomInitialValues(editingRecord) : undefined} onCancel={() => setEditingRecord(null)} onFinish={handleUpdate} />
    </section>
  );
}

function BomForm({
  title,
  open,
  initialValues,
  onCancel,
  onFinish
}: {
  title: string;
  open: boolean;
  initialValues?: Partial<ProductionBomPayload>;
  onCancel: () => void;
  onFinish: (values: ProductionBomPayload) => Promise<boolean>;
}) {
  return (
    <ModalForm<ProductionBomPayload>
      title={title}
      open={open}
      width={980}
      grid
      rowProps={{ gutter: 16 }}
      initialValues={initialValues ?? { version: "V1.0", status: 1, items: [{ materialId: "", quantity: 1, lossRate: 0 }] }}
      modalProps={{ destroyOnClose: true, onCancel }}
      onFinish={onFinish}
    >
      <ProFormText name="code" label="BOM编码" rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="productId" label="产品ID" rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="version" label="版本" rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="effectiveDate" label="生效日期" placeholder="YYYY-MM-DD" colProps={{ xs: 24, md: 8 }} />
      <ProFormSelect name="status" label="状态" options={[{ label: "启用", value: 1 }, { label: "停用", value: 0 }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormTextArea name="remark" label="备注" colProps={{ span: 24 }} />
      <ProFormList name="items" label="BOM物料明细" creatorButtonProps={{ creatorButtonText: "添加物料" }}>
        <ProFormText name="materialId" label="物料ID" rules={[{ required: true }]} />
        <ProFormDigit name="quantity" label="数量" min={0.0001} fieldProps={{ precision: 4 }} rules={[{ required: true }]} />
        <ProFormText name="unit" label="单位" />
        <ProFormDigit name="lossRate" label="损耗率%" min={0} fieldProps={{ precision: 2 }} />
        <ProFormDigit name="processStepNo" label="工序号" min={1} fieldProps={{ precision: 0 }} />
        <ProFormText name="remark" label="备注" />
      </ProFormList>
    </ModalForm>
  );
}

function normalizePayload(values: ProductionBomPayload): ProductionBomPayload {
  return {
    ...values,
    items: (values.items ?? []).map(({ rowId: _rowId, ...item }: ProductionBomItemRecord) => item)
  };
}

function toBomInitialValues(record: ProductionBomRecord): Partial<ProductionBomPayload> {
  return {
    code: record.code,
    productId: record.productId,
    version: record.version,
    status: record.status,
    effectiveDate: record.effectiveDate ?? undefined,
    remark: record.remark ?? undefined,
    items: record.items ?? []
  };
}
