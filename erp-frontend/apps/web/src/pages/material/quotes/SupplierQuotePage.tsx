import { PlusOutlined } from "@ant-design/icons";
import {
  ModalForm,
  ProFormDatePicker,
  ProFormDigit,
  ProFormSelect,
  ProFormText,
  ProFormTextArea
} from "@ant-design/pro-components";
import { App, Button, Table, Typography } from "antd";
import { MATERIAL_PERMISSIONS } from "@erp/shared";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import {
  createSupplierQuote,
  fetchMaterials,
  fetchSupplierQuotes,
  fetchSuppliers,
  updateSupplierQuote
} from "../../../api/material";
import { hasPermission } from "../../../store/auth";
import type {
  MaterialRecord,
  SupplierQuotePayload,
  SupplierQuoteRecord,
  SupplierRecord
} from "../../../types/material";

const { Title, Text } = Typography;

export function SupplierQuotePage() {
  const [loading, setLoading] = useState(false);
  const [quotes, setQuotes] = useState<SupplierQuoteRecord[]>([]);
  const [suppliers, setSuppliers] = useState<SupplierRecord[]>([]);
  const [materials, setMaterials] = useState<MaterialRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);
  const [editingQuote, setEditingQuote] = useState<SupplierQuoteRecord | null>(null);
  const { message } = App.useApp();
  const canCreate = hasPermission(MATERIAL_PERMISSIONS.QUOTE_CREATE);
  const canUpdate = hasPermission(MATERIAL_PERMISSIONS.QUOTE_UPDATE);

  const supplierOptions = useMemo(
    () => suppliers.map((item) => ({ label: item.name, value: item.id })),
    [suppliers]
  );
  const materialOptions = useMemo(
    () => materials.map((item) => ({ label: item.name, value: item.id })),
    [materials]
  );

  useEffect(() => {
    void fetchSuppliers({ pageNum: 1, pageSize: 100 }).then((data) => setSuppliers(data.records)).catch(() => undefined);
    void fetchMaterials({ pageNum: 1, pageSize: 100 }).then((data) => setMaterials(data.records)).catch(() => undefined);
    void loadQuotes();
  }, []);

  async function loadQuotes(nextPageNum = pageNum, nextPageSize = pageSize) {
    setLoading(true);
    try {
      const data = await fetchSupplierQuotes({ pageNum: nextPageNum, pageSize: nextPageSize });
      setQuotes(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载供应商报价失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(values: SupplierQuotePayload) {
    try {
      await createSupplierQuote(values);
      message.success("供应商报价创建成功");
      setCreateOpen(false);
      await loadQuotes(1, pageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "供应商报价创建失败");
      return false;
    }
  }

  async function handleUpdate(values: SupplierQuotePayload) {
    if (!editingQuote?.id) {
      return false;
    }
    try {
      await updateSupplierQuote(editingQuote.id, values);
      message.success("供应商报价更新成功");
      setEditingQuote(null);
      await loadQuotes();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "供应商报价更新失败");
      return false;
    }
  }

  const columns: ColumnsType<SupplierQuoteRecord> = [
    { title: "供应商", dataIndex: "supplierName", key: "supplierName", width: 180 },
    { title: "原料", dataIndex: "materialName", key: "materialName", width: 180 },
    { title: "报价", dataIndex: "quotePrice", key: "quotePrice", width: 120 },
    { title: "币种", dataIndex: "currency", key: "currency", width: 90 },
    { title: "最小起订量", dataIndex: "minOrderQuantity", key: "minOrderQuantity", width: 120 },
    { title: "交期(天)", dataIndex: "leadTimeDays", key: "leadTimeDays", width: 100 },
    { title: "生效日期", dataIndex: "effectiveDate", key: "effectiveDate", width: 120 },
    { title: "失效日期", dataIndex: "expiryDate", key: "expiryDate", width: 120 },
    {
      title: "操作",
      key: "actions",
      width: 120,
      render: (_, record) => (
        <Button type="link" disabled={!canUpdate} onClick={() => setEditingQuote(record)}>
          编辑
        </Button>
      )
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>原料管理 / 供应商报价</Title>
          <Text type="secondary">维护供应商对原料的基础报价、交期和最小起订量，为后续采购建议和比价提供数据基础。</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateOpen(true)}>
          新建报价
        </Button>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={quotes}
        loading={loading}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadQuotes(nextPageNum, nextPageSize)
        }}
      />

      <QuoteForm
        title="新建报价"
        open={createOpen}
        supplierOptions={supplierOptions}
        materialOptions={materialOptions}
        onCancel={() => setCreateOpen(false)}
        onFinish={handleCreate}
      />
      <QuoteForm
        title="编辑报价"
        open={!!editingQuote}
        supplierOptions={supplierOptions}
        materialOptions={materialOptions}
        initialValues={
          editingQuote
            ? {
                supplierId: editingQuote.supplierId,
                materialId: editingQuote.materialId,
                quotePrice: editingQuote.quotePrice,
                currency: editingQuote.currency,
                minOrderQuantity: editingQuote.minOrderQuantity ?? undefined,
                leadTimeDays: editingQuote.leadTimeDays ?? undefined,
                remark: editingQuote.remark ?? "",
                effectiveDate: editingQuote.effectiveDate ?? undefined,
                expiryDate: editingQuote.expiryDate ?? undefined
              }
            : undefined
        }
        onCancel={() => setEditingQuote(null)}
        onFinish={handleUpdate}
      />
    </section>
  );
}

function QuoteForm({
  title,
  open,
  initialValues,
  supplierOptions,
  materialOptions,
  onCancel,
  onFinish
}: {
  title: string;
  open: boolean;
  initialValues?: Partial<SupplierQuotePayload>;
  supplierOptions: Array<{ label: string; value: string }>;
  materialOptions: Array<{ label: string; value: string }>;
  onCancel: () => void;
  onFinish: (values: SupplierQuotePayload) => Promise<boolean>;
}) {
  return (
    <ModalForm<SupplierQuotePayload>
      title={title}
      open={open}
      width={980}
      grid
      rowProps={{ gutter: 16 }}
      initialValues={initialValues ?? { currency: "CNY" }}
      modalProps={{ destroyOnClose: true, onCancel }}
      onFinish={onFinish}
    >
      <ProFormSelect name="supplierId" label="供应商" options={supplierOptions} rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormSelect name="materialId" label="原料" options={materialOptions} rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormDigit name="quotePrice" label="报价" min={0.01} fieldProps={{ precision: 2 }} rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="currency" label="币种" rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormDigit name="minOrderQuantity" label="最小起订量" min={0} fieldProps={{ precision: 2 }} colProps={{ xs: 24, md: 8 }} />
      <ProFormDigit name="leadTimeDays" label="交期(天)" min={0} fieldProps={{ precision: 0 }} colProps={{ xs: 24, md: 8 }} />
      <ProFormDatePicker name="effectiveDate" label="生效日期" colProps={{ xs: 24, md: 8 }} />
      <ProFormDatePicker name="expiryDate" label="失效日期" colProps={{ xs: 24, md: 8 }} />
      <ProFormTextArea name="remark" label="备注" fieldProps={{ autoSize: { minRows: 2, maxRows: 4 } }} colProps={{ span: 24 }} />
    </ModalForm>
  );
}
