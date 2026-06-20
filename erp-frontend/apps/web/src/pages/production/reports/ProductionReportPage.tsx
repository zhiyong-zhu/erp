import { PlayCircleOutlined, ToolOutlined } from "@ant-design/icons";
import { ModalForm, ProFormDigit, ProFormText, ProFormTextArea } from "@ant-design/pro-components";
import { App, Button, Divider, Input, Modal, Space, Table, Tag, Typography } from "antd";
import { PRODUCTION_PERMISSIONS } from "@erp/shared";
import type { ColumnsType } from "antd/es/table";
import { useEffect, useState } from "react";
import {
  createProductionBox,
  createProductionReport,
  fetchProductionBatches,
  fetchProductionBoxes,
  fetchProductionProductStock,
  fetchProductionReports,
  generateBatchSerialNumbers,
  receiveProductionBatch,
  startProductionBatch
} from "../../../api/production";
import { hasPermission } from "../../../store/auth";
import type {
  ProductionBatchRecord,
  ProductionBoxPayload,
  ProductionBoxRecord,
  ProductionProductStockRecord,
  ProductionReportPayload,
  ProductionReportRecord,
  SerialNumberGeneratePayload
} from "../../../types/production";

const { Title, Text } = Typography;

const batchStatusMap: Record<string, { text: string; color?: string }> = {
  DRAFT: { text: "草稿", color: "default" },
  RELEASED: { text: "已下达", color: "processing" },
  IN_PROGRESS: { text: "生产中", color: "blue" },
  COMPLETED: { text: "已完工", color: "success" },
  CLOSED: { text: "已关闭", color: "default" }
};

const reportStatusMap: Record<string, { text: string; color?: string }> = {
  SUBMITTED: { text: "已提交", color: "success" }
};

function canGenerateSerials(status: string) {
  return status !== "CLOSED";
}

function canStartBatch(status: string) {
  return status === "DRAFT" || status === "RELEASED";
}

function canReportBatch(status: string) {
  return status === "IN_PROGRESS";
}

function canPackBatch(status: string) {
  return status === "IN_PROGRESS" || status === "COMPLETED";
}

function canReceiveBatch(status: string) {
  return status === "COMPLETED";
}

