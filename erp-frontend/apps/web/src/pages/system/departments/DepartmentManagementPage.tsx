import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormSelect, ProFormText } from "@ant-design/pro-components";
import { App, Button, Space, Switch, Table, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { createDepartment, fetchDepartments, updateDepartment, updateDepartmentStatus } from "../../../api/system";
import type { DepartmentPayload, DepartmentRecord } from "../../../types/system";

const { Title, Text } = Typography;

function flattenDepartments(departments: DepartmentRecord[]): DepartmentRecord[] {
  return departments.flatMap((department) => [department, ...flattenDepartments(department.children ?? [])]);
}

export function DepartmentManagementPage() {
  const [loading, setLoading] = useState(false);
  const [departments, setDepartments] = useState<DepartmentRecord[]>([]);
  const [createOpen, setCreateOpen] = useState(false);
  const [editingDepartment, setEditingDepartment] = useState<DepartmentRecord | null>(null);
  const { message } = App.useApp();

  const departmentOptions = useMemo(
    () => flattenDepartments(departments).map((department) => ({ label: `${department.name}（${department.code}）`, value: department.id })),
    [departments]
  );

  async function loadDepartments() {
    setLoading(true);
    try {
      setDepartments(await fetchDepartments());
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载部门失败");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadDepartments();
  }, []);

  async function handleCreate(values: DepartmentPayload) {
    try {
      await createDepartment(values);
      message.success("部门创建成功");
      setCreateOpen(false);
      await loadDepartments();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "部门创建失败");
      return false;
    }
  }

  async function handleUpdate(values: DepartmentPayload) {
    if (!editingDepartment) return false;
    try {
      await updateDepartment(editingDepartment.id, values);
      message.success("部门更新成功");
      setEditingDepartment(null);
      await loadDepartments();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "部门更新失败");
      return false;
    }
  }

  async function handleToggleStatus(department: DepartmentRecord, checked: boolean) {
    try {
      await updateDepartmentStatus(department.id, checked ? 1 : 0);
      message.success(checked ? "部门已启用" : "部门已禁用");
      await loadDepartments();
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "状态更新失败");
    }
  }

  const columns: ColumnsType<DepartmentRecord> = [
    { title: "部门名称", dataIndex: "name", key: "name" },
    { title: "编码", dataIndex: "code", key: "code" },
    { title: "负责人", dataIndex: "leader", key: "leader" },
    { title: "电话", dataIndex: "phone", key: "phone" },
    { title: "排序", dataIndex: "sortOrder", key: "sortOrder", width: 90 },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      render: (_, record) => <Switch checked={record.status === 1} checkedChildren="启用" unCheckedChildren="禁用" onChange={(checked) => void handleToggleStatus(record, checked)} />
    },
    {
      title: "操作",
      key: "actions",
      render: (_, record) => <Button type="link" onClick={() => setEditingDepartment(record)}>编辑</Button>
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>系统管理 / 部门管理</Title>
          <Text type="secondary">维护组织架构树，供用户归属和后续数据权限使用。</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>新建部门</Button>
      </div>

      <Table rowKey="id" columns={columns} dataSource={departments} loading={loading} pagination={false} />

      <DepartmentForm title="新建部门" open={createOpen} departmentOptions={departmentOptions} onCancel={() => setCreateOpen(false)} onFinish={handleCreate} />
      <DepartmentForm
        title="编辑部门"
        open={!!editingDepartment}
        departmentOptions={departmentOptions.filter((option) => option.value !== editingDepartment?.id)}
        initialValues={
          editingDepartment
            ? {
                parentId: editingDepartment.parentId ?? undefined,
                name: editingDepartment.name,
                code: editingDepartment.code,
                leader: editingDepartment.leader ?? "",
                phone: editingDepartment.phone ?? "",
                sortOrder: editingDepartment.sortOrder
              }
            : undefined
        }
        onCancel={() => setEditingDepartment(null)}
        onFinish={handleUpdate}
      />
    </section>
  );
}

function DepartmentForm({ title, open, initialValues, departmentOptions, onCancel, onFinish }: {
  title: string;
  open: boolean;
  initialValues?: Partial<DepartmentPayload>;
  departmentOptions: Array<{ label: string; value: string }>;
  onCancel: () => void;
  onFinish: (values: DepartmentPayload) => Promise<boolean>;
}) {
  return (
    <ModalForm<DepartmentPayload> title={title} open={open} initialValues={initialValues} modalProps={{ destroyOnClose: true, onCancel }} onFinish={onFinish}>
      <ProFormSelect name="parentId" label="上级部门" options={departmentOptions} allowClear />
      <ProFormText name="name" label="部门名称" rules={[{ required: true }]} />
      <ProFormText name="code" label="部门编码" rules={[{ required: true }]} />
      <ProFormText name="leader" label="负责人" />
      <ProFormText name="phone" label="电话" />
      <ProFormDigit name="sortOrder" label="排序" min={0} fieldProps={{ precision: 0 }} />
    </ModalForm>
  );
}
