import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormSelect, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { App, Button, Form, Input, Space, Table, Tag, Typography } from "antd";
import { PRODUCTION_PERMISSIONS } from "@erp/shared";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchProducts } from "../../../api/product";
import { createProductionBatch, fetchProductionBatches, updateProductionBatch } from "../../../api/production";
import { hasPermission } from "../../../store/auth";
import type { ProductRecord } from "../../../types/product";
import type { ProductionBatchPayload, ProductionBatchRecord } from "../../../types/production";

const { Title, Text } = Typography;

const batchStatusOptions = [
  { label: "草稿", value: "DRAFT" },
  { label: "已下达", value: "RELEASED" },
  { label: "生产中", value: "IN_PROGRESS" },
  { label: "已完工", value: "COMPLETED" },
  { label: "已关闭", value: "CLOSED" }
];

type ProductSelectOption = {
  label: string;
  value: string;
  unit?: string | null;
};

export function ProductionBatchPage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<ProductionBatchRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<ProductionBatchRecord | null>(null);
  const { message } = App.useApp();
  const canCreate = hasPermission(PRODUCTION_PERMISSIONS.BATCH_CREATE);
  const canUpdate = hasPermission(PRODUCTION_PERMISSIONS.BATCH_UPDATE);

  useEffect(() => {
    void loadData();
  }, []);

  async function loadData(nextPageNum = pageNum, nextPageSize = pageSize, batchNo = keyword) {
    setLoading(true);
    try {
      const data = await fetchProductionBatches({ pageNum: nextPageNum, pageSize: nextPageSize, batchNo });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载生产批次失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(values: ProductionBatchPayload) {
    try {
      await createProductionBatch(values);
      message.success("生产批次已创建");
      setCreateOpen(false);
      await loadData(1, pageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "创建生产批次失败");
      return false;
    }
  }

  async function handleUpdate(values: ProductionBatchPayload) {
    if (!editingRecord?.id) return false;
    try {
      await updateProductionBatch(editingRecord.id, values);
      message.success("生产批次已更新");
      setEditingRecord(null);
      await loadData();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "更新生产批次失败");
      return false;
    }
  }

  const columns: ColumnsType<ProductionBatchRecord> = [
    { title: "批次号", dataIndex: "batchNo", key: "batchNo", width: 170 },
    { title: "产品", dataIndex: "productName", key: "productName", render: (value, record) => value ?? record.productCode ?? record.productId },
    { title: "计划数", dataIndex: "plannedQuantity", key: "plannedQuantity", width: 110 },
    { title: "完成数", dataIndex: "completedQuantity", key: "completedQuantity", width: 110, render: (value) => value ?? 0 },
    { title: "单位", dataIndex: "unit", key: "unit", width: 80 },
    { title: "工艺", dataIndex: "processName", key: "processName", width: 160, render: (value, record) => value ?? record.processId ?? "-" },
    { title: "BOM", dataIndex: "bomCode", key: "bomCode", width: 140, render: (value, record) => value ?? record.bomId ?? "-" },
    { title: "状态", dataIndex: "status", key: "status", width: 130, render: (value: string) => <Tag>{formatStatus(value)}</Tag> },
    { title: "计划开始", dataIndex: "plannedStartDate", key: "plannedStartDate", width: 120 },
    { title: "计划结束", dataIndex: "plannedEndDate", key: "plannedEndDate", width: 120 },
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
          <Title level={3} style={{ margin: 0 }}>生产管理 / 生产工单</Title>
          <Text type="secondary">维护生产计划工单、计划产量、关联工艺和BOM，执行动作在生产执行页完成。</Text>
        </div>
        <Space>
          <Input.Search
            allowClear
            placeholder="搜索批次"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            onSearch={(value) => {
              setKeyword(value);
              void loadData(1, pageSize, value);
            }}
            style={{ width: 240 }}
          />
          <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateOpen(true)}>
            新建工单
          </Button>
        </Space>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={records}
        loading={loading}
        scroll={{ x: 1300 }}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadData(nextPageNum, nextPageSize)
        }}
      />

      <BatchForm title="新建生产工单" open={createOpen} onCancel={() => setCreateOpen(false)} onFinish={handleCreate} />
      <BatchForm
        title="编辑生产工单"
        open={!!editingRecord}
        initialValues={editingRecord ? toBatchInitialValues(editingRecord) : undefined}
        initialProduct={editingRecord ? toProductSelectOption(editingRecord) : undefined}
        onCancel={() => setEditingRecord(null)}
        onFinish={handleUpdate}
      />
    </section>
  );
}

