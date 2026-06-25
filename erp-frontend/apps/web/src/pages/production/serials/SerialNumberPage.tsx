import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormSelect, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { App, Button, Input, Space, Table, Tag, Typography } from "antd";
import { PRODUCTION_PERMISSIONS } from "@erp/shared";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { createSerialNumber, fetchSerialNumbers, updateSerialNumber } from "../../../api/production";
import { hasPermission } from "../../../store/auth";
import type { SerialNumberPayload, SerialNumberRecord } from "../../../types/production";

const { Title, Text } = Typography;

const serialStatusOptions = [
  { label: "已生成", value: "GENERATED" },
  { label: "已装箱", value: "PACKED" },
  { label: "已入库", value: "STOCKED" },
  { label: "已发货", value: "SHIPPED" },
  { label: "已报废", value: "SCRAPPED" }
];

export function SerialNumberPage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<SerialNumberRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<SerialNumberRecord | null>(null);
  const { message } = App.useApp();
  const canCreate = hasPermission(PRODUCTION_PERMISSIONS.SERIAL_CREATE);
  const canUpdate = hasPermission(PRODUCTION_PERMISSIONS.SERIAL_UPDATE);

  useEffect(() => {
    void loadData();
  }, []);

  async function loadData(nextPageNum = pageNum, nextPageSize = pageSize, serialNo = keyword) {
    setLoading(true);
    try {
      const data = await fetchSerialNumbers({ pageNum: nextPageNum, pageSize: nextPageSize, serialNo });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载序列号失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(values: SerialNumberPayload) {
    try {
      await createSerialNumber(values);
      message.success("序列号已创建");
      setCreateOpen(false);
      await loadData(1, pageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "创建序列号失败");
      return false;
    }
  }

  async function handleUpdate(values: SerialNumberPayload) {
    if (!editingRecord?.id) return false;
    try {
      await updateSerialNumber(editingRecord.id, values);
      message.success("序列号已更新");
      setEditingRecord(null);
      await loadData();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "更新序列号失败");
      return false;
    }
  }

  const columns: ColumnsType<SerialNumberRecord> = [
    { title: "序列号", dataIndex: "serialNo", key: "serialNo", width: 190 },
    { title: "产品", dataIndex: "productName", key: "productName", render: (value, record) => value ?? record.productCode ?? record.productId },
    { title: "批次", dataIndex: "batchNo", key: "batchNo", width: 170, render: (value, record) => value ?? record.batchId ?? "-" },
    { title: "状态", dataIndex: "status", key: "status", width: 120, render: (value: string) => <Tag>{formatStatus(value)}</Tag> },
    { title: "生产时间", dataIndex: "producedAt", key: "producedAt", width: 170 },
    { title: "发货时间", dataIndex: "shippedAt", key: "shippedAt", width: 170 },
    { title: "备注", dataIndex: "remark", key: "remark" },
    {
      title: "操作",
      key: "actions",
      width: 110,
      render: (_, record) => (
        <Button type="link" disabled={!canUpdate} onClick={() => setEditingRecord(record)}>
          编辑
        </Button>
      )
    }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>生产管理 / 序列号追溯</Title>
          <Text type="secondary">跟踪单件序列号的批次、生产状态、检验和发货信息。</Text>
        </div>
        <Space>
          <Input.Search
            allowClear
            placeholder="搜索序列号"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            onSearch={(value) => {
              setKeyword(value);
              void loadData(1, pageSize, value);
            }}
            style={{ width: 240 }}
          />
          <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateOpen(true)}>
            新建序列号
          </Button>
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

      <SerialForm title="新建序列号" open={createOpen} onCancel={() => setCreateOpen(false)} onFinish={handleCreate} />
      <SerialForm title="编辑序列号" open={!!editingRecord} initialValues={editingRecord ? toSerialInitialValues(editingRecord) : undefined} onCancel={() => setEditingRecord(null)} onFinish={handleUpdate} />
    </section>
  );
}

function SerialForm({
  title,
  open,
  initialValues,
  onCancel,
  onFinish
}: {
  title: string;
  open: boolean;
  initialValues?: Partial<SerialNumberPayload>;
  onCancel: () => void;
  onFinish: (values: SerialNumberPayload) => Promise<boolean>;
}) {
  return (
    <ModalForm<SerialNumberPayload>
      title={title}
      open={open}
      width={820}
      grid
      rowProps={{ gutter: 16 }}
      initialValues={initialValues ?? { status: "GENERATED" }}
      modalProps={{ destroyOnClose: true, onCancel }}
      onFinish={onFinish}
    >
      <ProFormText name="serialNo" label="序列号" rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="productId" label="产品ID" rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="batchId" label="批次ID" colProps={{ xs: 24, md: 8 }} />
      <ProFormSelect name="status" label="状态" options={serialStatusOptions} colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="producedAt" label="生产时间" placeholder="YYYY-MM-DD HH:mm:ss" colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="shippedAt" label="发货时间" placeholder="YYYY-MM-DD HH:mm:ss" colProps={{ xs: 24, md: 8 }} />
      <ProFormTextArea name="remark" label="备注" colProps={{ span: 24 }} />
    </ModalForm>
  );
}

function formatStatus(status: string) {
  return serialStatusOptions.find((item) => item.value === status)?.label ?? status;
}

function toSerialInitialValues(record: SerialNumberRecord): Partial<SerialNumberPayload> {
  return {
    serialNo: record.serialNo,
    batchId: record.batchId,
    productId: record.productId,
    status: record.status,
    producedAt: record.producedAt ?? undefined,
    shippedAt: record.shippedAt ?? undefined,
    remark: record.remark ?? undefined
  };
}
