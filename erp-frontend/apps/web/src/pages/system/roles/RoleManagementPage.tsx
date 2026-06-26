import { PlusOutlined } from "@ant-design/icons";
import { App, Button, Card, Space, Switch, Table, Tree, Typography } from "antd";
import { SYSTEM_PERMISSIONS } from "@erp/shared";
import { CreateForm } from "../../../components/CreateForm";
import type { DataNode } from "antd/es/tree";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { createRole, fetchPermissionTree, fetchRoles, updateRole, updateRoleStatus } from "../../../api/system";
import { hasPermission } from "../../../store/auth";
import type { PermissionRecord, RolePayload, RoleRecord } from "../../../types/system";

const { Title, Text } = Typography;

const dataScopeOptions = [
  { label: "全部数据", value: 1 },
  { label: "部门数据", value: 2 },
  { label: "本人数据", value: 3 }
];

export function RoleManagementPage() {
  const [loading, setLoading] = useState(false);
  const [roles, setRoles] = useState<RoleRecord[]>([]);
  const [permissions, setPermissions] = useState<PermissionRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);
  const [editingRole, setEditingRole] = useState<RoleRecord | null>(null);
  const { message } = App.useApp();
  const canCreate = hasPermission(SYSTEM_PERMISSIONS.ROLE_CREATE);
  const canUpdate = hasPermission(SYSTEM_PERMISSIONS.ROLE_UPDATE);
  const treeData = useMemo<DataNode[]>(() => buildTreeNodes(permissions), [permissions]);

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
    void fetchPermissionTree().then(setPermissions).catch(() => undefined);
  }, []);

  async function handleCreate(values: RolePayload & { permissionIds?: string[] }) {
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

  async function handleUpdate(values: RolePayload & { permissionIds?: string[] }) {
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
      render: (_, record) => <Switch checked={record.status === 1} checkedChildren="启用" unCheckedChildren="禁用" disabled={!canUpdate} onChange={(checked) => void handleToggleStatus(record, checked)} />
    },
    {
      title: "操作",
      key: "actions",
      render: (_, record) => (
        <Space size="small">
          <Button type="link" disabled={!canUpdate} onClick={() => setEditingRole(record)}>编辑</Button>
        </Space>
      )
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>系统管理 / 角色管理</Title>
          <Text type="secondary">维护 RBAC 角色和数据权限范围。</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateOpen(true)}>新建角色</Button>
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

      <RoleForm
        title="新建角色"
        open={createOpen}
        treeData={treeData}
        initialValues={{ dataScope: 1, permissionIds: [] }}
        onCancel={() => setCreateOpen(false)}
        onFinish={handleCreate}
      />
      <RoleForm
        title="编辑角色"
        open={!!editingRole}
        treeData={treeData}
        initialValues={
          editingRole
            ? {
                name: editingRole.name,
                code: editingRole.code,
                description: editingRole.description ?? "",
                dataScope: editingRole.dataScope,
                permissionIds: editingRole.permissionIds ?? []
              }
            : undefined
        }
        onCancel={() => setEditingRole(null)}
        onFinish={handleUpdate}
      />
    </section>
  );
}

function RoleForm({ title, open, initialValues, treeData, onCancel, onFinish }: {
  title: string;
  open: boolean;
  initialValues?: Partial<RolePayload & { permissionIds?: string[] }>;
  treeData: DataNode[];
  onCancel: () => void;
  onFinish: (values: RolePayload & { permissionIds?: string[] }) => Promise<boolean>;
}) {
  const [checkedKeys, setCheckedKeys] = useState<string[]>([]);

  useEffect(() => {
    if (open) {
      setCheckedKeys((initialValues?.permissionIds as string[] | undefined) ?? []);
    } else {
      setCheckedKeys([]);
    }
  }, [initialValues?.permissionIds, open]);

  return (
    <CreateForm
      title={title}
      open={open}
      width={920}
      initialValues={initialValues}
      onCancel={onCancel}
      onFinish={(values) => onFinish({ ...values, permissionIds: checkedKeys })}
      sections={[
        {
          title: "角色信息",
          fields: [
            { type: "text", name: "name", label: "角色名称", rules: [{ required: true }], colSpan: 12 },
            { type: "text", name: "code", label: "角色编码", rules: [{ required: true }], colSpan: 12 },
            { type: "text", name: "description", label: "描述", colSpan: 12 },
            { type: "select", name: "dataScope", label: "数据范围", options: dataScopeOptions, rules: [{ required: true }], colSpan: 12 }
          ]
        },
        {
          title: "功能权限",
          slot: (
            <>
              <Text type="secondary">目录、菜单、按钮和字段权限统一在这里分配。数据范围在上方单独配置，不放进权限树。</Text>
              <Tree checkable defaultExpandAll checkedKeys={checkedKeys} onCheck={(keys) => setCheckedKeys((keys as string[]) ?? [])} treeData={treeData} />
            </>
          )
        }
      ]}
    />
  );
}

function buildTreeNodes(records: PermissionRecord[]): DataNode[] {
  return records.map((record) => ({
    key: record.id,
    title: `${renderTypeLabel(record.type)} ${record.name}${record.code ? ` (${record.code})` : ""}`,
    children: buildTreeNodes(record.children ?? [])
  }));
}

function renderTypeLabel(type: number) {
  if (type === 1) {
    return "[目录]";
  }
  if (type === 2) {
    return "[菜单]";
  }
  return "[按钮]";
}
