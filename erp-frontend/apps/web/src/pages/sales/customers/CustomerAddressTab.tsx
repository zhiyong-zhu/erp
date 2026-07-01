import { PlusOutlined } from "@ant-design/icons";
import { App, Button, Input, Popconfirm, Space, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import {
  createCustomerAddress,
  deleteCustomerAddress,
  fetchCustomerAddresses,
  setDefaultCustomerAddress,
  updateCustomerAddress
} from "../../../api/sales";
import type { CustomerAddressPayload, CustomerAddressRecord } from "../../../types/sales";

const { Title, Text } = Typography;

export function CustomerAddressTab({ customerId, canUpdate }: { customerId: string; canUpdate: boolean }) {
  const [loading, setLoading] = useState(false);
  const [addresses, setAddresses] = useState<CustomerAddressRecord[]>([]);
  const [editing, setEditing] = useState<CustomerAddressRecord | null>(null);
  const [formOpen, setFormOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  // form fields
  const [recipient, setRecipient] = useState("");
  const [phone, setPhone] = useState("");
  const [address, setAddress] = useState("");
  const [isDefault, setIsDefault] = useState(false);
  const [remark, setRemark] = useState("");
  const { message } = App.useApp();

  useEffect(() => {
    void loadAddresses();
  }, [customerId]);

  async function loadAddresses() {
    setLoading(true);
    try {
      setAddresses(await fetchCustomerAddresses(customerId));
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载地址失败");
    } finally {
      setLoading(false);
    }
  }

  function openCreate() {
    setEditing(null);
    setRecipient("");
    setPhone("");
    setAddress("");
    setIsDefault(false);
    setRemark("");
    setFormOpen(true);
  }

  function openEdit(record: CustomerAddressRecord) {
    setEditing(record);
    setRecipient(record.recipient ?? "");
    setPhone(record.phone ?? "");
    setAddress(record.address);
    setIsDefault(Boolean(record.isDefault));
    setRemark(record.remark ?? "");
    setFormOpen(true);
  }

  async function submit() {
    if (!address.trim()) {
      message.warning("请输入详细地址");
      return;
    }
    const payload: CustomerAddressPayload = { recipient, phone, address, isDefault, remark };
    setSubmitting(true);
    try {
      if (editing) {
        await updateCustomerAddress(customerId, editing.id, payload);
        message.success("地址已更新");
      } else {
        await createCustomerAddress(customerId, payload);
        message.success("地址已添加");
      }
      setFormOpen(false);
      await loadAddresses();
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "保存失败");
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(id: string) {
    try {
      await deleteCustomerAddress(customerId, id);
      message.success("地址已删除");
      await loadAddresses();
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "删除失败");
    }
  }

  async function handleSetDefault(id: string) {
    try {
      await setDefaultCustomerAddress(customerId, id);
      message.success("已设为默认");
      await loadAddresses();
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "设置失败");
    }
  }

  const columns: ColumnsType<CustomerAddressRecord> = [
    {
      title: "默认", dataIndex: "isDefault", key: "isDefault", width: 70,
      render: (v: boolean) => (v ? <Tag color="green">默认</Tag> : "-")
    },
    { title: "收件人", dataIndex: "recipient", key: "recipient", width: 120, render: (v) => v ?? "-" },
    { title: "电话", dataIndex: "phone", key: "phone", width: 140, render: (v) => v ?? "-" },
    { title: "详细地址", dataIndex: "address", key: "address" },
    { title: "备注", dataIndex: "remark", key: "remark", width: 160, render: (v) => v ?? "-" },
    {
      title: "操作", key: "actions", width: 220,
      render: (_, record) => (
        <Space size="small">
          {!record.isDefault && (
            <Button type="link" size="small" disabled={!canUpdate} onClick={() => handleSetDefault(record.id)}>设默认</Button>
          )}
          <Button type="link" size="small" disabled={!canUpdate} onClick={() => openEdit(record)}>编辑</Button>
          <Popconfirm title="确定删除该地址？" onConfirm={() => handleDelete(record.id)}>
            <Button type="link" size="small" danger disabled={!canUpdate}>删除</Button>
          </Popconfirm>
        </Space>
      )
    }
  ];

  return (
    <div>
      <div style={{ marginBottom: 12, display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <Text type="secondary">管理客户收货地址，销售订单创建时自动带出默认地址。</Text>
        <Button type="primary" size="small" icon={<PlusOutlined />} disabled={!canUpdate} onClick={openCreate}>添加地址</Button>
      </div>
      <Table
        rowKey="id"
        size="small"
        loading={loading}
        columns={columns}
        dataSource={addresses}
        pagination={false}
      />
      {formOpen && (
        <div style={{ marginTop: 16, padding: 16, border: "1px solid #f0f0f0", borderRadius: 8, background: "#fafafa" }}>
          <Title level={5} style={{ marginTop: 0 }}>{editing ? "编辑地址" : "添加地址"}</Title>
          <Space direction="vertical" size={12} style={{ width: "100%" }}>
            <Space wrap>
              <Input placeholder="收件人" value={recipient} onChange={(e) => setRecipient(e.target.value)} style={{ width: 200 }} />
              <Input placeholder="电话" value={phone} onChange={(e) => setPhone(e.target.value)} style={{ width: 200 }} />
            </Space>
            <Input.TextArea placeholder="详细地址" value={address} onChange={(e) => setAddress(e.target.value)} autoSize={{ minRows: 2 }} />
            <Input placeholder="备注" value={remark} onChange={(e) => setRemark(e.target.value)} />
            <Space>
              <label style={{ cursor: "pointer" }}>
                <input type="checkbox" checked={isDefault} onChange={(e) => setIsDefault(e.target.checked)} style={{ marginRight: 6 }} />
                设为默认地址
              </label>
            </Space>
            <Space>
              <Button type="primary" loading={submitting} onClick={() => void submit()}>保存</Button>
              <Button onClick={() => setFormOpen(false)}>取消</Button>
            </Space>
          </Space>
        </div>
      )}
    </div>
  );
}