function BatchForm({
  title,
  open,
  initialValues,
  initialProduct,
  onCancel,
  onFinish
}: {
  title: string;
  open: boolean;
  initialValues?: Partial<ProductionBatchPayload>;
  initialProduct?: ProductSelectOption;
  onCancel: () => void;
  onFinish: (values: ProductionBatchPayload) => Promise<boolean>;
}) {
  const [form] = Form.useForm<ProductionBatchPayload>();
  const [productOptions, setProductOptions] = useState<ProductSelectOption[]>([]);

  useEffect(() => {
    if (!open) {
      setProductOptions([]);
      return;
    }
    void loadProductOptions("", initialProduct);
  }, [initialProduct?.value, open]);

  async function loadProductOptions(keyword = "", selectedProduct = initialProduct) {
    const data = await fetchProducts({ pageNum: 1, pageSize: 20, name: keyword || undefined });
    const nextOptions = data.records.map(toProductOption);
    setProductOptions(mergeProductOptions(selectedProduct ? [selectedProduct, ...nextOptions] : nextOptions));
  }

  return (
    <ModalForm<ProductionBatchPayload>
      title={title}
      open={open}
      width={920}
      grid
      form={form}
      rowProps={{ gutter: 16 }}
      initialValues={initialValues ?? { status: "DRAFT", completedQuantity: 0 }}
      modalProps={{ destroyOnClose: true, onCancel }}
      onFinish={onFinish}
    >
      <ProFormText name="batchNo" label="批次号" rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormSelect
        name="productId"
        label="产品"
        rules={[{ required: true }]}
        options={productOptions}
        colProps={{ xs: 24, md: 8 }}
        fieldProps={{
          showSearch: true,
          filterOption: false,
          placeholder: "搜索并选择产品",
          onSearch: (value) => void loadProductOptions(value),
          onChange: (_, option) => {
            const selected = Array.isArray(option) ? option[0] : option;
            form.setFieldsValue({ unit: (selected as ProductSelectOption | undefined)?.unit ?? undefined });
          }
        }}
      />
      <ProFormDigit name="plannedQuantity" label="计划数量" min={0.0001} fieldProps={{ precision: 4 }} rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormDigit name="completedQuantity" label="完成数量" min={0} fieldProps={{ precision: 4 }} colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="unit" label="单位" colProps={{ xs: 24, md: 8 }} />
      <ProFormSelect name="status" label="状态" options={batchStatusOptions} colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="processId" label="工艺ID" colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="bomId" label="BOM ID" colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="plannedStartDate" label="计划开始" placeholder="YYYY-MM-DD" colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="plannedEndDate" label="计划结束" placeholder="YYYY-MM-DD" colProps={{ xs: 24, md: 8 }} />
      <ProFormTextArea name="remark" label="备注" colProps={{ span: 24 }} />
    </ModalForm>
  );
}

function toProductOption(product: ProductRecord): ProductSelectOption {
  return {
    label: `${product.code} / ${product.name}`,
    value: product.id,
    unit: product.unit
  };
}

function toProductSelectOption(record: ProductionBatchRecord): ProductSelectOption {
  return {
    label: [record.productCode, record.productName].filter(Boolean).join(" / ") || record.productId,
    value: record.productId,
    unit: record.unit
  };
}

function mergeProductOptions(options: ProductSelectOption[]) {
  const optionMap = new Map<string, ProductSelectOption>();
  for (const option of options) {
    optionMap.set(option.value, option);
  }
  return Array.from(optionMap.values());
}

function formatStatus(status: string) {
  return batchStatusOptions.find((item) => item.value === status)?.label ?? status;
}

function toBatchInitialValues(record: ProductionBatchRecord): Partial<ProductionBatchPayload> {
  return {
    batchNo: record.batchNo,
    productId: record.productId,
    plannedQuantity: record.plannedQuantity,
    completedQuantity: record.completedQuantity ?? undefined,
    unit: record.unit ?? undefined,
    processId: record.processId,
    bomId: record.bomId,
    status: record.status,
    plannedStartDate: record.plannedStartDate ?? undefined,
    plannedEndDate: record.plannedEndDate ?? undefined,
    remark: record.remark ?? undefined
  };
}
