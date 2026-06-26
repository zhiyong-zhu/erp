import { PlusOutlined } from "@ant-design/icons";
import { ProFormSelect } from "@ant-design/pro-components";
import { App, Button, Card, Empty, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { fetchMaterials } from "../../../api/material";
import { fetchProducts } from "../../../api/product";
import { createProductionBom, fetchProductionBoms, updateProductionBom } from "../../../api/production";
import { CreateForm } from "../../../components/CreateForm";
import type { ProductionBomItemRecord, ProductionBomPayload, ProductionBomRecord } from "../../../types/production";

const { Text } = Typography;

type BomFormValues = ProductionBomPayload;

interface MaterialOption {
  value: string;
  label: string;
  unit?: string | null;
}

const ITEM_TYPE_OPTIONS = [
  { label: "原料", value: 1 },
  { label: "半成品", value: 2 }
];

export function ProductBomTab({ productId, productName, canCreate, canUpdate, height }: {
  productId: string;
  productName?: string | null;
  canCreate: boolean;
  canUpdate: boolean;
  height: number;
}) {
  const { message } = App.useApp();
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<ProductionBomRecord[]>([]);
  const [createOpen, setCreateOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<ProductionBomRecord | null>(null);
  const [materialOptions, setMaterialOptions] = useState<MaterialOption[]>([]);
  const [semifinishedOptions, setSemifinishedOptions] = useState<MaterialOption[]>([]);

  useEffect(() => {
    void loadBoms();
    void loadMaterialOptions();
  }, [productId]);

  async function loadMaterialOptions() {
    try {
      const [materialData, productData] = await Promise.all([
        fetchMaterials({ pageNum: 1, pageSize: 500 }),
        fetchProducts({ pageNum: 1, pageSize: 500, isSemifinished: true })
      ]);
      setMaterialOptions(materialData.records.map((item) => ({
        value: item.id,
        label: `${item.name}${item.code ? ` (${item.code})` : ""}`,
        unit: item.unit
      })));
      setSemifinishedOptions(productData.records.map((item) => ({
        value: item.id,
        label: `${item.name}${item.code ? ` (${item.code})` : ""}`,
        unit: item.unit
      })));
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载物料列表失败");
    }
  }

  async function loadBoms() {
    setLoading(true);
    try {
      const data = await fetchProductionBoms({ pageNum: 1, pageSize: 100, productId });
      setRecords(data.records);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载生产BOM失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(values: BomFormValues) {
    try {
      await createProductionBom(normalizePayload({ ...values, productId }));
      message.success("生产BOM已创建");
      setCreateOpen(false);
      await loadBoms();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "创建生产BOM失败");
      return false;
    }
  }

  async function handleUpdate(values: BomFormValues) {
    if (!editingRecord?.id) return false;
    try {
      await updateProductionBom(editingRecord.id, normalizePayload({ ...values, productId }));
      message.success("生产BOM已更新");
      setEditingRecord(null);
      await loadBoms();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "更新生产BOM失败");
      return false;
    }
  }

  const columns: ColumnsType<ProductionBomRecord> = [
    { title: "BOM编码", dataIndex: "code", key: "code", width: 160 },
    { title: "版本", dataIndex: "version", key: "version", width: 100 },
    { title: "生效日期", dataIndex: "effectiveDate", key: "effectiveDate", width: 130, render: (value) => value ?? "-" },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 90,
      render: (value: number) => <Tag color={value === 1 ? "success" : "default"}>{value === 1 ? "启用" : "停用"}</Tag>
    },
    { title: "物料项", key: "items", width: 80, render: (_, record) => record.items?.length ?? 0 },
    { title: "备注", dataIndex: "remark", key: "remark", render: (value) => value ?? "-" },
    {
      title: "操作",
      key: "actions",
      width: 90,
      render: (_, record) => (
        <Button type="link" disabled={!canUpdate} onClick={() => setEditingRecord(record)}>
          编辑
        </Button>
      )
    }
  ];

  return (
    <div className="product-detail-tab-scroll" style={{ height }}>
      <Card
        size="small"
        extra={
          <Button type="primary" disabled={!canCreate} icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
            新建BOM
          </Button>
        }
      >
        <Text type="secondary">
          维护当前产品{productName ? `（${productName}）` : ""}的生产用料清单，支持多版本与生效日期。
        </Text>
        <Table
          rowKey="id"
          style={{ marginTop: 12 }}
          columns={columns}
          dataSource={records}
          loading={loading}
          size="small"
          locale={{ emptyText: <Empty description="暂无生产BOM" /> }}
          pagination={false}
          scroll={{ y: Math.max(height - 150, 120) }}
          expandable={{
            expandedRowRender: (record) => (
              <Table
                rowKey={(item) => String(item.id ?? item.rowId ?? item.materialId)}
                size="small"
                pagination={false}
                columns={[
                  { title: "物料", dataIndex: "materialName", key: "materialName", render: (value, item) => value ?? item.materialCode ?? item.materialId },
                  { title: "类型", dataIndex: "itemType", key: "itemType", width: 80, render: (value) => <Tag color={value === 2 ? "orange" : "blue"}>{value === 2 ? "半成品" : "原料"}</Tag> },
                  { title: "数量", dataIndex: "quantity", key: "quantity", width: 100 },
                  { title: "单位", dataIndex: "unit", key: "unit", width: 80 },
                  { title: "损耗率%", dataIndex: "lossRate", key: "lossRate", width: 100 },
                  { title: "工序号", dataIndex: "processStepNo", key: "processStepNo", width: 100 },
                  { title: "备注", dataIndex: "remark", key: "remark" }
                ]}
                dataSource={record.items ?? []}
              />
            )
          }}
        />
      </Card>

      <BomForm
        title="新建生产BOM"
        open={createOpen}
        productName={productName}
        materialOptions={materialOptions}
        semifinishedOptions={semifinishedOptions}
        onCancel={() => setCreateOpen(false)}
        onFinish={handleCreate}
      />
      <BomForm
        title="编辑生产BOM"
        open={!!editingRecord}
        productName={productName}
        materialOptions={materialOptions}
        semifinishedOptions={semifinishedOptions}
        initialValues={editingRecord ? toBomInitialValues(editingRecord) : undefined}
        onCancel={() => setEditingRecord(null)}
        onFinish={handleUpdate}
      />
    </div>
  );
}

function BomForm({
  title,
  open,
  productName,
  materialOptions,
  semifinishedOptions,
  initialValues,
  onCancel,
  onFinish
}: {
  title: string;
  open: boolean;
  productName?: string | null;
  materialOptions: MaterialOption[];
  semifinishedOptions: MaterialOption[];
  initialValues?: Partial<BomFormValues>;
  onCancel: () => void;
  onFinish: (values: BomFormValues) => Promise<boolean>;
}) {
  return (
    <CreateForm
      title={title}
      open={open}
      width={980}
      bodyMaxHeight="72vh"
      initialValues={initialValues ?? { version: "V1.0", status: 1, items: [{ itemType: 1, materialId: undefined, quantity: 1, lossRate: 0 }] }}
      onCancel={onCancel}
      onFinish={onFinish}
      sections={[
        {
          title: "BOM 信息",
          fields: [
            { type: "text", name: "code", label: "BOM编码", rules: [{ required: true }], colSpan: 8 },
            { type: "text", name: "productName", label: "产品", disabled: true, colSpan: 8, fieldProps: { value: productName ?? "" } },
            { type: "text", name: "version", label: "版本", rules: [{ required: true }], colSpan: 8 },
            { type: "datepicker", name: "effectiveDate", label: "生效日期", colSpan: 8 },
            { type: "select", name: "status", label: "状态", options: [{ label: "启用", value: 1 }, { label: "停用", value: 0 }], colSpan: 8 },
            { type: "textarea", name: "remark", label: "备注", colSpan: 24 }
          ]
        },
        {
          title: "物料明细",
          fields: [
            {
              type: "list",
              name: "items",
              creatorButtonText: "添加物料",
              rowGutter: 12,
              rowFields: [
                { type: "select", name: "itemType", label: "类型", options: ITEM_TYPE_OPTIONS, rules: [{ required: true }], colSpan: 3 },
                {
                  type: "dep",
                  watch: ["itemType"],
                  colSpan: 7,
                  render: ({ itemType }) => {
                    const options = itemType === 2 ? semifinishedOptions : materialOptions;
                    const label = itemType === 2 ? "半成品" : "原料";
                    return (
                      <ProFormSelect
                        name="materialId"
                        label={label}
                        options={options}
                        showSearch
                        fieldProps={{ optionFilterProp: "label", placeholder: `搜索并选择${label}` }}
                        rules={[{ required: true, message: `请选择${label}` }]}
                      />
                    );
                  }
                },
                { type: "digit", name: "quantity", label: "数量", min: 0.0001, precision: 4, rules: [{ required: true }], colSpan: 3 },
                { type: "text", name: "unit", label: "单位", colSpan: 3 },
                { type: "digit", name: "lossRate", label: "损耗率%", min: 0, precision: 2, colSpan: 3 },
                { type: "digit", name: "processStepNo", label: "工序号", min: 1, precision: 0, colSpan: 2 },
                { type: "text", name: "remark", label: "备注", colSpan: 3 }
              ]
            }
          ]
        }
      ]}
    />
  );
}

function normalizePayload(values: BomFormValues): ProductionBomPayload {
  return {
    ...values,
    items: (values.items ?? []).map(({ rowId: _rowId, ...item }: ProductionBomItemRecord) => item)
  };
}

function toBomInitialValues(record: ProductionBomRecord): Partial<BomFormValues> {
  return {
    code: record.code,
    productId: record.productId,
    version: record.version,
    status: record.status,
    effectiveDate: record.effectiveDate ?? undefined,
    remark: record.remark ?? undefined,
    items: record.items ?? []
  };
}