export function ProductionReportPage() {
  const [batchLoading, setBatchLoading] = useState(false);
  const [reportLoading, setReportLoading] = useState(false);
  const [batches, setBatches] = useState<ProductionBatchRecord[]>([]);
  const [reports, setReports] = useState<ProductionReportRecord[]>([]);
  const [boxes, setBoxes] = useState<ProductionBoxRecord[]>([]);
  const [stocks, setStocks] = useState<ProductionProductStockRecord[]>([]);
  const [batchPageNum, setBatchPageNum] = useState(1);
  const [batchPageSize, setBatchPageSize] = useState(10);
  const [batchTotal, setBatchTotal] = useState(0);
  const [reportPageNum, setReportPageNum] = useState(1);
  const [reportPageSize, setReportPageSize] = useState(10);
  const [reportTotal, setReportTotal] = useState(0);
  const [keyword, setKeyword] = useState("");
  const [reportingBatch, setReportingBatch] = useState<ProductionBatchRecord | null>(null);
  const [serialBatch, setSerialBatch] = useState<ProductionBatchRecord | null>(null);
  const [packingBatch, setPackingBatch] = useState<ProductionBatchRecord | null>(null);
  const { message } = App.useApp();
  const canCreateReport = hasPermission(PRODUCTION_PERMISSIONS.REPORT_CREATE);

  useEffect(() => {
    void loadBatches();
    void loadReports();
    void loadBoxes();
    void loadStocks();
  }, []);

  async function loadBatches(nextPageNum = batchPageNum, nextPageSize = batchPageSize, batchNo = keyword) {
    setBatchLoading(true);
    try {
      const data = await fetchProductionBatches({ pageNum: nextPageNum, pageSize: nextPageSize, batchNo });
      setBatches(data.records);
      setBatchPageNum(data.pageNum);
      setBatchPageSize(data.pageSize);
      setBatchTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载生产批次失败");
    } finally {
      setBatchLoading(false);
    }
  }

  async function loadReports(nextPageNum = reportPageNum, nextPageSize = reportPageSize, batchNo = keyword) {
    setReportLoading(true);
    try {
      const data = await fetchProductionReports({ pageNum: nextPageNum, pageSize: nextPageSize, batchNo });
      setReports(data.records);
      setReportPageNum(data.pageNum);
      setReportPageSize(data.pageSize);
      setReportTotal(data.total);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "加载报工记录失败");
    } finally {
      setReportLoading(false);
    }
  }

  async function loadBoxes(batchNo = keyword) {
    const data = await fetchProductionBoxes({ pageNum: 1, pageSize: 10, batchNo });
    setBoxes(data.records);
  }

  async function loadStocks(productName = "") {
    const data = await fetchProductionProductStock({ pageNum: 1, pageSize: 10, productName });
    setStocks(data.records);
  }

  async function handleSearch(value: string) {
    setKeyword(value);
    await Promise.all([loadBatches(1, batchPageSize, value), loadReports(1, reportPageSize, value), loadBoxes(value)]);
  }

  async function handleStart(batch: ProductionBatchRecord) {
    try {
      await startProductionBatch(batch.id);
      message.success("已投产");
      await loadBatches();
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "投产失败");
    }
  }

  function confirmStart(batch: ProductionBatchRecord) {
    Modal.confirm({
      title: "确认投产？",
      content: `批次号：${batch.batchNo}`,
      okText: "确认",
      cancelText: "取消",
      onOk: () => handleStart(batch)
    });
  }

  async function handleGenerateSerials(values: SerialNumberGeneratePayload) {
    if (!serialBatch) return false;
    try {
      const created = await generateBatchSerialNumbers(serialBatch.id, values);
      message.success(`已生成 ${created.length} 个序列号`);
      setSerialBatch(null);
      await loadBatches();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "生成序列号失败");
      return false;
    }
  }

  async function handlePack(values: ProductionBoxPayload) {
    if (!packingBatch) return false;
    try {
      await createProductionBox({ ...values, batchId: packingBatch.id });
      message.success("装箱完成，箱码已生成");
      setPackingBatch(null);
      await loadBoxes();
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "装箱失败");
      return false;
    }
  }

  async function handleReceipt(batch: ProductionBatchRecord) {
    try {
      await receiveProductionBatch(batch.id);
      message.success("生产入库完成，成品库存已更新");
      await Promise.all([loadBatches(), loadStocks()]);
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "生产入库失败");
    }
  }

  function confirmReceipt(batch: ProductionBatchRecord) {
    Modal.confirm({
      title: "确认完工入库？",
      content: `批次 ${batch.batchNo} 将以完成数 ${batch.completedQuantity ?? 0} 写入成品库存和库存流水。`,
      okText: "确认入库",
      cancelText: "取消",
      onOk: () => handleReceipt(batch)
    });
  }

  async function handleReport(values: ProductionReportPayload) {
    if (!reportingBatch) {
      return false;
    }
    try {
      await createProductionReport({ ...values, batchId: reportingBatch.id });
      message.success("报工已提交");
      setReportingBatch(null);
      await Promise.all([loadBatches(), loadReports(1, reportPageSize), loadStocks()]);
      return true;
    } catch (err: any) {
      message.error(err?.response?.data?.message ?? err?.message ?? "报工失败");
      return false;
    }
  }

  const batchColumns: ColumnsType<ProductionBatchRecord> = [
    { title: "批次号", dataIndex: "batchNo", key: "batchNo", width: 160 },
    { title: "产品", dataIndex: "productName", key: "productName", render: (value, record) => value ?? record.productCode ?? record.productId },
    { title: "计划数", dataIndex: "plannedQuantity", key: "plannedQuantity", width: 100 },
    { title: "完成数", dataIndex: "completedQuantity", key: "completedQuantity", width: 100, render: (value) => value ?? 0 },
    { title: "单位", dataIndex: "unit", key: "unit", width: 80 },
    { title: "工艺", dataIndex: "processName", key: "processName", width: 150, render: (value, record) => value ?? record.processId ?? "-" },
    { title: "BOM", dataIndex: "bomCode", key: "bomCode", width: 120, render: (value, record) => value ?? record.bomId ?? "-" },
    { title: "状态", dataIndex: "status", key: "status", width: 110, render: (value: string) => renderBatchStatus(value) },
    {
      title: "流程操作",
      key: "actions",
      width: 360,
      render: (_, record) => {
        return (
          <Space>
            <Button size="small" disabled={!canCreateReport || !canGenerateSerials(record.status)} onClick={() => setSerialBatch(record)}>
              生成码
            </Button>
            <Button size="small" icon={<PlayCircleOutlined />} disabled={!canCreateReport || !canStartBatch(record.status)} onClick={() => confirmStart(record)}>
              投产
            </Button>
            <Button size="small" type="primary" icon={<ToolOutlined />} disabled={!canCreateReport || !canReportBatch(record.status)} onClick={() => setReportingBatch(record)}>
              报工
            </Button>
            <Button size="small" disabled={!canCreateReport || !canPackBatch(record.status)} onClick={() => setPackingBatch(record)}>
              装箱
            </Button>
            <Button size="small" disabled={!canCreateReport || !canReceiveBatch(record.status)} onClick={() => confirmReceipt(record)}>
              入库
            </Button>
          </Space>
        );
      }
    }
  ];

  const reportColumns: ColumnsType<ProductionReportRecord> = [
    { title: "报工单号", dataIndex: "reportNo", key: "reportNo", width: 160 },
    { title: "批次号", dataIndex: "batchNo", key: "batchNo", width: 150 },
    { title: "产品", dataIndex: "productName", key: "productName", render: (value, record) => value ?? record.productCode ?? record.productId },
    { title: "报工数", dataIndex: "reportQuantity", key: "reportQuantity", width: 100 },
    { title: "良品数", dataIndex: "goodQuantity", key: "goodQuantity", width: 100 },
    { title: "不良数", dataIndex: "defectQuantity", key: "defectQuantity", width: 100 },
    { title: "报工时间", dataIndex: "reportAt", key: "reportAt", width: 180, render: (value) => formatDateTime(value) },
    { title: "操作人", dataIndex: "operatorName", key: "operatorName", width: 120 },
    { title: "状态", dataIndex: "status", key: "status", width: 100, render: (value: string) => renderReportStatus(value) },
    { title: "备注", dataIndex: "remark", key: "remark" }
  ];

  const boxColumns: ColumnsType<ProductionBoxRecord> = [
    { title: "箱码", dataIndex: "boxCode", key: "boxCode", width: 180 },
    { title: "批次号", dataIndex: "batchNo", key: "batchNo", width: 150 },
    { title: "产品", dataIndex: "productName", key: "productName", render: (value, record) => value ?? record.productCode ?? record.productId },
    { title: "包装规格", dataIndex: "packageName", key: "packageName", width: 140 },
    { title: "数量", dataIndex: "quantity", key: "quantity", width: 100 },
    { title: "状态", dataIndex: "status", key: "status", width: 100, render: () => <Tag color="success">已装箱</Tag> },
    { title: "标签预览", dataIndex: "labelHtml", key: "labelHtml", width: 110, render: (value) => <Button size="small" disabled={!value} onClick={() => previewLabel(value)}>预览</Button> }
  ];

  const stockColumns: ColumnsType<ProductionProductStockRecord> = [
    { title: "产品编码", dataIndex: "productCode", key: "productCode", width: 140 },
    { title: "产品名称", dataIndex: "productName", key: "productName" },
    { title: "成品库存", dataIndex: "currentStock", key: "currentStock", width: 120 }
  ];

  return (
    <section>
      <div className="page-header">
        <div>
          <Title level={3} style={{ margin: 0 }}>生产管理 / 生产执行</Title>
          <Text type="secondary">从生产工单到批次/序列号、投产报工、装箱打码、完工入库的完整执行链路。</Text>
        </div>
        <Input.Search
          allowClear
          placeholder="搜索批次号"
          value={keyword}
          onChange={(event) => setKeyword(event.target.value)}
          onSearch={(value) => void handleSearch(value)}
          style={{ width: 260 }}
        />
      </div>

      <Table
        rowKey="id"
        columns={batchColumns}
        dataSource={batches}
        loading={batchLoading}
        scroll={{ x: 1180 }}
        pagination={{
          current: batchPageNum,
          pageSize: batchPageSize,
          total: batchTotal,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadBatches(nextPageNum, nextPageSize)
        }}
      />

      <Divider orientation="left">箱码与装箱</Divider>

      <Table rowKey="id" columns={boxColumns} dataSource={boxes} pagination={false} scroll={{ x: 900 }} />

      <Divider orientation="left">报工记录</Divider>

      <Table
        rowKey="id"
        columns={reportColumns}
        dataSource={reports}
        loading={reportLoading}
        scroll={{ x: 1200 }}
        pagination={{
          current: reportPageNum,
          pageSize: reportPageSize,
          total: reportTotal,
          showSizeChanger: true,
          showTotal: (count) => `共 ${count} 条`,
          onChange: (nextPageNum, nextPageSize) => void loadReports(nextPageNum, nextPageSize)
        }}
      />

      <Divider orientation="left">成品库存</Divider>

      <Table rowKey="id" columns={stockColumns} dataSource={stocks} pagination={false} />

      <ReportForm batch={reportingBatch} onCancel={() => setReportingBatch(null)} onFinish={handleReport} />
      <SerialGenerateForm batch={serialBatch} onCancel={() => setSerialBatch(null)} onFinish={handleGenerateSerials} />
      <PackForm batch={packingBatch} onCancel={() => setPackingBatch(null)} onFinish={handlePack} />
    </section>
  );
}

