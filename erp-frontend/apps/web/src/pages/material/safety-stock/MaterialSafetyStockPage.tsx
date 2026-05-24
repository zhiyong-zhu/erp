import { EditOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormSelect, ProFormText } from "@ant-design/pro-components";
import { App, Button, Input, Table, Tag, Typography } from "antd";
import { MATERIAL_PERMISSIONS } from "@erp/shared";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { fetchMaterials, fetchSuppliers, updateMaterial } from "../../../api/material";
import { hasPermission } from "../../../store/auth";
import type { MaterialPayload, MaterialRecord, SupplierRecord } from "../../../types/material";

const { Title, Text } = Typography;

export function MaterialSafetyStockPage() {
  const [loading, setLoading] = useState(false);
  const [materials, setMaterials] = useState<MaterialRecord[]>([]);
  const [suppliers, setSuppliers] = useState<SupplierRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [editingMaterial, setEditingMaterial] = useState<MaterialRecord | null>(null);
  const { message } = App.useApp();
  const canUpdate = hasPermission(MATERIAL_PERMISSIONS.MATERIAL_UPDATE);

  const supplierOptions = useMemo(
    () => suppliers.filter((item) => item.status === 1).map((item) => ({ label: item.name, value: item.id })),
    [suppliers]
  );

  useEffect(() => {
    void fetchSuppliers({ pageNum: 1, pageSize: 100 }).then((data) => setSuppliers(data.records)).catch(() => undefined);
    void loadMaterials();
  }, []);

  async function loadMaterials(nextPageNum = pageNum, nextPageSize = pageSize, name = keyword) {
    setLoading(true);
    try {
      const data = await fetchMaterials({ pageNum: nextPageNum, pageSize: nextPageSize, name });
      setMaterials(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载安全库存设置失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleUpdateSafetyStock(values: Pick<MaterialPayload, "safetyStock" | "leadTimeDays" | "defaultSupplierId">) {
    if (!editingMaterial?.id) {
      return false;
    }
    try {
      await updateMaterial(editingMaterial.id, buildPayload(editingMaterial, values));
      message.success("安全库存设置更新成功");
      setEditingMaterial(null);
      await loadMaterials();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "安全库存设置更新失败");
      return false;
    }
  }

  const columns: ColumnsType<MaterialRecord> = [
    { title: "原料编码", dataIndex: "code", key: "code", width: 140 },
    { title: "原料名称", dataIndex: "name", key: "name" },
    { title: "分类", dataIndex: "categoryName", key: "categoryName", width: 140 },
    {
      title: "安全库存",
      dataIndex: "safetyStock",
      key: "safetyStock",
      width: 110,
      render: (value?: number | null) => value ?? 0
    },
    {
      title: "当前库存",
      dataIndex: "currentStock",
      key: "currentStock",
      width: 110,
      render: (value: number | null | undefined, record: MaterialRecord) => {
        const currentStock = value ?? 0;
        const safetyStock = record.safetyStock ?? 0;
        return currentStock <= safetyStock ? <Tag color="red">{currentStock}</Tag> : currentStock;
      }
    },
    { title: "采购周期(天)", dataIndex: "leadTimeDays", key: "leadTimeDays", width: 120 },
    { title: "默认供应商", dataIndex: "defaultSupplierName", key: "defaultSupplierName", width: 180 },
    {
      title: "操作",
      key: "actions",
      width: 120,
      render: (_, record) => (
        <Button type="link" icon={<EditOutlined />} disabled={!canUpdate} onClick={() => setEditingMaterial(record)}>
          设置规则
        </Button>
      )
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>原料管理 / 安全库存设置</Title>
          <Text type="secondary">集中维护原料安全库存、采购周期和默认供应商，作为补货规则的基础。</Text>
        </div>
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

      <ModalForm<Pick<MaterialPayload, "safetyStock" | "leadTimeDays" | "defaultSupplierId">>
        title="安全库存设置"
        open={!!editingMaterial}
        width={720}
        initialValues={{
          safetyStock: editingMaterial?.safetyStock ?? 0,
          leadTimeDays: editingMaterial?.leadTimeDays ?? undefined,
          defaultSupplierId: editingMaterial?.defaultSupplierId ?? undefined
        }}
        modalProps={{ destroyOnClose: true, onCancel: () => setEditingMaterial(null) }}
        onFinish={handleUpdateSafetyStock}
      >
        <ProFormText name="codePreview" label="原料编码" initialValue={editingMaterial?.code} readonly />
        <ProFormText name="namePreview" label="原料名称" initialValue={editingMaterial?.name} readonly />
        <ProFormDigit name="safetyStock" label="安全库存" min={0} fieldProps={{ precision: 2 }} rules={[{ required: true }]} />
        <ProFormDigit name="leadTimeDays" label="采购周期(天)" min={0} fieldProps={{ precision: 0 }} />
        <ProFormSelect name="defaultSupplierId" label="默认供应商" options={supplierOptions} allowClear />
      </ModalForm>
    </section>
  );
}

function buildPayload(
  record: MaterialRecord,
  patch: Partial<Pick<MaterialPayload, "safetyStock" | "leadTimeDays" | "defaultSupplierId">>
): MaterialPayload {
  return {
    code: record.code,
    name: record.name,
    categoryId: record.categoryId ?? undefined,
    unit: record.unit,
    specifications: record.specifications ?? undefined,
    defaultSupplierId: patch.defaultSupplierId ?? record.defaultSupplierId ?? undefined,
    safetyStock: patch.safetyStock ?? record.safetyStock ?? undefined,
    currentStock: record.currentStock ?? undefined,
    leadTimeDays: patch.leadTimeDays ?? record.leadTimeDays ?? undefined,
    status: record.status
  };
}
