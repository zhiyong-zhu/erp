import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormSelect, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { INVENTORY_PERMISSIONS } from "@erp/shared";
import { App, Button, Input, Space, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { createInventoryWarehouse, fetchInventoryWarehouses, updateInventoryWarehouse } from "../../../api/inventory";
import { hasPermission } from "../../../store/auth";
import type { InventoryWarehousePayload, InventoryWarehouseRecord } from "../../../types/inventory";

const { Title, Text } = Typography;

export function InventoryWarehousePage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<InventoryWarehouseRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editingWarehouse, setEditingWarehouse] = useState<InventoryWarehouseRecord | null>(null);
  const { message } = App.useApp();
  const canCreate = hasPermission(INVENTORY_PERMISSIONS.WAREHOUSE_CREATE);
  const canUpdate = hasPermission(INVENTORY_PERMISSIONS.WAREHOUSE_UPDATE);

  useEffect(() => {
    void loadData();
  }, []);

  async function loadData(nextPageNum = pageNum, nextPageSize = pageSize, nextKeyword = keyword) {
    setLoading(true);
    try {
      const data = await fetchInventoryWarehouses({ pageNum: nextPageNum, pageSize: nextPageSize, keyword: nextKeyword || undefined });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载仓库失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(values: InventoryWarehousePayload) {
    try {
      await createInventoryWarehouse(values);
      message.success("仓库创建成功");
      setCreateOpen(false);
      await loadData(1, pageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "仓库创建失败");
      return false;
    }
  }

  async function handleUpdate(values: InventoryWarehousePayload) {
    if (!editingWarehouse) return false;
    try {
      await updateInventoryWarehouse(editingWarehouse.id, values);
      message.success("仓库更新成功");
      setEditingWarehouse(null);
      await loadData();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "仓库更新失败");
      return false;
    }
  }

  const columns: ColumnsType<InventoryWarehouseRecord> = [
    { title: "仓库编码", dataIndex: "code", key: "code", width: 140 },
    { title: "仓库名称", dataIndex: "name", key: "name", width: 180 },
    { title: "负责人", dataIndex: "managerName", key: "managerName", width: 120, render: formatEmpty },
    { title: "电话", dataIndex: "phone", key: "phone", width: 140, render: formatEmpty },
    { title: "地址", dataIndex: "address", key: "address", render: formatEmpty },
    { title: "排序", dataIndex: "sortOrder", key: "sortOrder", width: 90 },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 100,
      render: (value: number) => <Tag color={value === 1 ? "green" : "default"}>{value === 1 ? "启用" : "禁用"}</Tag>
    },
    {
      title: "操作",
      key: "actions",
      width: 100,
      fixed: "right",
      render: (_, record) => <Button type="link" disabled={!canUpdate} onClick={() => setEditingWarehouse(record)}>编辑</Button>
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>库存管理 / 仓库管理</Title>
          <Text type="secondary">维护仓库主数据，为入库、出库、调拨、盘点提供统一仓库来源。</Text>
        </div>
        <Space>
          <Input.Search
            allowClear
            placeholder="搜索仓库编码/名称"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            onSearch={(value) => {
              setKeyword(value);
              void loadData(1, pageSize, value);
            }}
            style={{ width: 260 }}
          />
          <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateOpen(true)}>新建仓库</Button>
        </Space>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={records}
        loading={loading}
        scroll={{ x: 1100 }}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadData(nextPageNum, nextPageSize)
        }}
      />

      <WarehouseForm title="新建仓库" open={createOpen} onCancel={() => setCreateOpen(false)} onFinish={handleCreate} />
      <WarehouseForm
        title="编辑仓库"
        open={!!editingWarehouse}
        initialValues={editingWarehouse ? {
          code: editingWarehouse.code,
          name: editingWarehouse.name,
          address: editingWarehouse.address ?? "",
          managerName: editingWarehouse.managerName ?? "",
          phone: editingWarehouse.phone ?? "",
          sortOrder: editingWarehouse.sortOrder ?? 0,
          status: editingWarehouse.status,
          remark: editingWarehouse.remark ?? ""
        } : undefined}
        onCancel={() => setEditingWarehouse(null)}
        onFinish={handleUpdate}
      />
    </section>
  );
}

function WarehouseForm({ title, open, initialValues, onCancel, onFinish }: {
  title: string;
  open: boolean;
  initialValues?: Partial<InventoryWarehousePayload>;
  onCancel: () => void;
  onFinish: (values: InventoryWarehousePayload) => Promise<boolean>;
}) {
  return (
    <ModalForm<InventoryWarehousePayload>
      title={title}
      open={open}
      width={760}
      grid
      rowProps={{ gutter: 16 }}
      initialValues={initialValues ?? { status: 1, sortOrder: 0 }}
      modalProps={{ destroyOnClose: true, onCancel }}
      onFinish={onFinish}
    >
      <ProFormText name="code" label="仓库编码" rules={[{ required: true, message: "请输入仓库编码" }]} colProps={{ xs: 24, md: 12 }} />
      <ProFormText name="name" label="仓库名称" rules={[{ required: true, message: "请输入仓库名称" }]} colProps={{ xs: 24, md: 12 }} />
      <ProFormText name="managerName" label="负责人" colProps={{ xs: 24, md: 12 }} />
      <ProFormText name="phone" label="联系电话" colProps={{ xs: 24, md: 12 }} />
      <ProFormDigit name="sortOrder" label="排序" min={0} fieldProps={{ precision: 0 }} colProps={{ xs: 24, md: 12 }} />
      <ProFormSelect name="status" label="状态" options={[{ label: "启用", value: 1 }, { label: "禁用", value: 0 }]} colProps={{ xs: 24, md: 12 }} />
      <ProFormTextArea name="address" label="地址" fieldProps={{ autoSize: { minRows: 2, maxRows: 3 } }} colProps={{ span: 24 }} />
      <ProFormTextArea name="remark" label="备注" fieldProps={{ autoSize: { minRows: 2, maxRows: 3 } }} colProps={{ span: 24 }} />
    </ModalForm>
  );
}

function formatEmpty(value?: string | null) {
  return value || "-";
}
