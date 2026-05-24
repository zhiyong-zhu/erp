import { PlusOutlined } from "@ant-design/icons";
import {
  ModalForm,
  ProFormDigit,
  ProFormSelect,
  ProFormText
} from "@ant-design/pro-components";
import { App, Button, Switch, Table, Typography } from "antd";
import { MATERIAL_PERMISSIONS } from "@erp/shared";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import {
  createMaterialCategory,
  fetchMaterialCategoryTree,
  updateMaterialCategory
} from "../../../api/material";
import { hasPermission } from "../../../store/auth";
import type {
  MaterialCategoryPayload,
  MaterialCategoryRecord
} from "../../../types/material";

const { Title, Text } = Typography;

function flattenCategories(categories: MaterialCategoryRecord[]): MaterialCategoryRecord[] {
  return categories.flatMap((category) => [
    category,
    ...flattenCategories(category.children ?? [])
  ]);
}

export function MaterialCategoryPage() {
  const [loading, setLoading] = useState(false);
  const [categories, setCategories] = useState<MaterialCategoryRecord[]>([]);
  const [createOpen, setCreateOpen] = useState(false);
  const [editingCategory, setEditingCategory] = useState<MaterialCategoryRecord | null>(null);
  const { message } = App.useApp();
  const canCreate = hasPermission(MATERIAL_PERMISSIONS.CATEGORY_CREATE);
  const canUpdate = hasPermission(MATERIAL_PERMISSIONS.CATEGORY_UPDATE);

  const categoryOptions = useMemo(
    () =>
      flattenCategories(categories).map((category) => ({
        label: category.code ? `${category.name} (${category.code})` : category.name,
        value: category.id as string
      })),
    [categories]
  );

  useEffect(() => {
    void loadCategories();
  }, []);

  async function loadCategories() {
    setLoading(true);
    try {
      setCategories(await fetchMaterialCategoryTree());
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载原料分类失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(values: MaterialCategoryPayload) {
    try {
      await createMaterialCategory(values);
      message.success("原料分类创建成功");
      setCreateOpen(false);
      await loadCategories();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "原料分类创建失败");
      return false;
    }
  }

  async function handleUpdate(values: MaterialCategoryPayload) {
    if (!editingCategory?.id) {
      return false;
    }
    try {
      await updateMaterialCategory(editingCategory.id, values);
      message.success("原料分类更新成功");
      setEditingCategory(null);
      await loadCategories();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "原料分类更新失败");
      return false;
    }
  }

  const columns: ColumnsType<MaterialCategoryRecord> = [
    { title: "分类名称", dataIndex: "name", key: "name" },
    { title: "编码", dataIndex: "code", key: "code", width: 180 },
    { title: "排序", dataIndex: "sortOrder", key: "sortOrder", width: 100 },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (value: number) => <Switch checked={value === 1} disabled />
    },
    {
      title: "操作",
      key: "actions",
      width: 120,
      render: (_, record) => (
        <Button type="link" disabled={!canUpdate} onClick={() => setEditingCategory(record)}>
          编辑
        </Button>
      )
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>原料管理 / 分类管理</Title>
          <Text type="secondary">维护多级原料分类树，为原料主数据提供基础结构。</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateOpen(true)}>
          新建分类
        </Button>
      </div>

      <Table rowKey="id" columns={columns} dataSource={categories} loading={loading} pagination={false} />

      <CategoryForm
        title="新建分类"
        open={createOpen}
        categoryOptions={categoryOptions}
        onCancel={() => setCreateOpen(false)}
        onFinish={handleCreate}
      />
      <CategoryForm
        title="编辑分类"
        open={!!editingCategory}
        categoryOptions={categoryOptions.filter((option) => option.value !== editingCategory?.id)}
        initialValues={
          editingCategory
            ? {
                parentId: editingCategory.parentId ?? undefined,
                name: editingCategory.name,
                code: editingCategory.code ?? "",
                sortOrder: editingCategory.sortOrder,
                status: editingCategory.status
              }
            : undefined
        }
        onCancel={() => setEditingCategory(null)}
        onFinish={handleUpdate}
      />
    </section>
  );
}

function CategoryForm({
  title,
  open,
  initialValues,
  categoryOptions,
  onCancel,
  onFinish
}: {
  title: string;
  open: boolean;
  initialValues?: Partial<MaterialCategoryPayload>;
  categoryOptions: Array<{ label: string; value: string }>;
  onCancel: () => void;
  onFinish: (values: MaterialCategoryPayload) => Promise<boolean>;
}) {
  return (
    <ModalForm<MaterialCategoryPayload>
      title={title}
      open={open}
      width={880}
      grid
      rowProps={{ gutter: 16 }}
      initialValues={initialValues ?? { status: 1, sortOrder: 0 }}
      modalProps={{ destroyOnClose: true, onCancel }}
      onFinish={onFinish}
    >
      <ProFormSelect
        name="parentId"
        label="上级分类"
        options={categoryOptions}
        allowClear
        colProps={{ xs: 24, md: 8 }}
      />
      <ProFormText
        name="name"
        label="分类名称"
        rules={[{ required: true, message: "请输入分类名称" }]}
        colProps={{ xs: 24, md: 8 }}
      />
      <ProFormText
        name="code"
        label="分类编码"
        colProps={{ xs: 24, md: 8 }}
      />
      <ProFormDigit
        name="sortOrder"
        label="排序"
        min={0}
        fieldProps={{ precision: 0 }}
        colProps={{ xs: 24, md: 8 }}
      />
      <ProFormSelect
        name="status"
        label="状态"
        options={[
          { label: "启用", value: 1 },
          { label: "禁用", value: 0 }
        ]}
        colProps={{ xs: 24, md: 8 }}
      />
    </ModalForm>
  );
}
