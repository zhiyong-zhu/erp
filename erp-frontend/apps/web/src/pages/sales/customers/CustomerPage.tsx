import { PlusOutlined } from "@ant-design/icons";
import { ProFormText } from "@ant-design/pro-components";
import { SALES_PERMISSIONS } from "@erp/shared";
import { CreateForm } from "../../../components/CreateForm";
import { App, Button, Input, Space, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import {
  createCustomer,
  fetchCustomers,
  updateCustomer
} from "../../../api/sales";
import { hasPermission } from "../../../store/auth";
import type { CustomerPayload, CustomerRecord } from "../../../types/sales";

const { Title, Text } = Typography;

const CUSTOMER_TYPE_MAP: Record<number, string> = { 1: "企业", 2: "个人" };
const STATUS_MAP: Record<number, { label: string; color: string }> = {
  1: { label: "启用", color: "green" },
  0: { label: "禁用", color: "red" }
};

export function CustomerPage() {
  const [loading, setLoading] = useState(false);
  const [customers, setCustomers] = useState<CustomerRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editingCustomer, setEditingCustomer] = useState<CustomerRecord | null>(null);
  const { message } = App.useApp();
  const canCreate = hasPermission(SALES_PERMISSIONS.CUSTOMER_CREATE);
  const canUpdate = hasPermission(SALES_PERMISSIONS.CUSTOMER_UPDATE);

  useEffect(() => {
    void loadCustomers();
  }, []);

  async function loadCustomers(nextPageNum = pageNum, nextPageSize = pageSize, name = keyword) {
    setLoading(true);
    try {
      const data = await fetchCustomers({ pageNum: nextPageNum, pageSize: nextPageSize, name });
      setCustomers(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载客户失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(values: CustomerPayload) {
    try {
      await createCustomer(values);
      message.success("客户创建成功");
      setCreateOpen(false);
      await loadCustomers(1, pageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "客户创建失败");
      return false;
    }
  }

  async function handleUpdate(values: CustomerPayload) {
    if (!editingCustomer?.id) return false;
    try {
      await updateCustomer(editingCustomer.id, values);
      message.success("客户更新成功");
      setEditingCustomer(null);
      await loadCustomers();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "客户更新失败");
      return false;
    }
  }

  const columns: ColumnsType<CustomerRecord> = [
    { title: "客户编码", dataIndex: "code", key: "code", width: 140 },
    { title: "客户名称", dataIndex: "name", key: "name" },
    {
      title: "类型", dataIndex: "customerType", key: "customerType", width: 80,
      render: (v: number) => CUSTOMER_TYPE_MAP[v] ?? "-"
    },
    { title: "联系人", dataIndex: "contactPerson", key: "contactPerson", width: 100 },
    { title: "电话", dataIndex: "phone", key: "phone", width: 140 },
    { title: "邮箱", dataIndex: "email", key: "email", width: 180 },
    {
      title: "信用额度", dataIndex: "creditLimit", key: "creditLimit", width: 120,
      render: (v: number) => v != null ? `¥${v.toLocaleString()}` : "-"
    },
    {
      title: "状态", dataIndex: "status", key: "status", width: 80,
      render: (v: number) => { const s = STATUS_MAP[v]; return s ? <Tag color={s.color}>{s.label}</Tag> : "-"; }
    },
    {
      title: "操作", key: "actions", width: 80,
      render: (_, record) => (
        <Button type="link" disabled={!canUpdate} onClick={() => setEditingCustomer(record)}>
          编辑
        </Button>
      )
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>销售管理 / 客户管理</Title>
          <Text type="secondary">维护客户档案、联系人和信用额度信息，为销售订单和应收统计提供数据支持。</Text>
        </div>
        <Space>
          <Input.Search
            allowClear
            placeholder="搜索客户名称"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onSearch={(value) => { setKeyword(value); void loadCustomers(1, pageSize, value); }}
            style={{ width: 240 }}
          />
          <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateOpen(true)}>
            新建客户
          </Button>
        </Space>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={customers}
        loading={loading}
        pagination={{
          current: pageNum, pageSize, total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadCustomers(nextPageNum, nextPageSize)
        }}
      />

      <CustomerForm
        title="新建客户"
        open={createOpen}
        onCancel={() => setCreateOpen(false)}
        onFinish={handleCreate}
      />
      <CustomerForm
        title="编辑客户"
        open={!!editingCustomer}
        initialValues={
          editingCustomer
            ? {
                autoGenerateCode: false,
                code: editingCustomer.code,
                name: editingCustomer.name,
                shortName: editingCustomer.shortName ?? "",
                customerType: editingCustomer.customerType ?? 1,
                contactPerson: editingCustomer.contactPerson ?? "",
                phone: editingCustomer.phone ?? "",
                email: editingCustomer.email ?? "",
                address: editingCustomer.address ?? "",
                creditLimit: editingCustomer.creditLimit ?? undefined,
                paymentTerms: editingCustomer.paymentTerms ?? undefined,
                taxNumber: editingCustomer.taxNumber ?? "",
                status: editingCustomer.status,
                remark: editingCustomer.remark ?? ""
              }
            : undefined
        }
        onCancel={() => setEditingCustomer(null)}
        onFinish={handleUpdate}
      />
    </section>
  );
}

function CustomerForm({
  title, open, initialValues, onCancel, onFinish
}: {
  title: string;
  open: boolean;
  initialValues?: Partial<CustomerPayload> & { autoGenerateCode?: boolean };
  onCancel: () => void;
  onFinish: (values: CustomerPayload & { autoGenerateCode?: boolean }) => Promise<boolean>;
}) {
  // 编辑态默认不自动生成（已有编码），新建态默认自动生成
  const isEdit = !!initialValues?.code;
  return (
    <CreateForm
      title={title}
      open={open}
      width={980}
      initialValues={initialValues ?? { autoGenerateCode: true, customerType: 1, status: 1 }}
      onCancel={onCancel}
      onFinish={async (values) => {
        const { autoGenerateCode, ...rest } = values;
        // 自动生成时清空编码，交由后端生成
        const payload: CustomerPayload = { ...rest };
        if (autoGenerateCode) {
          delete payload.code;
        }
        return onFinish(payload);
      }}
      sections={[
        {
          title: "基本信息",
          fields: [
            { type: "switch", name: "autoGenerateCode", label: "自动生成编码", defaultChecked: !isEdit, colSpan: 8 },
            {
              type: "dep",
              watch: ["autoGenerateCode"],
              colSpan: 8,
              render: (values) => {
                const auto = values.autoGenerateCode ?? !isEdit;
                return (
                  <ProFormText
                    name="code"
                    label="客户编码"
                    placeholder={auto ? "保存时由系统自动生成" : "请输入客户编码"}
                    disabled={auto}
                    rules={auto ? [] : [{ required: true, message: "请输入客户编码" }]}
                  />
                );
              }
            },
            { type: "text", name: "name", label: "客户名称", rules: [{ required: true, message: "请输入客户名称" }], colSpan: 8 },
            { type: "text", name: "shortName", label: "简称", colSpan: 8 },
            { type: "select", name: "customerType", label: "客户类型", options: [{ label: "企业", value: 1 }, { label: "个人", value: 2 }], colSpan: 8 },
            { type: "text", name: "contactPerson", label: "联系人", colSpan: 8 },
            { type: "text", name: "phone", label: "电话", colSpan: 8 },
            { type: "text", name: "email", label: "邮箱", colSpan: 8 }
          ]
        },
        {
          title: "商务信息",
          fields: [
            { type: "digit", name: "creditLimit", label: "信用额度", min: 0, precision: 2, colSpan: 8 },
            { type: "select", name: "paymentTerms", label: "付款条件", options: [{ label: "货到付款", value: 1 }, { label: "30天", value: 2 }, { label: "60天", value: 3 }, { label: "90天", value: 4 }], colSpan: 8 },
            { type: "text", name: "taxNumber", label: "税号", colSpan: 8 },
            { type: "select", name: "status", label: "状态", options: [{ label: "启用", value: 1 }, { label: "禁用", value: 0 }], colSpan: 8 },
            { type: "textarea", name: "address", label: "地址", colSpan: 24 },
            { type: "textarea", name: "remark", label: "备注", colSpan: 24 }
          ]
        }
      ]}
    />
  );
}
