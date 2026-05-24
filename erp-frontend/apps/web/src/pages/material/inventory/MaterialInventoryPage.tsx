import { EditOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormText } from "@ant-design/pro-components";
import { App, Button, Input, Table, Tag, Typography } from "antd";
import { MATERIAL_PERMISSIONS } from "@erp/shared";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchMaterials, updateMaterial } from "../../../api/material";
import { hasPermission } from "../../../store/auth";
import type { MaterialPayload, MaterialRecord } from "../../../types/material";

const { Title, Text } = Typography;

export function MaterialInventoryPage() {
  const [loading, setLoading] = useState(false);
  const [materials, setMaterials] = useState<MaterialRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [editingMaterial, setEditingMaterial] = useState<MaterialRecord | null>(null);
  const { message } = App.useApp();
  const canUpdate = hasPermission(MATERIAL_PERMISSIONS.MATERIAL_UPDATE);

  useEffect(() => {
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

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>原料管理 / 库存台账</Title>
          <Text type="secondary">集中查看和维护原料当前库存，低于安全库存时会直接高亮。</Text>
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

      <ModalForm<Pick<MaterialPayload, "currentStock">>
        title="调整库存"
        open={!!editingMaterial}
        width={640}
        initialValues={{ currentStock: editingMaterial?.currentStock ?? 0 }}
        modalProps={{ destroyOnClose: true, onCancel: () => setEditingMaterial(null) }}
        onFinish={handleUpdateInventory}
      >
        <ProFormText name="codePreview" label="原料编码" initialValue={editingMaterial?.code} readonly />
        <ProFormText name="namePreview" label="原料名称" initialValue={editingMaterial?.name} readonly />
        <ProFormDigit name="currentStock" label="当前库存" min={0} fieldProps={{ precision: 2 }} rules={[{ required: true }]} />
      </ModalForm>
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
