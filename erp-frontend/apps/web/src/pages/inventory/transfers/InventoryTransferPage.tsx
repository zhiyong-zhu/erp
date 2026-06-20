import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDependency, ProFormDigit, ProFormList, ProFormSelect, ProFormText } from "@ant-design/pro-components";
import { App, Button, Table, Tag, Typography } from "antd";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useMemo, useState } from "react";
import { createInventoryTransfer, fetchInventoryTransfers } from "../../../api/inventory";
import { fetchMaterials } from "../../../api/material";
import type { InventoryTransferPayload, InventoryTransferRecord } from "../../../types/inventory";
import type { MaterialRecord } from "../../../types/material";

const { Title, Text } = Typography;

export function InventoryTransferPage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<InventoryTransferRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);
  const [materials, setMaterials] = useState<MaterialRecord[]>([]);
  const { message } = App.useApp();

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
  }, []);

  async function loadData(nextPageNum = pageNum, nextPageSize = pageSize) {
    setLoading(true);
    try {
      const data = await fetchInventoryTransfers({ pageNum: nextPageNum, pageSize: nextPageSize });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载调拨单失败");
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

  async function handleCreate(values: InventoryTransferPayload) {
    try {
      await createInventoryTransfer(values);
      message.success("调拨完成");
      setCreateOpen(false);
      await loadData(1, pageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "调拨失败");
      return false;
    }
  }

  const columns: ColumnsType<InventoryTransferRecord> = [
    { title: "调拨单号", dataIndex: "transferNo", key: "transferNo", width: 180 },
    { title: "调出位置", dataIndex: "fromLocation", key: "fromLocation", width: 160 },
    { title: "调入位置", dataIndex: "toLocation", key: "toLocation", width: 160 },
    { title: "调拨总量", dataIndex: "totalQuantity", key: "totalQuantity", width: 120 },
    {
      title: "状态",
      dataIndex: "status",
      key: "status",
      width: 120,
      render: (value: string) => <Tag color={value === "COMPLETED" ? "green" : "default"}>{value}</Tag>
    },
    { title: "备注", dataIndex: "remark", key: "remark" },
    { title: "创建时间", dataIndex: "createdAt", key: "createdAt", width: 180 }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>库存管理 / 调拨管理</Title>
          <Text type="secondary">登记原料在仓库、区域或货位之间的调拨，记录调出与调入流水。</Text>
        </div>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => setCreateOpen(true)}>
          新建调拨
        </Button>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={records}
        loading={loading}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadData(nextPageNum, nextPageSize)
        }}
      />

      <ModalForm<InventoryTransferPayload>
        title="新建调拨"
        open={createOpen}
        modalProps={{ destroyOnHidden: true, onCancel: () => setCreateOpen(false), width: 760 }}
        initialValues={{ items: [{}] }}
        onFinish={handleCreate}
      >
        <ProFormText name="fromLocation" label="调出位置" placeholder="如：主仓 / A区 / A-01" rules={[{ required: true, message: "请输入调出位置" }]} />
        <ProFormText name="toLocation" label="调入位置" placeholder="如：副仓 / B区 / B-02" rules={[{ required: true, message: "请输入调入位置" }]} />
        <ProFormText name="remark" label="备注" />
        <ProFormList
          name="items"
          label="调拨明细"
          creatorButtonProps={{ creatorButtonText: "添加明细" }}
          rules={[
            {
              validator: async (_, value) => {
                if (!value || value.length === 0) {
                  throw new Error("至少添加一条调拨明细");
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
            label="调拨数量"
            min={0.01}
            fieldProps={{ precision: 2 }}
            rules={[{ required: true, message: "请输入调拨数量" }]}
          />
          <ProFormText name="remark" label="明细备注" />
        </ProFormList>
      </ModalForm>
    </section>
  );
}