function ReportForm({
  batch,
  onCancel,
  onFinish
}: {
  batch: ProductionBatchRecord | null;
  onCancel: () => void;
  onFinish: (values: ProductionReportPayload) => Promise<boolean>;
}) {
  return (
    <ModalForm<ProductionReportPayload>
      title={batch ? `批次 ${batch.batchNo} 报工` : "生产报工"}
      open={!!batch}
      width={760}
      grid
      rowProps={{ gutter: 16 }}
      initialValues={{
        batchId: batch?.id,
        reportQuantity: 1,
        goodQuantity: 1,
        defectQuantity: 0
      }}
      modalProps={{ destroyOnClose: true, onCancel }}
      onFinish={onFinish}
    >
      <ProFormText name="batchId" label="批次ID" readonly colProps={{ span: 24 }} />
      <ProFormText name="reportNo" label="报工单号" placeholder="留空自动生成" colProps={{ xs: 24, md: 12 }} />
      <ProFormDigit name="reportQuantity" label="报工数量" min={0.0001} fieldProps={{ precision: 4 }} rules={[{ required: true }]} colProps={{ xs: 24, md: 8 }} />
      <ProFormDigit name="goodQuantity" label="良品数量" min={0} fieldProps={{ precision: 4 }} colProps={{ xs: 24, md: 8 }} />
      <ProFormDigit name="defectQuantity" label="不良数量" min={0} fieldProps={{ precision: 4 }} colProps={{ xs: 24, md: 8 }} />
      <ProFormText name="reportAt" label="报工时间" placeholder="YYYY-MM-DD HH:mm:ss，留空为当前时间" colProps={{ xs: 24, md: 12 }} />
      <ProFormText name="operatorName" label="操作人" colProps={{ xs: 24, md: 12 }} />
      <ProFormTextArea name="remark" label="备注" colProps={{ span: 24 }} />
    </ModalForm>
  );
}

