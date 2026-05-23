import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormSelect, ProFormText } from "@ant-design/pro-components";
import { App, Button, Switch, Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { createRole, fetchRoles, updateRole, updateRoleStatus } from "../../../api/system";
import type { RolePayload, RoleRecord } from "../../../types/system";

const { Title, Text } = Typography;

const dataScopeOptions = [
  { label: "全部数据", value: 1 },
  { label: "部门数据", value: 2 },
  { label: "本人数据", value: 3 }
];

export function RoleManagementPage() {
  const [loading, setLoading] = useState(false);
  const [roles, setRoles] = useState<RoleRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);
  const [editingRole, setEditingRole] = useState<RoleRecord | null>(null);
  const { message } = App.useApp();

  async function loadRoles(nextPageNum = pageNum, nextPageSize = pageSize) {
    setLoading(true);
    try {
      const data = await fetchRoles({ pageNum: nextPageNum, pageSize: nextPageSize });
      setRoles(data.records);
      setTotal(data.total);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载角色失败");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadRoles();
  }, []);

  async function handleCreate(values: RolePayload) {
    try {
      await createRole(values);
      message.success("角色创建成功");
      setCreateOpen(false);
      await loadRoles(1, pageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "角色创建失败");
      return false;
    }
  }

  async function handleUpdate(values: RolePayload) {
    if (!editingRole) return false;
    try {
      await updateRole(editingRole.id, values);
      message.success("角色更新成功");
      setEditingRole(null);
      await loadRoles();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "角色更新失败");
      return false;
    }
  }

  async function handleToggleStatus(role: RoleRecord, checked: boolean) {
    try {
      await updateRoleStatus(role.id, checked ? 1 : 0);
      message.success(checked ? "角色已启用" : "角色已禁用");
      await loadRoles();
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "状态更新失败");
    }
  }

  const columns: ColumnsType<RoleRecord> = [
    { title: "角色名称", dataIndex: "name", key: "name" },
    { title: "编码", dataIndex: "code", key: "code" },
    { title: "描述", dataIndex: "description", key: "description" },
    {
      title: "数据范围",
      dataIndex: "dataScope",
      key: "dataScope",
      render: (value: number) => dataScopeOptions.find((option) => option.value === value)?.label ?? value
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      render: (_, record) => <Switch checked={record.status === 1} checkedChildren="启用" unCheckedChildren="禁用" onChange={(checked) => void handleToggleStatus(record, checked)} />
    },
    {
      title: "操作",
      key: "actions",
      render: (_, record) => <Button type="link" onClick={() => setEditingRole(record)}>编辑</Button>
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>系统管理 / 角色管理</Title>
          <Text type="secondary">维护 RBAC 角色和数据权限范围。</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>新建角色</Button>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={roles}
        loading={loading}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadRoles(nextPageNum, nextPageSize)
        }}
      />

      <RoleForm title="新建角色" open={createOpen} initialValues={{ dataScope: 1 }} onCancel={() => setCreateOpen(false)} onFinish={handleCreate} />
      <RoleForm
        title="编辑角色"
        open={!!editingRole}
        initialValues={
          editingRole
            ? {
                name: editingRole.name,
                code: editingRole.code,
                description: editingRole.description ?? "",
                dataScope: editingRole.dataScope
              }
            : undefined
        }
        onCancel={() => setEditingRole(null)}
        onFinish={handleUpdate}
      />
    </section>
  );
}

function RoleForm({ title, open, initialValues, onCancel, onFinish }: {
  title: string;
  open: boolean;
  initialValues?: Partial<RolePayload>;
  onCancel: () => void;
  onFinish: (values: RolePayload) => Promise<boolean>;
}) {
  return (
    <ModalForm<RolePayload> title={title} open={open} initialValues={initialValues} modalProps={{ destroyOnClose: true, onCancel }} onFinish={onFinish}>
      <ProFormText name="name" label="角色名称" rules={[{ required: true }]} />
      <ProFormText name="code" label="角色编码" rules={[{ required: true }]} />
      <ProFormText name="description" label="描述" />
      <ProFormSelect name="dataScope" label="数据范围" options={dataScopeOptions} rules={[{ required: true }]} />
    </ModalForm>
  );
}
