import { PlusOutlined } from "@ant-design/icons";
import { SALES_PERMISSIONS } from "@erp/shared";
import { CreateForm } from "../../../components/CreateForm";
import { App, Button, Card, Checkbox, Descriptions, Empty, Form, Input, Space, Table, Tabs, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import {
  createCustomer,
  fetchCustomers,
  updateCustomer
} from "../../../api/sales";
import { hasPermission } from "../../../store/auth";
import type { CustomerPayload, CustomerRecord } from "../../../types/sales";
import { CustomerAddressTab } from "./CustomerAddressTab";

const { Title, Text } = Typography;

const CUSTOMER_TYPE_MAP: Record<number, string> = { 1: "企业", 2: "个人" };
const GRADE_MAP: Record<string, { label: string; color: string }> = {
  A: { label: "核心客户", color: "gold" },
  B: { label: "潜力客户", color: "blue" },
  C: { label: "普通客户", color: "default" }
};
const GRADE_OPTIONS = [
  { label: "A - 核心客户", value: "A" },
  { label: "B - 潜力客户", value: "B" },
  { label: "C - 普通客户", value: "C" }
];
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
  const [viewingCustomer, setViewingCustomer] = useState<CustomerRecord | null>(null);
  const [detailTab, setDetailTab] = useState("base");
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
      title: "客户等级", dataIndex: "grade", key: "grade", width: 110,
      render: (v: string) => {
        const g = GRADE_MAP[v ?? ""];
        return g ? <Tag color={g.color}>{v} · {g.label}</Tag> : "-";
      }
    },
    {
      title: "状态", dataIndex: "status", key: "status", width: 80,
      render: (v: number) => { const s = STATUS_MAP[v]; return s ? <Tag color={s.color}>{s.label}</Tag> : "-"; }
    },
    {
      title: "操作", key: "actions", width: 140,
      render: (_, record) => (
        <Space size="small">
          <Button type="link" size="small" onClick={() => { setViewingCustomer(record); setDetailTab("base"); }}>详情</Button>
          <Button type="link" size="small" disabled={!canUpdate} onClick={() => setEditingCustomer(record)}>编辑</Button>
        </Space>
      )
    }
  ];

  return (
    <section style={{ height: "100%", display: "flex", flexDirection: "column", minHeight: 0 }}>
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

      <div className="product-page-split">
        <div className="product-page-panel" style={{ flex: viewingCustomer ? "1 1 0" : "1 1 100%" }}>
          <Card size="small" className="product-list-panel" styles={{ body: { padding: 0 } }}>
            <Table
              rowKey="id"
              columns={columns}
              dataSource={customers}
              loading={loading}
              size="small"
              onRow={(record) => ({
                onClick: () => { setViewingCustomer(record); setDetailTab("base"); },
                style: { cursor: "pointer" }
              })}
              pagination={{
                current: pageNum, pageSize, total,
                showSizeChanger: true,
                showTotal: (count) => `共 ${count} 条`,
                onChange: (nextPageNum, nextPageSize) => void loadCustomers(nextPageNum, nextPageSize)
              }}
              scroll={{ y: 320 }}
            />
          </Card>
        </div>

        {viewingCustomer ? (
          <div className="product-page-panel" style={{ flex: "1 1 0" }}>
            <Card size="small" className="product-list-panel" styles={{ body: { padding: 12 } }}>
              <div style={{ marginBottom: 8, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                <Text strong>{viewingCustomer.name}（{viewingCustomer.code}）</Text>
                <Button type="link" size="small" onClick={() => setViewingCustomer(null)}>收起</Button>
              </div>
              <Tabs
                activeKey={detailTab}
                onChange={setDetailTab}
                items={[
                  {
                    key: "base",
                    label: "基础信息",
                    children: (
                      <Descriptions column={2} bordered size="small">
                        <Descriptions.Item label="客户编码">{viewingCustomer.code}</Descriptions.Item>
                        <Descriptions.Item label="客户名称">{viewingCustomer.name}</Descriptions.Item>
                        <Descriptions.Item label="简称">{viewingCustomer.shortName ?? "-"}</Descriptions.Item>
                        <Descriptions.Item label="类型">{CUSTOMER_TYPE_MAP[viewingCustomer.customerType ?? 0] ?? "-"}</Descriptions.Item>
                        <Descriptions.Item label="联系人">{viewingCustomer.contactPerson ?? "-"}</Descriptions.Item>
                        <Descriptions.Item label="电话">{viewingCustomer.phone ?? "-"}</Descriptions.Item>
                        <Descriptions.Item label="邮箱" span={2}>{viewingCustomer.email ?? "-"}</Descriptions.Item>
                        <Descriptions.Item label="客户等级">
                          {(() => { const g = GRADE_MAP[viewingCustomer.grade ?? ""]; return g ? <Tag color={g.color}>{viewingCustomer.grade} · {g.label}</Tag> : "-"; })()}
                        </Descriptions.Item>
                        <Descriptions.Item label="状态">
                          {(() => { const s = STATUS_MAP[viewingCustomer.status]; return s ? <Tag color={s.color}>{s.label}</Tag> : "-"; })()}
                        </Descriptions.Item>
                        <Descriptions.Item label="地址" span={2}>{viewingCustomer.address ?? "-"}</Descriptions.Item>
                        <Descriptions.Item label="备注" span={2}>{viewingCustomer.remark ?? "-"}</Descriptions.Item>
                      </Descriptions>
                    )
                  },
                  {
                    key: "address",
                    label: "收货地址",
                    children: <CustomerAddressTab customerId={viewingCustomer.id} canUpdate={canUpdate} />
                  }
                ]}
              />
            </Card>
          </div>
        ) : null}
      </div>

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
                grade: editingCustomer.grade ?? "C",
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

/**
 * 客户编码输入框：自动生成复选框内嵌在输入框内部（suffix），
 * 与输入框一体化。勾选「自动」时禁用输入并清空值，提交时交后端生成。
 */
function CodeInputWithAuto({ autoGenerate }: { autoGenerate: boolean }) {
  const form = Form.useFormInstance();
  const auto = autoGenerate;
  return (
    <Form.Item
      label="客户编码"
      name="code"
      rules={auto ? [] : [{ required: true, message: "请输入客户编码" }]}
    >
      <Input
        allowClear
        disabled={auto}
        placeholder={auto ? "保存时由系统自动生成" : "请输入客户编码"}
        suffix={
          <Checkbox
            checked={auto}
            onChange={(e) => {
              form.setFieldValue("autoGenerateCode", e.target.checked);
              if (e.target.checked) {
                // 切回自动生成时清空已输入的编码
                form.setFieldValue("code", undefined);
              }
            }}
          >
            自动
          </Checkbox>
        }
      />
    </Form.Item>
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
      initialValues={initialValues ?? { autoGenerateCode: true, customerType: 1, grade: "C", status: 1 }}
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
            {
              type: "dep",
              watch: ["autoGenerateCode"],
              colSpan: 8,
              render: (values) => (
                <CodeInputWithAuto autoGenerate={values.autoGenerateCode ?? !isEdit} />
              )
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
            { type: "select", name: "grade", label: "客户等级", options: GRADE_OPTIONS, colSpan: 8 },
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