function SerialGenerateForm({
  batch,
  onCancel,
  onFinish
}: {
  batch: ProductionBatchRecord | null;
  onCancel: () => void;
  onFinish: (values: SerialNumberGeneratePayload) => Promise<boolean>;
}) {
  return (
    <ModalForm<SerialNumberGeneratePayload>
      title={batch ? `批次 ${batch.batchNo} 生成批次/序列号` : "生成序列号"}
      open={!!batch}
      width={520}
      initialValues={{ quantity: batch?.plannedQuantity, prefix: batch?.batchNo }}
      modalProps={{ destroyOnClose: true, onCancel }}
      onFinish={onFinish}
    >
      <ProFormDigit name="quantity" label="生成数量" min={1} fieldProps={{ precision: 0 }} rules={[{ required: true }]} />
      <ProFormText name="prefix" label="编码前缀" />
    </ModalForm>
  );
}

function PackForm({
  batch,
  onCancel,
  onFinish
}: {
  batch: ProductionBatchRecord | null;
  onCancel: () => void;
  onFinish: (values: ProductionBoxPayload) => Promise<boolean>;
}) {
  return (
    <ModalForm<ProductionBoxPayload>
      title={batch ? `批次 ${batch.batchNo} 装箱` : "装箱"}
      open={!!batch}
      width={720}
      grid
      rowProps={{ gutter: 16 }}
      initialValues={{ batchId: batch?.id, quantity: batch?.completedQuantity ?? 1 }}
      modalProps={{ destroyOnClose: true, onCancel }}
      onFinish={onFinish}
    >
      <ProFormText name="batchId" label="批次ID" readonly colProps={{ span: 24 }} />
      <ProFormText name="packageId" label="包装规格ID" rules={[{ required: true }]} colProps={{ xs: 24, md: 12 }} />
      <ProFormDigit name="quantity" label="装箱数量" min={0.0001} fieldProps={{ precision: 4 }} colProps={{ xs: 24, md: 12 }} />
      <ProFormTextArea name="remark" label="备注" colProps={{ span: 24 }} />
    </ModalForm>
  );
}

function renderBatchStatus(status: string) {
  const item = batchStatusMap[status] ?? { text: status, color: "default" };
  return <Tag color={item.color}>{item.text}</Tag>;
}

function renderReportStatus(status: string) {
  const item = reportStatusMap[status] ?? { text: status, color: "default" };
  return <Tag color={item.color}>{item.text}</Tag>;
}

function formatDateTime(value?: string | null) {
  if (!value) return "-";
  return value.replace("T", " ").replace(/\+\d{2}:\d{2}$/, "");
}

function previewLabel(html?: string | null) {
  if (!html) return;
  const popup = window.open("", "_blank", "width=420,height=520");
  if (!popup) return;
  popup.document.write(`<html><head><title>箱码标签</title></head><body>${html}</body></html>`);
  popup.document.close();
}
