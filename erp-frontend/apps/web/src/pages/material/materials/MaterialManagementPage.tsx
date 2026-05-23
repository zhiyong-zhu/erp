import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormSelect, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { App, Button, Input, Space, Table, Typography } from "antd";
import { MATERIAL_PERMISSIONS } from "@erp/shared";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { fetchMaterialCategoryTree, fetchMaterials, fetchSuppliers, createMaterial, updateMaterial } from "../../../api/material";
import { hasPermission } from "../../../store/auth";
import type { MaterialCategoryRecord, MaterialPayload, MaterialRecord, SupplierRecord } from "../../../types/material";

const { Title, Text } = Typography;

function flattenCategories(categories: MaterialCategoryRecord[]): MaterialCategoryRecord[] {
  return categories.flatMap((category) => [category, ...flattenCategories(category.children ?? [])]);
}

export function MaterialManagementPage() {
  const [loading, setLoading] = useState(false);
  const [materials, setMaterials] = useState<MaterialRecord[]>([]);
  const [categories, setCategories] = useState<MaterialCategoryRecord[]>([]);
  const [suppliers, setSuppliers] = useState<SupplierRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editingMaterial, setEditingMaterial] = useState<MaterialRecord | null>(null);
  const { message } = App.useApp();
  const canCreate = hasPermission(MATERIAL_PERMISSIONS.MATERIAL_CREATE);
  const canUpdate = hasPermission(MATERIAL_PERMISSIONS.MATERIAL_UPDATE);

  const categoryOptions = useMemo(
    () => flattenCategories(categories).map((category) => ({ label: category.name, value: category.id })),
    [categories]
  );
  const supplierOptions = useMemo(
    () => suppliers.map((supplier) => ({ label: supplier.name, value: supplier.id })),
    [suppliers]
  );

  useEffect(() => {
    void fetchMaterialCategoryTree().then(setCategories).catch(() => undefined);
    void fetchSuppliers({ pageNum: 1, pageSize: 100 }).then((data) => setSuppliers(data.records)).catch(() => undefined);
    void loadMaterials();
  }, []);

  async function loadMaterials(nextPageNum = pageNum, nextPageSize = pageSize, name = keyword) {
    setLoading(true);
    try {
      const data = await fetchMaterials({ pageNum: nextPageNum, pageSize: nextPageSize, name });
      setMaterials(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载原料失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(values: MaterialPayload) {
    try {
      await createMaterial(values);
      message.success("原料创建成功");
      setCreateOpen(false);
      await loadMaterials(1, pageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "原料创建失败");
      return false;
    }
  }

  async function handleUpdate(values: MaterialPayload) {
    if (!editingMaterial) {
      return false;
    }
    try {
      await updateMaterial(editingMaterial.id, values);
      message.success("原料更新成功");
      setEditingMaterial(null);
      await loadMaterials();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "原料更新失败");
      return false;
    }
  }

  const columns: ColumnsType<MaterialRecord> = [
    { title: "原料编码", dataIndex: "code", key: "code" },
    { title: "原料名称", dataIndex: "name", key: "name" },
    { title: "分类", dataIndex: "categoryName", key: "categoryName" },
    { title: "单位", dataIndex: "unit", key: "unit", width: 80 },
    { title: "默认供应商", dataIndex: "defaultSupplierName", key: "defaultSupplierName" },
    { title: "安全库存", dataIndex: "safetyStock", key: "safetyStock", width: 100 },
    { title: "采购周期(天)", dataIndex: "leadTimeDays", key: "leadTimeDays", width: 120 },
    { title: "操作", key: "actions", render: (_, record) => <Button type="link" disabled={!canUpdate} onClick={() => setEditingMaterial(record)}>编辑</Button> }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>原料管理 / 原料列表</Title>
          <Text type="secondary">维护原料基础资料、规格、安全库存和默认供应商，为采购和库存预警提供主数据。</Text>
        </div>
        <Space>
          <Input.Search
            allowClear
            placeholder="搜索原料名称"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            onSearch={(value) => {
              setKeyword(value);
              void loadMaterials(1, pageSize, value);
            }}
            style={{ width: 240 }}
          />
          <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateOpen(true)}>新建原料</Button>
        </Space>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={materials}
        loading={loading}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadMaterials(nextPageNum, nextPageSize)
        }}
      />

      <MaterialForm title="新建原料" open={createOpen} categoryOptions={categoryOptions} supplierOptions={supplierOptions} onCancel={() => setCreateOpen(false)} onFinish={handleCreate} />
      <MaterialForm
        title="编辑原料"
        open={!!editingMaterial}
        categoryOptions={categoryOptions}
        supplierOptions={supplierOptions}
        initialValues={editingMaterial ? {
          code: editingMaterial.code,
          name: editingMaterial.name,
          categoryId: editingMaterial.categoryId ?? undefined,
          unit: editingMaterial.unit,
          specifications: editingMaterial.specifications ?? "",
          defaultSupplierId: editingMaterial.defaultSupplierId ?? undefined,
          safetyStock: editingMaterial.safetyStock ?? undefined,
          leadTimeDays: editingMaterial.leadTimeDays ?? undefined,
          status: editingMaterial.status
        } : undefined}
        onCancel={() => setEditingMaterial(null)}
        onFinish={handleUpdate}
      />
    </section>
  );
}

function MaterialForm({ title, open, initialValues, categoryOptions, supplierOptions, onCancel, onFinish }: {
  title: string;
  open: boolean;
  initialValues?: Partial<MaterialPayload>;
  categoryOptions: Array<{ label: string; value: string }>;
  supplierOptions: Array<{ label: string; value: string }>;
  onCancel: () => void;
  onFinish: (values: MaterialPayload) => Promise<boolean>;
}) {
  return (
    <ModalForm<MaterialPayload> title={title} open={open} initialValues={initialValues ?? { status: 1 }} modalProps={{ destroyOnClose: true, onCancel }} onFinish={onFinish}>
      <ProFormText name="code" label="原料编码" rules={[{ required: true }]} />
      <ProFormText name="name" label="原料名称" rules={[{ required: true }]} />
      <ProFormSelect name="categoryId" label="原料分类" options={categoryOptions} />
      <ProFormText name="unit" label="单位" rules={[{ required: true }]} />
      <ProFormTextArea name="specifications" label="规格描述" fieldProps={{ autoSize: { minRows: 2, maxRows: 4 } }} />
      <ProFormSelect name="defaultSupplierId" label="默认供应商" options={supplierOptions} allowClear />
      <ProFormDigit name="safetyStock" label="安全库存" min={0} fieldProps={{ precision: 2 }} />
      <ProFormDigit name="leadTimeDays" label="采购周期(天)" min={0} fieldProps={{ precision: 0 }} />
      <ProFormSelect name="status" label="状态" options={[{ label: "启用", value: 1 }, { label: "禁用", value: 0 }]} />
    </ModalForm>
  );
}
