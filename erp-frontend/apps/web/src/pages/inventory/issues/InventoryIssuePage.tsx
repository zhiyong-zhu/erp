import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDependency, ProFormDigit, ProFormList, ProFormSelect, ProFormText } from "@ant-design/pro-components";
import { App, Button, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { createInventoryIssue, fetchInventoryIssues } from "../../../api/inventory";
import { fetchMaterials } from "../../../api/material";
import type { InventoryIssuePayload, InventoryIssueRecord } from "../../../types/inventory";
import type { MaterialRecord } from "../../../types/material";
import { printInventoryDocument } from "../../../utils/documentPrint";
import { useInventoryPositions } from "../components/useInventoryPositions";

const { Title, Text } = Typography;

const issueTypeOptions = [
  { label: "手工出库", value: "MANUAL_OUT" },
  { label: "生产领料", value: "PRODUCTION_PICK" },
  { label: "损耗出库", value: "LOSS_OUT" },
  { label: "其他出库", value: "OTHER_OUT" }
];

export function InventoryIssuePage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<InventoryIssueRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);
  const [materials, setMaterials] = useState<MaterialRecord[]>([]);
  const { message } = App.useApp();
  const { locationOptions, loadPositions, applyLocation } = useInventoryPositions();

  const materialOptions = useMemo(
    () => materials.map((item) => ({
      label: `${item.code} · ${item.name}（库存 ${item.currentStock ?? 0}${item.unit ? ` ${item.unit}` : ""}）`,
      value: item.id
    })),
    [materials]
  );

  useEffect(() => {
    void loadData();
    void loadMaterials();
    void loadPositions();
  }, []);

  async function loadData(nextPageNum = pageNum, nextPageSize = pageSize) {
    setLoading(true);
    try {
      const data = await fetchInventoryIssues({ pageNum: nextPageNum, pageSize: nextPageSize });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载出库单失败");
    } finally {
      setLoading(false);
    }
  }

  async function loadMaterials(keyword?: string) {
    try {
      const data = await fetchMaterials({ pageNum: 1, pageSize: 50, name: keyword, status: 1 });
      setMaterials(data.records);
    } catch {
      setMaterials([]);
    }
  }

  async function handleCreate(values: InventoryIssuePayload & { locationId?: string }) {
    try {
      const payload = normalizeIssuePayload(applyLocation(values, values.locationId));
      await createInventoryIssue(payload);
      message.success("出库成功");
      setCreateOpen(false);
      await loadData(1, pageSize);
      await loadMaterials();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "出库失败");
      return false;
    }
  }

  const columns: ColumnsType<InventoryIssueRecord> = [
    { title: "出库单号", dataIndex: "issueNo", key: "issueNo", width: 180 },
    {
      title: "出库类型",
      dataIndex: "issueType",
      key: "issueType",
      width: 140,
      render: (value: string) => issueTypeOptions.find((item) => item.value === value)?.label ?? value
    },
    { title: "来源单号", dataIndex: "sourceOrderNo", key: "sourceOrderNo", width: 180 },
    { title: "幂等键", dataIndex: "idempotencyKey", key: "idempotencyKey", width: 200, render: (value?: string | null) => value || "-" },
    { title: "出库总量", dataIndex: "totalQuantity", key: "totalQuantity", width: 120 },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 120,
      render: (value: string) => <Tag color={value === "COMPLETED" ? "green" : "default"}>{value}</Tag>
    },
    { title: "备注", dataIndex: "remark", key: "remark" },
    { title: "创建时间", dataIndex: "createdAt", key: "createdAt", width: 180 },
    {
      title: "操作",
      key: "actions",
      width: 90,
      fixed: "right",
      render: (_, record) => <Button type="link" onClick={() => printInventoryDocument("issue", record)}>打印</Button>
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>库存管理 / 出库管理</Title>
          <Text type="secondary">创建手工出库、生产领料或损耗出库，并同步扣减原料库存。</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
          新建出库
        </Button>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={records}
        loading={loading}
        scroll={{ x: 1200 }}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadData(nextPageNum, nextPageSize)
        }}
      />

      <ModalForm<InventoryIssuePayload>
        title="新建出库"
        open={createOpen}
        modalProps={{ destroyOnHidden: true, onCancel: () => setCreateOpen(false), width: 760 }}
        initialValues={{
          issueType: "MANUAL_OUT",
          batchNo: "DEFAULT",
          idempotencyKey: `issue-${Date.now()}`,
          items: [{}]
        }}
        onFinish={handleCreate}
      >
        <ProFormSelect
          name="issueType"
          label="出库类型"
          options={issueTypeOptions}
          rules={[{ required: true, message: "请选择出库类型" }]}
        />
        <ProFormText name="sourceOrderNo" label="来源单号" placeholder="可选，如生产工单号/调整单号" />
        <ProFormText name="idempotencyKey" label="幂等键" tooltip="重复提交时用于避免重复扣减库存" rules={[{ required: true, message: "请输入幂等键" }]} />
        <ProFormSelect
          name="locationId"
          label="出库库位"
          options={locationOptions}
          rules={[{ required: true, message: "请选择出库库位" }]}
        />
        <ProFormText name="batchNo" label="批次" rules={[{ required: true, message: "请输入批次" }]} />
        <ProFormText name="remark" label="备注" />
        <ProFormList
          name="items"
          label="出库明细"
          creatorButtonProps={{ creatorButtonText: "添加明细" }}
          rules={[
            {
              validator: async (_, value) => {
                if (!value || value.length === 0) {
                  throw new Error("至少添加一条出库明细");
                }
              }
            }
          ]}
        >
          <ProFormSelect
            name="materialId"
            label="原料"
            showSearch
            options={materialOptions}
            fieldProps={{
              filterOption: false,
              onSearch: (value) => void loadMaterials(value)
            }}
            rules={[{ required: true, message: "请选择原料" }]}
          />
          <ProFormDependency name={["materialId"]}>
            {({ materialId }) => {
              const material = materials.find((item) => item.id === materialId);
              return (
                <Text type="secondary">
                  当前库存：{material?.currentStock ?? "-"} {material?.unit ?? ""}
                </Text>
              );
            }}
          </ProFormDependency>
          <ProFormDigit
            name="quantity"
            label="出库数量"
            min={0.01}
            fieldProps={{ precision: 2 }}
            rules={[{ required: true, message: "请输入出库数量" }]}
          />
          <ProFormText name="remark" label="明细备注" />
        </ProFormList>
      </ModalForm>
    </section>
  );
}

function normalizeIssuePayload(values: Record<string, any>): InventoryIssuePayload {
  const { locationId: _locationId, warehouseId: _warehouseId, items, ...rest } = values;
  return {
    ...rest,
    items: items ?? []
  } as InventoryIssuePayload;
}
