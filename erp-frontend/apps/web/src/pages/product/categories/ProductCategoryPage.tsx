import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormSelect, ProFormText } from "@ant-design/pro-components";
import { App, Button, Switch, Table, Typography } from "antd";
import { PRODUCT_PERMISSIONS } from "@erp/shared";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { createProductCategory, fetchProductCategoryTree, updateProductCategory } from "../../../api/product";
import { hasPermission } from "../../../store/auth";
import type { ProductCategoryPayload, ProductCategoryRecord } from "../../../types/product";

const { Title, Text } = Typography;

function flattenCategories(categories: ProductCategoryRecord[]): ProductCategoryRecord[] {
  return categories.flatMap((category) => [category, ...flattenCategories(category.children ?? [])]);
}

export function ProductCategoryPage() {
  const [loading, setLoading] = useState(false);
  const [categories, setCategories] = useState<ProductCategoryRecord[]>([]);
  const [createOpen, setCreateOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<ProductCategoryRecord | null>(null);
  const { message } = App.useApp();
  const canCreate = hasPermission(PRODUCT_PERMISSIONS.CATEGORY_CREATE);
  const canUpdate = hasPermission(PRODUCT_PERMISSIONS.CATEGORY_UPDATE);

  const categoryOptions = useMemo(
    () => flattenCategories(categories).map((category) => ({ label: `${category.name}${category.code ? `（${category.code}）` : ""}`, value: category.id })),
    [categories]
  );

  useEffect(() => {
    void loadCategories();
  }, []);

  async function loadCategories() {
    setLoading(true);
    try {
      setCategories(await fetchProductCategoryTree());
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载产品分类失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(values: ProductCategoryPayload) {
    try {
      await createProductCategory(values);
      message.success("产品分类创建成功");
      setCreateOpen(false);
      await loadCategories();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "产品分类创建失败");
      return false;
    }
  }

  async function handleUpdate(values: ProductCategoryPayload) {
    if (!editingCategory) {
      return false;
    }
    try {
      await updateProductCategory(editingCategory.id, values);
      message.success("产品分类更新成功");
      setEditingCategory(null);
      await loadCategories();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "产品分类更新失败");
      return false;
    }
  }

  const columns: ColumnsType<ProductCategoryRecord> = [
    { title: "分类名称", dataIndex: "name", key: "name" },
    { title: "编码", dataIndex: "code", key: "code" },
    { title: "排序", dataIndex: "sortOrder", key: "sortOrder", width: 90 },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      render: (value: number) => <Switch checked={value === 1} disabled />
    },
    {
      title: "操作",
      key: "actions",
      render: (_, record) => <Button type="link" disabled={!canUpdate} onClick={() => setEditingCategory(record)}>编辑</Button>
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>产品管理 / 分类管理</Title>
          <Text type="secondary">维护多级产品分类树，供产品基础资料归类使用。</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateOpen(true)}>新建分类</Button>
      </div>

      <Table rowKey="id" columns={columns} dataSource={categories} loading={loading} pagination={false} />

      <CategoryForm title="新建分类" open={createOpen} categoryOptions={categoryOptions} onCancel={() => setCreateOpen(false)} onFinish={handleCreate} />
      <CategoryForm
        title="编辑分类"
        open={!!editingCategory}
        categoryOptions={categoryOptions.filter((option) => option.value !== editingCategory?.id)}
        initialValues={editingCategory ? {
          parentId: editingCategory.parentId ?? undefined,
          name: editingCategory.name,
          code: editingCategory.code ?? "",
          sortOrder: editingCategory.sortOrder,
          status: editingCategory.status
        } : undefined}
        onCancel={() => setEditingCategory(null)}
        onFinish={handleUpdate}
      />
    </section>
  );
}

function CategoryForm({ title, open, initialValues, categoryOptions, onCancel, onFinish }: {
  title: string;
  open: boolean;
  initialValues?: Partial<ProductCategoryPayload>;
  categoryOptions: Array<{ label: string; value: string }>;
  onCancel: () => void;
  onFinish: (values: ProductCategoryPayload) => Promise<boolean>;
}) {
  return (
    <ModalForm<ProductCategoryPayload> title={title} open={open} initialValues={initialValues ?? { status: 1, sortOrder: 0 }} modalProps={{ destroyOnClose: true, onCancel }} onFinish={onFinish}>
      <ProFormSelect name="parentId" label="上级分类" options={categoryOptions} allowClear />
      <ProFormText name="name" label="分类名称" rules={[{ required: true }]} />
      <ProFormText name="code" label="分类编码" />
      <ProFormDigit name="sortOrder" label="排序" min={0} fieldProps={{ precision: 0 }} />
      <ProFormSelect name="status" label="状态" options={[{ label: "启用", value: 1 }, { label: "禁用", value: 0 }]} />
    </ModalForm>
  );
}
