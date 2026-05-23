import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormSelect, ProFormText } from "@ant-design/pro-components";
import { App, Button, Space, Switch, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { fetchDepartments, fetchRoles } from "../../../api/system";
import { createUser, fetchUsers, updateUser, updateUserStatus } from "../../../api/user";
import type { DepartmentRecord, RoleRecord } from "../../../types/system";
import type { UserCreatePayload, UserRecord, UserUpdatePayload } from "../../../types/user";

const { Title, Text } = Typography;

export function UserManagementPage() {
  const [loading, setLoading] = useState(false);
  const [users, setUsers] = useState<UserRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [departments, setDepartments] = useState<DepartmentRecord[]>([]);
  const [roles, setRoles] = useState<RoleRecord[]>([]);
  const [createOpen, setCreateOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<UserRecord | null>(null);
  const { message } = App.useApp();

  const departmentOptions = useMemo(
    () => flattenDepartments(departments).map((department) => ({ label: `${department.name}（${department.code}）`, value: department.id })),
    [departments]
  );
  const roleOptions = useMemo(() => roles.map((role) => ({ label: `${role.name}（${role.code}）`, value: role.id })), [roles]);

  async function loadUsers(nextPageNum = pageNum, nextPageSize = pageSize) {
    setLoading(true);
    try {
      const data = await fetchUsers({ pageNum: nextPageNum, pageSize: nextPageSize });
      setUsers(data.records);
      setTotal(data.total);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载用户失败");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadUsers();
    void fetchDepartments().then(setDepartments).catch(() => undefined);
    void fetchRoles({ pageNum: 1, pageSize: 100 }).then((data) => setRoles(data.records)).catch(() => undefined);
  }, []);

  async function handleCreate(values: UserCreatePayload) {
    try {
      await createUser(values);
      message.success("用户创建成功");
      setCreateOpen(false);
      await loadUsers(1, pageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "用户创建失败");
      return false;
    }
  }

  async function handleUpdate(values: UserUpdatePayload) {
    if (!editingUser) return false;
    try {
      await updateUser(editingUser.id, values);
      message.success("用户更新成功");
      setEditingUser(null);
      await loadUsers();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "用户更新失败");
      return false;
    }
  }

  async function handleToggleStatus(user: UserRecord, checked: boolean) {
    try {
      await updateUserStatus(user.id, checked ? 1 : 0);
      message.success(checked ? "用户已启用" : "用户已禁用");
      await loadUsers();
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "状态更新失败");
    }
  }

  const columns: ColumnsType<UserRecord> = [
    { title: "用户名", dataIndex: "username", key: "username" },
    { title: "姓名", dataIndex: "realName", key: "realName" },
    { title: "手机号", dataIndex: "phone", key: "phone" },
    { title: "邮箱", dataIndex: "email", key: "email" },
    { title: "部门", dataIndex: "departmentName", key: "departmentName", render: (value) => value ?? <Text type="secondary">未分配</Text> },
    {
      title: "角色",
      dataIndex: "roleCodes",
      key: "roleCodes",
      render: (roles: string[]) => (
        <Space wrap>
          {(roles ?? []).length ? roles.map((role) => <Tag key={role}>{role}</Tag>) : <Text type="secondary">未分配</Text>}
        </Space>
      )
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      render: (_, record) => (
        <Switch
          checked={record.status === 1}
          checkedChildren="启用"
          unCheckedChildren="禁用"
          onChange={(checked) => void handleToggleStatus(record, checked)}
        />
      )
    },
    {
      title: "操作",
      key: "actions",
      render: (_, record) => (
        <Button type="link" onClick={() => setEditingUser(record)}>
          编辑
        </Button>
      )
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>系统管理 / 用户管理</Title>
          <Text type="secondary">对接后端登录和用户管理接口，完成创建、编辑、启停用户的最小闭环。</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
          新建用户
        </Button>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={users}
        loading={loading}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadUsers(nextPageNum, nextPageSize)
        }}
      />

      <ModalForm<UserCreatePayload>
        title="新建用户"
        open={createOpen}
        modalProps={{ destroyOnClose: true, onCancel: () => setCreateOpen(false) }}
        onFinish={handleCreate}
      >
        <ProFormText name="username" label="用户名" rules={[{ required: true }]} />
        <ProFormText.Password name="password" label="密码" rules={[{ required: true }]} />
        <ProFormText name="realName" label="姓名" rules={[{ required: true }]} />
        <ProFormText name="phone" label="手机号" />
        <ProFormText name="email" label="邮箱" />
        <ProFormSelect name="departmentId" label="部门" options={departmentOptions} allowClear />
        <ProFormSelect name="roleIds" label="角色" options={roleOptions} mode="multiple" />
      </ModalForm>

      <ModalForm<UserUpdatePayload>
        title="编辑用户"
        open={!!editingUser}
        initialValues={
          editingUser
            ? {
                realName: editingUser.realName,
                phone: editingUser.phone ?? "",
                email: editingUser.email ?? "",
                departmentId: editingUser.departmentId ?? null,
                roleIds: editingUser.roleIds ?? []
              }
            : undefined
        }
        modalProps={{ destroyOnClose: true, onCancel: () => setEditingUser(null) }}
        onFinish={handleUpdate}
      >
        <ProFormText name="realName" label="姓名" rules={[{ required: true }]} />
        <ProFormText name="phone" label="手机号" />
        <ProFormText name="email" label="邮箱" />
        <ProFormSelect name="departmentId" label="部门" options={departmentOptions} allowClear />
        <ProFormSelect name="roleIds" label="角色" options={roleOptions} mode="multiple" />
      </ModalForm>
    </section>
  );
}

function flattenDepartments(departments: DepartmentRecord[]): DepartmentRecord[] {
  return departments.flatMap((department) => [department, ...flattenDepartments(department.children ?? [])]);
}
