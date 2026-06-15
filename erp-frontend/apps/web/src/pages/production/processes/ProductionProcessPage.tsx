import { PlusOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormList, ProFormSelect, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { App, Button, Input, Space, Table, Tag, Typography } from "antd";
import { PRODUCTION_PERMISSIONS } from "@erp/shared";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import { createProductionProcess, fetchProductionProcesses, updateProductionProcess } from "../../../api/production";
import { hasPermission } from "../../../store/auth";
import type { ProductionProcessPayload, ProductionProcessRecord, ProductionProcessStepRecord } from "../../../types/production";

const { Title, Text } = Typography;

export function ProductionProcessPage() {
  const [loading, setLoading] = useState(false);
  const [records, setRecords] = useState<ProductionProcessRecord[]>([]);
  const [pageNum, setPageNum] = useState(1);
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [createOpen, setCreateOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<ProductionProcessRecord | null>(null);
  const { message } = App.useApp();
  const canCreate = hasPermission(PRODUCTION_PERMISSIONS.PROCESS_CREATE);
  const canUpdate = hasPermission(PRODUCTION_PERMISSIONS.PROCESS_UPDATE);

  useEffect(() => {
    void loadData();
  }, []);

  async function loadData(nextPageNum = pageNum, nextPageSize = pageSize, name = keyword) {
    setLoading(true);
    try {
      const data = await fetchProductionProcesses({ pageNum: nextPageNum, pageSize: nextPageSize, name });
      setRecords(data.records);
      setPageNum(data.pageNum);
      setPageSize(data.pageSize);
      setTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载工艺路线失败");
    } finally {
      setLoading(false);
    }
  }

  async function handleCreate(values: ProductionProcessPayload) {
    try {
      await createProductionProcess(normalizePayload(values));
      message.success("工艺路线已创建");
      setCreateOpen(false);
      await loadData(1, pageSize);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "创建工艺路线失败");
      return false;
    }
  }

  async function handleUpdate(values: ProductionProcessPayload) {
    if (!editingRecord?.id) return false;
    try {
      await updateProductionProcess(editingRecord.id, normalizePayload(values));
      message.success("工艺路线已更新");
      setEditingRecord(null);
      await loadData();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "更新工艺路线失败");
      return false;
    }
  }

  const columns: ColumnsType<ProductionProcessRecord> = [
    { title: "工艺编码", dataIndex: "code", key: "code", width: 150 },
    { title: "工艺名称", dataIndex: "name", key: "name" },
    { title: "产品", dataIndex: "productName", key: "productName", width: 180, render: (value, record) => value ?? record.productId ?? "-" },
    { title: "版本", dataIndex: "version", key: "version", width: 100 },
    { title: "工序数", key: "steps", width: 90, render: (_, record) => record.steps?.length ?? 0 },
    { title: "状态", dataIndex: "status", key: "status", width: 110, render: (value: number) => <Tag color={value === 1 ? "success" : "default"}>{value === 1 ? "启用" : "停用"}</Tag> },
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
          <Title level={3} style={{ margin: 0 }}>生产管理 / 工艺路线</Title>
          <Text type="secondary">维护产品工艺、工位、标准工时和质量要求。</Text>
        </div>
        <Space>
          <Input.Search
            allowClear
            placeholder="搜索工艺"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            onSearch={(value) => {
              setKeyword(value);
              void loadData(1, pageSize, value);
            }}
            style={{ width: 240 }}
          />
          <Button type="primary" icon={<PlusOutlined />} disabled={!canCreate} onClick={() => setCreateOpen(true)}>
            新建工艺
          </Button>
        </Space>
      </div>

      <Table
        rowKey="id"
        columns={columns}
        dataSource={records}
        loading={loading}
        expandable={{
          expandedRowRender: (record) => (
            <Table
              rowKey={(step) => String(step.id ?? step.stepNo)}
              size="small"
              pagination={false}
              columns={[
                { title: "工序号", dataIndex: "stepNo", key: "stepNo", width: 80 },
                { title: "工序名称", dataIndex: "name", key: "name" },
                { title: "工位", dataIndex: "workstation", key: "workstation" },
                { title: "标准工时", dataIndex: "standardMinutes", key: "standardMinutes", width: 110 },
                { title: "质量要求", dataIndex: "qualityRequirement", key: "qualityRequirement" },
                { title: "备注", dataIndex: "remark", key: "remark" }
              ]}
              dataSource={record.steps ?? []}
            />
          )
        }}
        pagination={{
          current: pageNum,
          pageSize,
          total,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadData(nextPageNum, nextPageSize)
        }}
      />

      <ProcessForm title="新建工艺路线" open={createOpen} onCancel={() => setCreateOpen(false)} onFinish={handleCreate} />
      <ProcessForm
        title="编辑工艺路线"
        open={!!editingRecord}
        initialValues={editingRecord ? toProcessInitialValues(editingRecord) : undefined}
        onCancel={() => setEditingRecord(null)}
        onFinish={handleUpdate}
      />
    </section>
  );
}

function ProcessForm({
  title,
  open,
  initialValues,
  onCancel,
  onFinish
}: {
  title: string;
  open: boolean;
  initialValues?: Partial<ProductionProcessPayload>;
  onCancel: () => void;
  onFinish: (values: ProductionProcessPayload) => Promise<boolean>;
}) {
  return (
    <ModalForm<ProductionProcessPayload>
      title={title}
      open={open}
      width={960}
      grid
      rowProps={{ gutter: 16 }}
      initialValues={initialValues ?? { version: "V1.0", status: 1, steps: [{ stepNo: 10, name: "下料" }] }}
      modalProps={{ destroyOnClose: true, onCancel }}
      onFinish={onFinish}
    >
      <ProFormText name="code" label="工艺编码" rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="name" label="工艺名称" rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="productId" label="产品ID" colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="version" label="版本" rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormSelect name="status" label="状态" options={[{ label: "启用", value: 1 }, { label: "停用", value: 0 }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormTextArea name="remark" label="备注" colProps={{ span: 24 }} />
      <ProFormList name="steps" label="工序明细" creatorButtonProps={{ creatorButtonText: "添加工序" }}>
        <ProFormDigit name="stepNo" label="工序号" min={1} fieldProps={{ precision: 0 }} rules={[{ required: true }]} />
        <ProFormText name="name" label="工序名称" rules={[{ required: true }]} />
        <ProFormText name="workstation" label="工位" />
        <ProFormDigit name="standardMinutes" label="标准工时" min={0} fieldProps={{ precision: 2 }} />
        <ProFormText name="qualityRequirement" label="质量要求" />
        <ProFormText name="remark" label="备注" />
      </ProFormList>
    </ModalForm>
  );
}

function normalizePayload(values: ProductionProcessPayload): ProductionProcessPayload {
  return {
    ...values,
    steps: (values.steps ?? []).map(({ rowId: _rowId, ...step }: ProductionProcessStepRecord) => step)
  };
}

function toProcessInitialValues(record: ProductionProcessRecord): Partial<ProductionProcessPayload> {
  return {
    code: record.code,
    name: record.name,
    productId: record.productId,
    version: record.version,
    status: record.status,
    remark: record.remark ?? undefined,
    steps: record.steps ?? []
  };
}
