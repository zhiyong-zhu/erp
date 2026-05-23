import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormSelect, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { App, Button, Input, Space, Table, Typography } from "antd";
import { MATERIAL_PERMISSIONS } from "@erp/shared";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { createSupplier, fetchSuppliers, updateSupplier } from "../../../api/material";
import { hasPermission } from "../../../store/auth";
import type { SupplierPayload, SupplierRecord } from "../../../types/material";

const { Title, Text } = Typography;

export function SupplierManagementPage() {
  const [loading, setLoading] = useState(false);
  const [suppliers, setSuppliers] = useState<SupplierRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editingSupplier, setEditingSupplier] = useState<SupplierRecord | null>(null);
  const { message } = App.useApp();
  const canCreate = hasPermission(MATERIAL_PERMISSIONS.SUPPLIER_CREATE);
  const canUpdate = hasPermission(MATERIAL_PERMISSIONS.SUPPLIER_UPDATE);

  useEffect(() => {
    void loadSuppliers();
  }, []);

  async function loadSuppliers(nextPageNum = pageNum, nextPageSize = pageSize, name = keyword) {
    setLoading(true);
    try {
      const data = await fetchSuppliers({ pageNum: nextPageNum, pageSize: nextPageSize, name });
      setSuppliers(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载供应商失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(values: SupplierPayload) {
    try {
      await createSupplier(values);
      message.success("供应商创建成功");
      setCreateOpen(false);
      await loadSuppliers(1, pageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "供应商创建失败");
      return false;
    }
  }

  async function handleUpdate(values: SupplierPayload) {
    if (!editingSupplier) {
      return false;
    }
    try {
      await updateSupplier(editingSupplier.id, values);
      message.success("供应商更新成功");
      setEditingSupplier(null);
      await loadSuppliers();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "供应商更新失败");
      return false;
    }
  }

  const columns: ColumnsType<SupplierRecord> = [
    { title: "供应商编码", dataIndex: "code", key: "code" },
    { title: "供应商名称", dataIndex: "name", key: "name" },
    { title: "联系人", dataIndex: "contactPerson", key: "contactPerson" },
    { title: "电话", dataIndex: "phone", key: "phone" },
    { title: "邮箱", dataIndex: "email", key: "email" },
    { title: "信用评级", dataIndex: "creditRating", key: "creditRating", width: 100 },
    { title: "操作", key: "actions", render: (_, record) => <Button type="link" disabled={!canUpdate} onClick={() => setEditingSupplier(record)}>编辑</Button> }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>原料管理 / 供应商管理</Title>
          <Text type="secondary">维护供应商档案、联系人和基础资质信息，为默认供应商、采购与补货预警提供基础数据。</Text>
        </div>
        <Space>
          <Input.Search
            allowClear
            placeholder="搜索供应商名称"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            onSearch={(value) => {
              setKeyword(value);
              void loadSuppliers(1, pageSize, value);
            }}
            style={{ width: 240 }}
          />
          <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateOpen(true)}>新建供应商</Button>
        </Space>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={suppliers}
        loading={loading}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadSuppliers(nextPageNum, nextPageSize)
        }}
      />

      <SupplierForm title="新建供应商" open={createOpen} onCancel={() => setCreateOpen(false)} onFinish={handleCreate} />
      <SupplierForm
        title="编辑供应商"
        open={!!editingSupplier}
        initialValues={editingSupplier ? {
          code: editingSupplier.code,
          name: editingSupplier.name,
          shortName: editingSupplier.shortName ?? "",
          contactPerson: editingSupplier.contactPerson ?? "",
          phone: editingSupplier.phone ?? "",
          email: editingSupplier.email ?? "",
          address: editingSupplier.address ?? "",
          bankName: editingSupplier.bankName ?? "",
          bankAccount: editingSupplier.bankAccount ?? "",
          taxNumber: editingSupplier.taxNumber ?? "",
          creditRating: editingSupplier.creditRating ?? undefined,
          status: editingSupplier.status
        } : undefined}
        onCancel={() => setEditingSupplier(null)}
        onFinish={handleUpdate}
      />
    </section>
  );
}

function SupplierForm({ title, open, initialValues, onCancel, onFinish }: {
  title: string;
  open: boolean;
  initialValues?: Partial<SupplierPayload>;
  onCancel: () => void;
  onFinish: (values: SupplierPayload) => Promise<boolean>;
}) {
  return (
    <ModalForm<SupplierPayload> title={title} open={open} initialValues={initialValues ?? { status: 1 }} modalProps={{ destroyOnClose: true, onCancel }} onFinish={onFinish}>
      <ProFormText name="code" label="供应商编码" rules={[{ required: true }]} />
      <ProFormText name="name" label="供应商名称" rules={[{ required: true }]} />
      <ProFormText name="shortName" label="简称" />
      <ProFormText name="contactPerson" label="联系人" />
      <ProFormText name="phone" label="电话" />
      <ProFormText name="email" label="邮箱" />
      <ProFormTextArea name="address" label="地址" fieldProps={{ autoSize: { minRows: 2, maxRows: 4 } }} />
      <ProFormText name="bankName" label="开户行" />
      <ProFormText name="bankAccount" label="银行账号" />
      <ProFormText name="taxNumber" label="税号" />
      <ProFormDigit name="creditRating" label="信用评级" min={1} max={5} fieldProps={{ precision: 0 }} />
      <ProFormSelect name="status" label="状态" options={[{ label: "启用", value: 1 }, { label: "禁用", value: 0 }]} />
    </ModalForm>
  );
}
