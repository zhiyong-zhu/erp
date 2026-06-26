import { EditOutlined } from "@ant-design/icons";
import { App, Button, Input, Space, Table, Tabs, Tag, Typography } from "antd";
import { MATERIAL_PERMISSIONS } from "@erp/shared";
import { CreateForm } from "../../../components/CreateForm";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { exportInventoryBalances, fetchInventoryBalances } from "../../../api/inventory";
import { fetchMaterials, updateMaterial } from "../../../api/material";
import { hasPermission } from "../../../store/auth";
import type { InventoryBalanceRecord } from "../../../types/inventory";
import type { MaterialPayload, MaterialRecord } from "../../../types/material";
import { downloadBlob } from "../../../utils/export";

const { Title, Text } = Typography;

interface BalanceFilters {
  materialName: string;
  warehouseCode: string;
  locationCode: string;
  batchNo: string;
}

export function MaterialInventoryPage() {
  const [loading, setLoading] = useState(false);
  const [balanceLoading, setBalanceLoading] = useState(false);
  const [materials, setMaterials] = useState<MaterialRecord[]>([]);
  const [balances, setBalances] = useState<InventoryBalanceRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [balancePageNum, setBalancePageNum] = useState(1);
  const [balancePageSize, setBalancePageSize] = useState(10);
  const [balanceTotal, setBalanceTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [balanceFilters, setBalanceFilters] = useState<BalanceFilters>(createEmptyBalanceFilters());
  const [editingMaterial, setEditingMaterial] = useState<MaterialRecord | null>(null);
  const { message } = App.useApp();
  const canUpdate = hasPermission(MATERIAL_PERMISSIONS.MATERIAL_UPDATE);

  useEffect(() => {
    void loadBalances();
    void loadMaterials();
  }, []);

  async function loadBalances(
    nextPageNum = balancePageNum,
    nextPageSize = balancePageSize,
    filters = balanceFilters
  ) {
    setBalanceLoading(true);
    try {
      const data = await fetchInventoryBalances({
        pageNum: nextPageNum,
        pageSize: nextPageSize,
        materialName: filters.materialName || undefined,
        warehouseCode: filters.warehouseCode || undefined,
        locationCode: filters.locationCode || undefined,
        batchNo: filters.batchNo || undefined
      });
      setBalances(data.records);
      setBalancePageNum(data.pageNum);
      setBalancePageSize(data.pageSize);
      setBalanceTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载库存余额失败");
    } finally {
      setBalanceLoading(false);
    }
  }

  async function loadMaterials(nextPageNum = pageNum, nextPageSize = pageSize, name = keyword) {
    setLoading(true);
    try {
      const data = await fetchMaterials({ pageNum: nextPageNum, pageSize: nextPageSize, name });
      setMaterials(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载库存台账失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleUpdateInventory(values: Pick<MaterialPayload, "currentStock">) {
    if (!editingMaterial?.id) {
      return false;
    }
    try {
      await updateMaterial(editingMaterial.id, buildPayload(editingMaterial, values));
      message.success("库存更新成功");
      setEditingMaterial(null);
      await loadMaterials();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "库存更新失败");
      return false;
    }
  }

  async function handleExportBalances() {
    try {
      const blob = await exportInventoryBalances({
        materialName: balanceFilters.materialName || undefined,
        warehouseCode: balanceFilters.warehouseCode || undefined,
        locationCode: balanceFilters.locationCode || undefined,
        batchNo: balanceFilters.batchNo || undefined
      });
      downloadBlob("inventory-balances.xlsx", blob);
      message.success("库存余额导出成功");
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "库存余额导出失败");
    }
  }

  function handleBalanceFilterChange(field: keyof BalanceFilters, value: string) {
    setBalanceFilters((current) => ({ ...current, [field]: value }));
  }

  async function handleSearchBalances() {
    await loadBalances(1, balancePageSize, balanceFilters);
  }

  async function handleResetBalances() {
    const emptyFilters = createEmptyBalanceFilters();
    setBalanceFilters(emptyFilters);
    await loadBalances(1, balancePageSize, emptyFilters);
  }

  const columns: ColumnsType<MaterialRecord> = [
    { title: "原料编码", dataIndex: "code", key: "code", width: 140 },
    { title: "原料名称", dataIndex: "name", key: "name" },
    { title: "分类", dataIndex: "categoryName", key: "categoryName", width: 140 },
    { title: "单位", dataIndex: "unit", key: "unit", width: 90 },
    {
      title: "当前库存",
      dataIndex: "currentStock",
      key: "currentStock",
      width: 110,
      render: (value?: number | null) => value ?? 0
    },
    {
      title: "安全库存",
      dataIndex: "safetyStock",
      key: "safetyStock",
      width: 110,
      render: (value: number | null | undefined, record: MaterialRecord) => {
        const currentStock = record.currentStock ?? 0;
        const safetyStock = value ?? 0;
        return currentStock <= safetyStock ? <Tag color="red">{safetyStock}</Tag> : safetyStock;
      }
    },
    { title: "默认供应商", dataIndex: "defaultSupplierName", key: "defaultSupplierName", width: 160 },
    { title: "采购周期(天)", dataIndex: "leadTimeDays", key: "leadTimeDays", width: 120 },
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
      width: 120,
      render: (_, record) => (
        <Button type="link" icon={<EditOutlined />} disabled={!canUpdate} onClick={() => setEditingMaterial(record)}>
          调整库存
        </Button>
      )
    }
  ];

  const balanceColumns: ColumnsType<InventoryBalanceRecord> = [
    { title: "原料编码", dataIndex: "materialCode", key: "materialCode", width: 140 },
    { title: "原料名称", dataIndex: "materialName", key: "materialName", width: 180 },
    {
      title: "仓库",
      dataIndex: "warehouseName",
      key: "warehouseName",
      width: 160,
      render: (value: string | null | undefined, record) => value ? `${value}（${record.warehouseCode}）` : record.warehouseCode
    },
    {
      title: "库位",
      dataIndex: "locationName",
      key: "locationName",
      width: 160,
      render: (value: string | null | undefined, record) => value ? `${value}（${record.locationCode}）` : record.locationCode
    },
    { title: "批次", dataIndex: "batchNo", key: "batchNo", width: 140 },
    {
      title: "可用库存",
      dataIndex: "availableQuantity",
      key: "availableQuantity",
      width: 120,
      render: formatQuantity
    },
    {
      title: "冻结库存",
      dataIndex: "frozenQuantity",
      key: "frozenQuantity",
      width: 120,
      render: formatQuantity
    },
    {
      title: "总库存",
      dataIndex: "totalQuantity",
      key: "totalQuantity",
      width: 120,
      render: formatQuantity
    },
    {
      title: "更新时间",
      dataIndex: "updatedAt",
      key: "updatedAt",
      width: 180,
      render: formatDateTime
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>原料管理 / 库存台账</Title>
          <Text type="secondary">优先查看仓库、库位、批次维度的库存余额；物料总库存保留为历史台账视图。</Text>
        </div>
      </div>

      <Tabs
        defaultActiveKey="balances"
        items={[
          {
            key: "balances",
            label: "库存余额",
            children: (
              <>
                <Space wrap style={{ marginBottom: 16 }}>
                  <Input
                    allowClear
                    placeholder="原料名称"
                    value={balanceFilters.materialName}
                    onChange={(event) => handleBalanceFilterChange("materialName", event.target.value)}
                    style={{ width: 180 }}
                  />
                  <Input
                    allowClear
                    placeholder="仓库编码"
                    value={balanceFilters.warehouseCode}
                    onChange={(event) => handleBalanceFilterChange("warehouseCode", event.target.value)}
                    style={{ width: 160 }}
                  />
                  <Input
                    allowClear
                    placeholder="库位编码"
                    value={balanceFilters.locationCode}
                    onChange={(event) => handleBalanceFilterChange("locationCode", event.target.value)}
                    style={{ width: 160 }}
                  />
                  <Input
                    allowClear
                    placeholder="批次"
                    value={balanceFilters.batchNo}
                    onChange={(event) => handleBalanceFilterChange("batchNo", event.target.value)}
                    style={{ width: 160 }}
                  />
                  <Button type="primary" onClick={() => void handleSearchBalances()}>查询</Button>
                  <Button onClick={() => void handleResetBalances()}>重置</Button>
                  <Button onClick={() => void handleExportBalances()}>导出余额Excel</Button>
                </Space>
                <Table
                  rowKey="id"
                  columns={balanceColumns}
                  dataSource={balances}
                  loading={balanceLoading}
                  scroll={{ x: 1280 }}
                  pagination={{
                    current: balancePageNum,
                    pageSize: balancePageSize,
                    total: balanceTotal,
                    showSizeChanger: true,
                    showTotal: (count) => `共 ${count} 条`,
                    onChange: (nextPageNum, nextPageSize) => void loadBalances(nextPageNum, nextPageSize)
                  }}
                />
              </>
            )
          },
          {
            key: "materials",
            label: "物料总库存",
            children: (
              <>
                <div style={{ display: "flex", justifyContent: "flex-end", marginBottom: 16 }}>
                  <Input.Search
                    allowClear
                    placeholder="搜索原料名称"
                    value={keyword}
                    onChange={(event) => setKeyword(event.target.value)}
                    onSearch={(value) => {
                      setKeyword(value);
                      void loadMaterials(1, pageSize, value);
                    }}
                    style={{ width: 240 }}
                  />
                </div>
                <Table
                  rowKey="id"
                  columns={columns}
                  dataSource={materials}
                  loading={loading}
                  pagination={{
                    current: pageNum,
                    pageSize,
                    total,
                    showSizeChanger: true,
                    showTotal: (count) => `共 ${count} 条`,
                    onChange: (nextPageNum, nextPageSize) => void loadMaterials(nextPageNum, nextPageSize)
                  }}
                />
              </>
            )
          }
        ]}
      />

      <CreateForm
        title="调整库存"
        open={!!editingMaterial}
        width={640}
        initialValues={{ currentStock: editingMaterial?.currentStock ?? 0 }}
        onCancel={() => setEditingMaterial(null)}
        onFinish={handleUpdateInventory}
        sections={[
          {
            title: "库存调整",
            fields: [
              { type: "text", name: "codePreview", label: "原料编码", colSpan: 12, fieldProps: { readOnly: true, value: editingMaterial?.code } },
              { type: "text", name: "namePreview", label: "原料名称", colSpan: 12, fieldProps: { readOnly: true, value: editingMaterial?.name } },
              { type: "digit", name: "currentStock", label: "当前库存", min: 0, precision: 2, rules: [{ required: true }], colSpan: 24 }
            ]
          }
        ]}
      />
    </section>
  );
}

function buildPayload(record: MaterialRecord, patch: Partial<MaterialPayload>): MaterialPayload {
  return {
    code: record.code,
    name: record.name,
    categoryId: record.categoryId ?? undefined,
    unit: record.unit,
    specifications: record.specifications ?? undefined,
    defaultSupplierId: record.defaultSupplierId ?? undefined,
    safetyStock: record.safetyStock ?? undefined,
    currentStock: patch.currentStock ?? record.currentStock ?? undefined,
    leadTimeDays: record.leadTimeDays ?? undefined,
    status: record.status
  };
}

function createEmptyBalanceFilters(): BalanceFilters {
  return {
    materialName: "",
    warehouseCode: "",
    locationCode: "",
    batchNo: ""
  };
}

function formatQuantity(value?: number | null) {
  return value ?? 0;
}

function formatDateTime(value?: string | null) {
  return value ? new Date(value).toLocaleString() : "-";
}
