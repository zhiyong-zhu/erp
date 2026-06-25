import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormSelect, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { INVENTORY_PERMISSIONS } from "@erp/shared";
import { App, Button, Input, Select, Space, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import {
  createInventoryLocation,
  fetchInventoryLocations,
  fetchInventoryWarehouses,
  updateInventoryLocation
} from "../../../api/inventory";
import { hasPermission } from "../../../store/auth";
import type { InventoryLocationPayload, InventoryLocationRecord, InventoryWarehouseRecord } from "../../../types/inventory";

const { Title, Text } = Typography;

export function InventoryLocationPage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<InventoryLocationRecord[]>([]);
  const [warehouses, setWarehouses] = useState<InventoryWarehouseRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [warehouseId, setWarehouseId] = useState<string | undefined>();
  const [createOpen, setCreateOpen] = useState(false);
  const [editingLocation, setEditingLocation] = useState<InventoryLocationRecord | null>(null);
  const { message } = App.useApp();
  const canCreate = hasPermission(INVENTORY_PERMISSIONS.LOCATION_CREATE);
  const canUpdate = hasPermission(INVENTORY_PERMISSIONS.LOCATION_UPDATE);

  const warehouseOptions = useMemo(
    () => warehouses.map((item) => ({ label: `${item.code} · ${item.name}`, value: item.id })),
    [warehouses]
  );

  useEffect(() => {
    void loadWarehouses();
    void loadData();
  }, []);

  async function loadWarehouses() {
    try {
      const data = await fetchInventoryWarehouses({ pageNum: 1, pageSize: 200, status: 1 });
      setWarehouses(data.records);
    } catch {
      setWarehouses([]);
    }
  }

  async function loadData(nextPageNum = pageNum, nextPageSize = pageSize, nextKeyword = keyword, nextWarehouseId = warehouseId) {
    setLoading(true);
    try {
      const data = await fetchInventoryLocations({
        pageNum: nextPageNum,
        pageSize: nextPageSize,
        keyword: nextKeyword || undefined,
        warehouseId: nextWarehouseId
      });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载库位失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(values: InventoryLocationPayload) {
    try {
      await createInventoryLocation(values);
      message.success("库位创建成功");
      setCreateOpen(false);
      await loadData(1, pageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "库位创建失败");
      return false;
    }
  }

  async function handleUpdate(values: InventoryLocationPayload) {
    if (!editingLocation) return false;
    try {
      await updateInventoryLocation(editingLocation.id, values);
      message.success("库位更新成功");
      setEditingLocation(null);
      await loadData();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "库位更新失败");
      return false;
    }
  }

  const columns: ColumnsType<InventoryLocationRecord> = [
    { title: "所属仓库", dataIndex: "warehouseName", key: "warehouseName", width: 180, render: (_, record) => `${record.warehouseCode} · ${record.warehouseName}` },
    { title: "库位编码", dataIndex: "code", key: "code", width: 140 },
    { title: "库位名称", dataIndex: "name", key: "name", width: 180 },
    { title: "区域编码", dataIndex: "areaCode", key: "areaCode", width: 120, render: formatEmpty },
    { title: "区域名称", dataIndex: "areaName", key: "areaName", width: 140, render: formatEmpty },
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
      render: (_, record) => <Button type="link" disabled={!canUpdate} onClick={() => setEditingLocation(record)}>编辑</Button>
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>库存管理 / 库位管理</Title>
          <Text type="secondary">维护仓库下的库位主数据，供库存单据选择和库存余额定位使用。</Text>
        </div>
        <Space>
          <Select
            allowClear
            placeholder="筛选仓库"
            options={warehouseOptions}
            value={warehouseId}
            onChange={(value) => {
              setWarehouseId(value);
              void loadData(1, pageSize, keyword, value);
            }}
            style={{ width: 220 }}
          />
          <Input.Search
            allowClear
            placeholder="搜索库位/仓库"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            onSearch={(value) => {
              setKeyword(value);
              void loadData(1, pageSize, value);
            }}
            style={{ width: 240 }}
          />
          <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateOpen(true)}>新建库位</Button>
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

      <LocationForm title="新建库位" open={createOpen} warehouseOptions={warehouseOptions} onCancel={() => setCreateOpen(false)} onFinish={handleCreate} />
      <LocationForm
        title="编辑库位"
        open={!!editingLocation}
        warehouseOptions={warehouseOptions}
        initialValues={editingLocation ? {
          warehouseId: editingLocation.warehouseId,
          code: editingLocation.code,
          name: editingLocation.name,
          areaCode: editingLocation.areaCode ?? "",
          areaName: editingLocation.areaName ?? "",
          sortOrder: editingLocation.sortOrder ?? 0,
          status: editingLocation.status,
          remark: editingLocation.remark ?? ""
        } : undefined}
        onCancel={() => setEditingLocation(null)}
        onFinish={handleUpdate}
      />
    </section>
  );
}

function LocationForm({ title, open, initialValues, warehouseOptions, onCancel, onFinish }: {
  title: string;
  open: boolean;
  initialValues?: Partial<InventoryLocationPayload>;
  warehouseOptions: Array<{ label: string; value: string }>;
  onCancel: () => void;
  onFinish: (values: InventoryLocationPayload) => Promise<boolean>;
}) {
  return (
    <ModalForm<InventoryLocationPayload>
      title={title}
      open={open}
      width={760}
      grid
      rowProps={{ gutter: 16 }}
      initialValues={initialValues ?? { status: 1, sortOrder: 0 }}
      modalProps={{ destroyOnClose: true, onCancel }}
      onFinish={onFinish}
    >
      <ProFormSelect name="warehouseId" label="所属仓库" options={warehouseOptions} rules={[{ required: true, message: "请选择所属仓库" }]} colProps={{ xs: 24, md: 12 }} />
      <ProFormText name="code" label="库位编码" rules={[{ required: true, message: "请输入库位编码" }]} colProps={{ xs: 24, md: 12 }} />
      <ProFormText name="name" label="库位名称" rules={[{ required: true, message: "请输入库位名称" }]} colProps={{ xs: 24, md: 12 }} />
      <ProFormText name="areaCode" label="区域编码" colProps={{ xs: 24, md: 12 }} />
      <ProFormText name="areaName" label="区域名称" colProps={{ xs: 24, md: 12 }} />
      <ProFormDigit name="sortOrder" label="排序" min={0} fieldProps={{ precision: 0 }} colProps={{ xs: 24, md: 12 }} />
      <ProFormSelect name="status" label="状态" options={[{ label: "启用", value: 1 }, { label: "禁用", value: 0 }]} colProps={{ xs: 24, md: 12 }} />
      <ProFormTextArea name="remark" label="备注" fieldProps={{ autoSize: { minRows: 2, maxRows: 3 } }} colProps={{ span: 24 }} />
    </ModalForm>
  );
}

function formatEmpty(value?: string | null) {
  return value || "-";
}
