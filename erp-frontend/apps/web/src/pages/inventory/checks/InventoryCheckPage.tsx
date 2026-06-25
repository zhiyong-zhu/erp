import { CheckCircleOutlined, CloseCircleOutlined, PlusOutlined, SafetyCertificateOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDependency, ProFormDigit, ProFormList, ProFormSelect, ProFormText } from "@ant-design/pro-components";
import { App, Button, Popconfirm, Space, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { approveInventoryCheck, createInventoryCheck, fetchInventoryChecks, rejectInventoryCheck, reviewInventoryCheck } from "../../../api/inventory";
import { fetchMaterials } from "../../../api/material";
import type { InventoryCheckPayload, InventoryCheckRecord } from "../../../types/inventory";
import type { MaterialRecord } from "../../../types/material";
import { printInventoryDocument } from "../../../utils/documentPrint";
import { useInventoryPositions } from "../components/useInventoryPositions";

const { Title, Text } = Typography;

const checkTypeOptions = [
  { label: "全盘", value: "FULL" },
  { label: "抽盘", value: "SPOT" },
  { label: "循环盘点", value: "CYCLE" }
];

const statusMap: Record<string, { label: string; color: string }> = {
  FROZEN: { label: "已冻结", color: "blue" },
  REVIEWED: { label: "已复核", color: "gold" },
  APPROVED: { label: "已审批", color: "green" },
  REJECTED: { label: "已驳回", color: "red" },
  COMPLETED: { label: "已完成", color: "green" }
};

export function InventoryCheckPage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<InventoryCheckRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);
  const [materials, setMaterials] = useState<MaterialRecord[]>([]);
  const { message } = App.useApp();
  const { locationOptions, loadPositions, applyLocation } = useInventoryPositions();

  const materialOptions = useMemo(
    () => materials.map((item) => ({
      label: `${item.code} · ${item.name}（账面 ${item.currentStock ?? 0}${item.unit ? ` ${item.unit}` : ""}）`,
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
      const data = await fetchInventoryChecks({ pageNum: nextPageNum, pageSize: nextPageSize });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载盘点单失败");
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

  async function handleCreate(values: InventoryCheckPayload & { locationId?: string }) {
    try {
      const payload = normalizeCheckPayload(applyLocation(values, values.locationId));
      await createInventoryCheck(payload);
      message.success("盘点单已创建，账面库存已冻结待复核");
      setCreateOpen(false);
      await loadData(1, pageSize);
      await loadMaterials();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "盘点失败");
      return false;
    }
  }

  async function handleAction(record: InventoryCheckRecord, action: "review" | "approve" | "reject") {
    const actionMap = {
      review: { request: reviewInventoryCheck, success: "盘点单已复核" },
      approve: { request: approveInventoryCheck, success: "盘点单已审批，差异已调账" },
      reject: { request: rejectInventoryCheck, success: "盘点单已驳回，冻结已释放" }
    };
    try {
      await actionMap[action].request(record.id);
      message.success(actionMap[action].success);
      await loadData(pageNum, pageSize);
      await loadMaterials();
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "盘点操作失败");
    }
  }

  const columns: ColumnsType<InventoryCheckRecord> = [
    { title: "盘点单号", dataIndex: "checkNo", key: "checkNo", width: 180 },
    {
      title: "盘点类型",
      dataIndex: "checkType",
      key: "checkType",
      width: 140,
      render: (value: string) => checkTypeOptions.find((item) => item.value === value)?.label ?? value
    },
    {
      title: "差异合计",
      dataIndex: "totalDifference",
      key: "totalDifference",
      width: 120,
      render: (value: number) => <Text type={value < 0 ? "danger" : value > 0 ? "success" : undefined}>{value}</Text>
    },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 120,
      render: (value: string) => {
        const status = statusMap[value] ?? { label: value, color: "default" };
        return <Tag color={status.color}>{status.label}</Tag>;
      }
    },
    { title: "备注", dataIndex: "remark", key: "remark" },
    { title: "创建时间", dataIndex: "createdAt", key: "createdAt", width: 180 },
    {
      title: "操作",
      key: "actions",
      width: 260,
      fixed: "right",
      render: (_, record) => (
        <Space size={4}>
          {record.status === "FROZEN" && (
            <Button type="link" icon={<SafetyCertificateOutlined />} onClick={() => void handleAction(record, "review")}>
              复核
            </Button>
          )}
          {record.status === "REVIEWED" && (
            <Popconfirm
              title="确认审批通过？"
              description="审批通过后会释放冻结并按差异生成盘盈/盘亏流水。"
              onConfirm={() => void handleAction(record, "approve")}
            >
              <Button type="link" icon={<CheckCircleOutlined />}>审批</Button>
            </Popconfirm>
          )}
          {(record.status === "FROZEN" || record.status === "REVIEWED") && (
            <Popconfirm
              title="确认驳回盘点单？"
              description="驳回后会释放本次盘点冻结库存，不产生调账流水。"
              onConfirm={() => void handleAction(record, "reject")}
            >
              <Button type="link" danger icon={<CloseCircleOutlined />}>驳回</Button>
            </Popconfirm>
          )}
          <Button type="link" onClick={() => printInventoryDocument("check", record)}>打印</Button>
        </Space>
      )
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>库存管理 / 盘点管理</Title>
          <Text type="secondary">录入实盘数量后冻结账面库存，复核审批通过后再生成盘盈/盘亏库存流水。</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
          新建盘点
        </Button>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={records}
        loading={loading}
        scroll={{ x: 1000 }}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadData(nextPageNum, nextPageSize)
        }}
      />

      <ModalForm<InventoryCheckPayload>
        title="新建盘点"
        open={createOpen}
        modalProps={{ destroyOnHidden: true, onCancel: () => setCreateOpen(false), width: 760 }}
        initialValues={{
          checkType: "FULL",
          batchNo: "DEFAULT",
          items: [{}]
        }}
        onFinish={handleCreate}
      >
        <ProFormSelect
          name="checkType"
          label="盘点类型"
          options={checkTypeOptions}
          rules={[{ required: true, message: "请选择盘点类型" }]}
        />
        <ProFormSelect
          name="locationId"
          label="盘点库位"
          options={locationOptions}
          rules={[{ required: true, message: "请选择盘点库位" }]}
        />
        <ProFormText name="batchNo" label="批次" rules={[{ required: true, message: "请输入批次" }]} />
        <ProFormText name="remark" label="备注" />
        <ProFormList
          name="items"
          label="盘点明细"
          creatorButtonProps={{ creatorButtonText: "添加明细" }}
          rules={[
            {
              validator: async (_, value) => {
                if (!value || value.length === 0) {
                  throw new Error("至少添加一条盘点明细");
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
                  账面库存：{material?.currentStock ?? "-"} {material?.unit ?? ""}
                </Text>
              );
            }}
          </ProFormDependency>
          <ProFormDigit
            name="actualQuantity"
            label="实盘数量"
            min={0}
            fieldProps={{ precision: 2 }}
            rules={[{ required: true, message: "请输入实盘数量" }]}
          />
          <ProFormText name="remark" label="明细备注" />
        </ProFormList>
      </ModalForm>
    </section>
  );
}

function normalizeCheckPayload(values: Record<string, any>): InventoryCheckPayload {
  const { locationId: _locationId, warehouseId: _warehouseId, items, ...rest } = values;
  return {
    ...rest,
    items: items ?? []
  } as InventoryCheckPayload;
}
